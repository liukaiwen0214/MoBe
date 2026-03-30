package com.mobe.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mobe.common.exception.BizException;
import com.mobe.common.util.AuthUtil;
import com.mobe.common.util.TokenUtil;
import com.mobe.user.dto.*;
import com.mobe.user.entity.UserEntity;
import com.mobe.user.entity.UserSessionEntity;
import com.mobe.user.mapper.UserMapper;
import com.mobe.user.mapper.UserSessionMapper;
import com.mobe.user.entity.UserPreferencesEntity;
import com.mobe.user.mapper.UserPreferencesMapper;
import com.mobe.common.util.CaptchaUtil;
import com.mobe.user.entity.AuthCodeEntity;
import com.mobe.user.mapper.AuthCodeMapper;
import com.mobe.common.util.CodeUtil;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务类
 * <p>
 * 文件用途：实现用户相关的业务逻辑，包括注册、登录、登出、个人资料管理、偏好设置等功能
 * 所属模块：user（用户模块）
 * 核心职责：处理用户相关的业务逻辑，提供用户认证、信息管理、会话管理等功能
 * 与其他模块的关联：依赖于userMapper、userSessionMapper、userPreferencesMapper、authCodeMapper等数据访问层组件，使用common模块的工具类和异常类
 * 在整体业务流程中的位置：位于服务层，是用户相关业务逻辑的核心实现
 * 说明：使用 @Service 注解标记为服务类
 */
@Slf4j
@Service
public class UserService {

    /**
     * 用户Mapper
     */
    private final UserMapper userMapper;
    
    /**
     * 用户会话Mapper
     */
    private final UserSessionMapper userSessionMapper;
    
    /**
     * 密码编码器
     */
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 用户偏好设置Mapper
     */
    private final UserPreferencesMapper userPreferencesMapper;
    
    /**
     * 验证码Mapper
     */
    private final AuthCodeMapper authCodeMapper;

    /**
     * 构造方法
     * <p>
     * 功能：注入所需的依赖
     * @param userMapper 用户Mapper实例
     * @param userSessionMapper 用户会话Mapper实例
     * @param passwordEncoder 密码编码器实例
     * @param userPreferencesMapper 用户偏好设置Mapper实例
     * @param authCodeMapper 验证码Mapper实例
     */
    public UserService(UserMapper userMapper,
                       UserSessionMapper userSessionMapper,
                       PasswordEncoder passwordEncoder, UserPreferencesMapper userPreferencesMapper, AuthCodeMapper authCodeMapper) {
        this.userMapper = userMapper;
        this.userSessionMapper = userSessionMapper;
        this.passwordEncoder = passwordEncoder;
        this.userPreferencesMapper = userPreferencesMapper;
        this.authCodeMapper = authCodeMapper;
    }

    /**
     * 用户注册
     * <p>
     * 功能：处理用户注册请求
     * @param request 注册请求参数
     * @return String 注册结果
     */
    public String register(UserRegisterRequest request) {
        // 检查用户名是否已存在
        Long usernameCount = userMapper.selectCount(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getUsername, request.getUsername())
                        .eq(UserEntity::getIsDeleted, 0)
        );

        if (usernameCount != null && usernameCount > 0) {
            return "用户名已存在";
        }

        // 检查邮箱是否已存在
        Long emailCount = userMapper.selectCount(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getEmail, request.getEmail())
                        .eq(UserEntity::getIsDeleted, 0)
        );

        if (emailCount != null && emailCount > 0) {
            return "邮箱已存在";
        }

        // 创建新用户
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setStatus("ACTIVE");
        user.setIsDeleted(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        return "注册成功";
    }

    /**
     * 用户登录
     * <p>
     * 功能：处理用户登录请求
     * @param request 登录请求参数
     * @param httpServletRequest HTTP 请求对象
     * @return UserLoginResponse 登录结果，包含用户信息和令牌
     */
    public UserLoginResponse login(UserLoginRequest request, HttpServletRequest httpServletRequest) {
        // 1. 校验验证码
        AuthCodeEntity authCode = authCodeMapper.selectOne(
                new LambdaQueryWrapper<AuthCodeEntity>()
                        .eq(AuthCodeEntity::getId, request.getCaptchaId())
                        .eq(AuthCodeEntity::getType, "CAPTCHA")
                        .eq(AuthCodeEntity::getStatus, "ACTIVE")
                        .eq(AuthCodeEntity::getUsed, 0)
                        .eq(AuthCodeEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (authCode == null) {
            throw new BizException("验证码不存在或已失效");
        }

        if (authCode.getExpiresAt() == null || authCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            authCode.setStatus("EXPIRED");
            authCode.setUpdatedAt(LocalDateTime.now());
            authCodeMapper.updateById(authCode);
            throw new BizException("验证码已过期");
        }

        if (!authCode.getCode().equalsIgnoreCase(request.getCode())) {
            throw new BizException("验证码错误");
        }

        // 验证码校验成功，置为已使用
        authCode.setUsed(1);
        authCode.setUsedAt(LocalDateTime.now());
        authCode.setStatus("USED");
        authCode.setUpdatedAt(LocalDateTime.now());
        authCodeMapper.updateById(authCode);

        // 2. 根据用户名或邮箱查询用户
        UserEntity user = userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>()
                        .and(wrapper -> wrapper
                                .eq(UserEntity::getUsername, request.getAccount())
                                .or()
                                .eq(UserEntity::getEmail, request.getAccount())
                        )
                        .eq(UserEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (user == null) {
            throw new BizException("用户不存在");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BizException("用户状态不可用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BizException("密码错误");
        }

        // 3. 创建会话
        LocalDateTime now = LocalDateTime.now();
        int rememberMe = request.getRememberMe() != null ? request.getRememberMe() : 0;
        LocalDateTime expiresAt = rememberMe == 1 ? now.plusDays(7) : now.plusHours(12);

        UserSessionEntity session = new UserSessionEntity();
        session.setUserId(user.getId());
        session.setSessionToken(TokenUtil.generateSessionToken());
        session.setRememberMe(rememberMe);
        session.setExpiresAt(expiresAt);
        session.setLastAccessedAt(now);
        session.setDeviceType("WEB");
        session.setClientIp(getClientIp(httpServletRequest));
        session.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        session.setStatus("ACTIVE");
        session.setIsDeleted(0);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);

        userSessionMapper.insert(session);

        // 4. 构建登录响应
        UserLoginResponse response = new UserLoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setNickname(user.getNickname());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setSessionToken(session.getSessionToken());
        response.setExpiresAt(session.getExpiresAt());

        return response;
    }

    /**
     * 获取当前用户信息
     * <p>
     * 功能：获取当前登录用户的信息
     * @param request HTTP 请求对象
     * @return UserMeResponse 当前用户信息
     */
    public UserMeResponse getCurrentUser(HttpServletRequest request) {
        UserEntity user = getCurrentUserEntityByRequest(request);

        // 更新会话的最后访问时间
        UserSessionEntity session = getCurrentActiveSessionByRequest(request);
        session.setLastAccessedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        userSessionMapper.updateById(session);

        return buildUserMeResponse(user);
    }

    /**
     * 用户登出
     * <p>
     * 功能：处理用户登出请求
     * @param request HTTP 请求对象
     * @return String 登出结果
     */
    public String logout(HttpServletRequest request) {
        UserSessionEntity session = getValidSessionByRequest(request);

        // 更新会话状态为已登出
        session.setStatus("LOGOUT");
        session.setUpdatedAt(LocalDateTime.now());
        userSessionMapper.updateById(session);

        return "退出登录成功";
    }

    /**
     * 从请求中获取有效的会话
     * <p>
     * 功能：从请求中提取会话令牌并查询对应的会话
     * @param request HTTP 请求对象
     * @return UserSessionEntity 有效的会话实体
     */
    private UserSessionEntity getValidSessionByRequest(HttpServletRequest request) {
        String sessionToken = AuthUtil.getBearerToken(request);
        if (sessionToken == null || sessionToken.isBlank()) {
            throw new BizException("未登录或登录令牌缺失");
        }

        UserSessionEntity session = userSessionMapper.selectOne(
                new LambdaQueryWrapper<UserSessionEntity>()
                        .eq(UserSessionEntity::getSessionToken, sessionToken)
                        .eq(UserSessionEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (session == null) {
            throw new BizException("登录会话不存在");
        }

        return session;
    }

    /**
     * 修改密码
     * <p>
     * 功能：修改当前用户的密码
     * @param request 修改密码请求参数
     * @param httpServletRequest HTTP 请求对象
     * @return String 修改结果
     */
    public String updatePassword(UserPasswordUpdateRequest request, HttpServletRequest httpServletRequest) {
        UserEntity user = getCurrentUserEntityByRequest(httpServletRequest);

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BizException("旧密码错误");
        }

        // 检查新密码是否与旧密码相同
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BizException("新密码不能与旧密码相同");
        }

        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
        return "修改密码成功";
    }

    /**
     * 根据ID获取用户信息
     * <p>
     * 功能：根据用户ID获取用户信息
     * @param userId 用户ID
     * @return UserMeResponse 用户信息
     */
    public UserMeResponse getUserById(String userId) {
        UserEntity user = getUserByIdOrThrow(userId);
        return buildUserMeResponse(user);
    }

    /**
     * 获取会话列表
     * <p>
     * 功能：获取当前用户的所有会话列表
     * @param request HTTP 请求对象
     * @return List<UserSessionResponse> 会话列表
     */
    public List<UserSessionResponse> getSessionList(HttpServletRequest request) {
        UserSessionEntity currentSession = getCurrentActiveSessionByRequest(request);

        // 查询用户的所有会话
        List<UserSessionEntity> sessions = userSessionMapper.selectList(
                new LambdaQueryWrapper<UserSessionEntity>()
                        .eq(UserSessionEntity::getUserId, currentSession.getUserId())
                        .eq(UserSessionEntity::getIsDeleted, 0)
                        .orderByDesc(UserSessionEntity::getCreatedAt)
        );

        // 构建会话响应列表
        List<UserSessionResponse> result = new ArrayList<>();
        for (UserSessionEntity session : sessions) {
            result.add(buildSessionResponse(session, currentSession));
        }
        return result;
    }

    /**
     * 更新个人资料
     * <p>
     * 功能：更新当前用户的个人资料
     * @param request 更新个人资料请求参数
     * @param httpServletRequest HTTP 请求对象
     * @return String 更新结果
     */
    public String updateProfile(UserProfileUpdateRequest request, HttpServletRequest httpServletRequest) {
        UserEntity user = getCurrentUserEntityByRequest(httpServletRequest);

        // 更新个人资料
        user.setNickname(request.getNickname());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
        return "更新个人资料成功";
    }

    /**
     * 下线会话
     * <p>
     * 功能：下线指定的会话
     * @param sessionId 会话ID
     * @param request HTTP 请求对象
     * @return String 下线结果
     */
    public String deleteSession(String sessionId, HttpServletRequest request) {
        UserSessionEntity currentSession = getCurrentActiveSessionByRequest(request);

        // 查询目标会话
        UserSessionEntity targetSession = userSessionMapper.selectOne(
                new LambdaQueryWrapper<UserSessionEntity>()
                        .eq(UserSessionEntity::getId, sessionId)
                        .eq(UserSessionEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (targetSession == null) {
            throw new BizException("会话不存在");
        }

        // 检查权限
        if (!currentSession.getUserId().equals(targetSession.getUserId())) {
            throw new BizException("无权操作该会话");
        }

        // 更新会话状态为已登出
        targetSession.setStatus("LOGOUT");
        targetSession.setUpdatedAt(LocalDateTime.now());
        userSessionMapper.updateById(targetSession);

        return "会话已下线";
    }

    /**
     * 从请求中获取当前用户实体
     * <p>
     * 功能：从请求中获取当前登录用户的实体
     * @param request HTTP 请求对象
     * @return UserEntity 当前用户实体
     */
    private UserEntity getCurrentUserEntityByRequest(HttpServletRequest request) {
        UserSessionEntity session = getCurrentActiveSessionByRequest(request);
        return getUserByIdOrThrow(session.getUserId());
    }

    /**
     * 构建会话响应
     * <p>
     * 功能：将会话实体转换为响应对象
     * @param session 会话实体
     * @param currentSession 当前会话实体
     * @return UserSessionResponse 会话响应对象
     */
    private UserSessionResponse buildSessionResponse(UserSessionEntity session, UserSessionEntity currentSession) {
        UserSessionResponse item = new UserSessionResponse();
        item.setSessionId(session.getId());
        item.setDeviceType(session.getDeviceType());
        item.setClientIp(session.getClientIp());
        item.setUserAgent(session.getUserAgent());
        item.setRememberMe(session.getRememberMe());
        item.setStatus(session.getStatus());
        item.setExpiresAt(session.getExpiresAt());
        item.setLastAccessedAt(session.getLastAccessedAt());
        item.setCreatedAt(session.getCreatedAt());
        item.setCurrent(session.getId().equals(currentSession.getId()));
        return item;
    }

    /**
     * 验证会话状态
     * <p>
     * 功能：验证会话是否有效
     * @param session 会话实体
     */
    private void validateSessionStatus(UserSessionEntity session) {
        if (!"ACTIVE".equals(session.getStatus())) {
            throw new BizException("登录会话已失效");
        }

        if (session.getExpiresAt() == null || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus("EXPIRED");
            session.setUpdatedAt(LocalDateTime.now());
            userSessionMapper.updateById(session);
            throw new BizException("登录已过期，请重新登录");
        }
    }

    /**
     * 获取客户端IP地址
     * <p>
     * 功能：从请求中获取客户端的真实IP地址
     * @param request HTTP 请求对象
     * @return String 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        // 尝试从 X-Forwarded-For 头获取
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        // 尝试从 X-Real-IP 头获取
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp.trim();
        }

        // 使用默认的远程地址
        return request.getRemoteAddr();
    }

    /**
     * 获取用户偏好设置
     * <p>
     * 功能：获取当前用户的偏好设置
     * @param request HTTP 请求对象
     * @return UserPreferencesResponse 用户偏好设置
     */
    public UserPreferencesResponse getMyPreferences(HttpServletRequest request) {
        UserEntity user = getCurrentUserEntityByRequest(request);

        UserPreferencesEntity preferences = getUserPreferencesByUserId(user.getId());

        // 如果偏好设置不存在，创建默认设置
        if (preferences == null) {
            preferences = new UserPreferencesEntity();
            preferences.setUserId(user.getId());
            preferences.setTheme("light");
            preferences.setLanguage("zh-CN");
            preferences.setDefaultHomepage("dashboard");
            preferences.setDateFormat("YYYY-MM-DD");
            preferences.setTimeFormat("24h");
            preferences.setWeekStartDay("MONDAY");
            preferences.setNotificationEnabled(1);
            preferences.setEmailNotificationEnabled(0);
            preferences.setIsDeleted(0);
            preferences.setCreatedAt(LocalDateTime.now());
            preferences.setUpdatedAt(LocalDateTime.now());
            userPreferencesMapper.insert(preferences);
        }

        return buildUserPreferencesResponse(preferences);
    }

    /**
     * 更新用户偏好设置
     * <p>
     * 功能：更新当前用户的偏好设置
     * @param request 更新偏好设置请求参数
     * @param httpServletRequest HTTP 请求对象
     * @return String 更新结果
     */
    public String updateMyPreferences(UserPreferencesUpdateRequest request, HttpServletRequest httpServletRequest) {
        UserEntity user = getCurrentUserEntityByRequest(httpServletRequest);

        UserPreferencesEntity preferences = getUserPreferencesByUserId(user.getId());

        // 如果偏好设置不存在，创建新的
        if (preferences == null) {
            preferences = new UserPreferencesEntity();
            preferences.setUserId(user.getId());
            preferences.setIsDeleted(0);
            preferences.setCreatedAt(LocalDateTime.now());
        }

        // 应用更新
        applyPreferencesUpdate(preferences, request);
        preferences.setUpdatedAt(LocalDateTime.now());

        // 保存更新
        if (preferences.getId() == null) {
            userPreferencesMapper.insert(preferences);
        } else {
            userPreferencesMapper.updateById(preferences);
        }

        return "更新用户偏好成功";
    }

    /**
     * 应用偏好设置更新
     * <p>
     * 功能：将请求参数应用到偏好设置实体
     * @param preferences 偏好设置实体
     * @param request 更新偏好设置请求参数
     */
    private void applyPreferencesUpdate(UserPreferencesEntity preferences, UserPreferencesUpdateRequest request) {
        preferences.setTheme(request.getTheme());
        preferences.setLanguage(request.getLanguage());
        preferences.setDefaultHomepage(request.getDefaultHomepage());
        preferences.setDateFormat(request.getDateFormat());
        preferences.setTimeFormat(request.getTimeFormat());
        preferences.setWeekStartDay(request.getWeekStartDay());
        preferences.setNotificationEnabled(request.getNotificationEnabled());
        preferences.setEmailNotificationEnabled(request.getEmailNotificationEnabled());
    }

    /**
     * 从请求中获取当前活跃的会话
     * <p>
     * 功能：从请求中获取当前登录用户的活跃会话
     * @param request HTTP 请求对象
     * @return UserSessionEntity 当前活跃的会话实体
     */
    private UserSessionEntity getCurrentActiveSessionByRequest(HttpServletRequest request) {
        UserSessionEntity session = getValidSessionByRequest(request);
        validateSessionStatus(session);
        return session;
    }

    /**
     * 根据ID获取用户实体，如果不存在则抛出异常
     * <p>
     * 功能：根据用户ID获取用户实体
     * @param userId 用户ID
     * @return UserEntity 用户实体
     */
    private UserEntity getUserByIdOrThrow(String userId) {
        UserEntity user = userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getId, userId)
                        .eq(UserEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (user == null) {
            throw new BizException("用户不存在");
        }

        return user;
    }

    /**
     * 根据用户ID获取偏好设置
     * <p>
     * 功能：根据用户ID获取用户的偏好设置
     * @param userId 用户ID
     * @return UserPreferencesEntity 用户偏好设置实体
     */
    private UserPreferencesEntity getUserPreferencesByUserId(String userId) {
        return userPreferencesMapper.selectOne(
                new LambdaQueryWrapper<UserPreferencesEntity>()
                        .eq(UserPreferencesEntity::getUserId, userId)
                        .eq(UserPreferencesEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );
    }

    /**
     * 构建用户信息响应
     * <p>
     * 功能：将用户实体转换为响应对象
     * @param user 用户实体
     * @return UserMeResponse 用户信息响应对象
     */
    private UserMeResponse buildUserMeResponse(UserEntity user) {
        UserMeResponse response = new UserMeResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setNickname(user.getNickname());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setStatus(user.getStatus());
        return response;
    }

    /**
     * 构建用户偏好设置响应
     * <p>
     * 功能：将偏好设置实体转换为响应对象
     * @param preferences 偏好设置实体
     * @return UserPreferencesResponse 偏好设置响应对象
     */
    private UserPreferencesResponse buildUserPreferencesResponse(UserPreferencesEntity preferences) {
        UserPreferencesResponse response = new UserPreferencesResponse();
        response.setTheme(preferences.getTheme());
        response.setLanguage(preferences.getLanguage());
        response.setDefaultHomepage(preferences.getDefaultHomepage());
        response.setDateFormat(preferences.getDateFormat());
        response.setTimeFormat(preferences.getTimeFormat());
        response.setWeekStartDay(preferences.getWeekStartDay());
        response.setNotificationEnabled(preferences.getNotificationEnabled());
        response.setEmailNotificationEnabled(preferences.getEmailNotificationEnabled());
        return response;
    }
    
    /**
     * 生成验证码
     * <p>
     * 功能：生成并返回验证码
     * @return CaptchaResponse 验证码响应对象
     */
    public CaptchaResponse generateCaptcha() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(5);
        String captchaCode = CaptchaUtil.generateCaptchaCode(4);

        // 保存验证码
        AuthCodeEntity authCode = new AuthCodeEntity();
        authCode.setTarget("CAPTCHA");
        authCode.setCode(captchaCode);
        authCode.setType("CAPTCHA");
        authCode.setExpiresAt(expiresAt);
        authCode.setUsed(0);
        authCode.setStatus("ACTIVE");
        authCode.setIsDeleted(0);
        authCode.setCreatedAt(now);
        authCode.setUpdatedAt(now);

        authCodeMapper.insert(authCode);

        // 构建验证码响应
        CaptchaResponse response = new CaptchaResponse();
        response.setCaptchaId(authCode.getId());
        response.setCaptchaCode(captchaCode);
        response.setExpiresAt(expiresAt);

        return response;
    }

    /**
     * 发送注册验证码
     * <p>
     * 功能：发送注册验证码到用户邮箱
     * @param request 发送验证码请求参数
     * @return SendCodeResponse 发送结果
     */
    public SendCodeResponse sendRegisterCode(SendCodeRequest request) {
        // 检查邮箱是否已注册
        Long emailCount = userMapper.selectCount(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getEmail, request.getEmail())
                        .eq(UserEntity::getIsDeleted, 0)
        );

        if (emailCount != null && emailCount > 0) {
            throw new BizException("邮箱已被注册");
        }

        // 生成验证码
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(10);
        String code = CodeUtil.generateNumericCode(6);

        // 保存验证码
        AuthCodeEntity authCode = new AuthCodeEntity();
        authCode.setTarget(request.getEmail());
        authCode.setCode(code);
        authCode.setType("REGISTER");
        authCode.setExpiresAt(expiresAt);
        authCode.setUsed(0);
        authCode.setStatus("ACTIVE");
        authCode.setIsDeleted(0);
        authCode.setCreatedAt(now);
        authCode.setUpdatedAt(now);

        authCodeMapper.insert(authCode);

        // 构建发送结果
        SendCodeResponse response = new SendCodeResponse();
        response.setCodeId(authCode.getId());
        response.setCode(code);
        response.setExpiresAt(expiresAt);

        return response;
    }

    /**
     * 发送找回密码验证码
     * <p>
     * 功能：发送找回密码验证码到用户邮箱
     * @param request 发送验证码请求参数
     * @return SendCodeResponse 发送结果
     */
    public SendCodeResponse sendPasswordResetCode(SendCodeRequest request) {
        // 检查邮箱是否已注册
        UserEntity user = userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getEmail, request.getEmail())
                        .eq(UserEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (user == null) {
            throw new BizException("邮箱未注册");
        }

        // 生成验证码
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(10);
        String code = CodeUtil.generateNumericCode(6);

        // 保存验证码
        AuthCodeEntity authCode = new AuthCodeEntity();
        authCode.setTarget(request.getEmail());
        authCode.setCode(code);
        authCode.setType("PASSWORD_RESET");
        authCode.setExpiresAt(expiresAt);
        authCode.setUsed(0);
        authCode.setStatus("ACTIVE");
        authCode.setIsDeleted(0);
        authCode.setCreatedAt(now);
        authCode.setUpdatedAt(now);

        authCodeMapper.insert(authCode);

        // 构建发送结果
        SendCodeResponse response = new SendCodeResponse();
        response.setCodeId(authCode.getId());
        response.setCode(code);
        response.setExpiresAt(expiresAt);

        return response;
    }

    /**
     * 重置密码
     * <p>
     * 功能：根据验证码重置用户密码
     * @param request 重置密码请求参数
     * @return String 重置结果
     */
    public String resetPassword(PasswordResetRequest request) {
        // 检查邮箱是否已注册
        UserEntity user = userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getEmail, request.getEmail())
                        .eq(UserEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (user == null) {
            throw new BizException("邮箱未注册");
        }

        // 查询验证码
        AuthCodeEntity authCode = authCodeMapper.selectOne(
                new LambdaQueryWrapper<AuthCodeEntity>()
                        .eq(AuthCodeEntity::getTarget, request.getEmail())
                        .eq(AuthCodeEntity::getType, "PASSWORD_RESET")
                        .eq(AuthCodeEntity::getIsDeleted, 0)
                        .orderByDesc(AuthCodeEntity::getCreatedAt)
                        .last("LIMIT 1")
        );

        if (authCode == null) {
            throw new BizException("验证码不存在");
        }

        // 验证验证码
        if (authCode.getUsed() != null && authCode.getUsed() == 1) {
            throw new BizException("验证码已使用");
        }

        if (!"ACTIVE".equals(authCode.getStatus())) {
            throw new BizException("验证码状态不可用");
        }

        if (authCode.getExpiresAt() == null || authCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            authCode.setStatus("EXPIRED");
            authCode.setUpdatedAt(LocalDateTime.now());
            authCodeMapper.updateById(authCode);
            throw new BizException("验证码已过期");
        }

        if (!request.getCode().equals(authCode.getCode())) {
            throw new BizException("验证码错误");
        }

        // 检查新密码是否与旧密码相同
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BizException("新密码不能与旧密码相同");
        }

        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 更新验证码状态
        authCode.setUsed(1);
        authCode.setUsedAt(LocalDateTime.now());
        authCode.setStatus("USED");
        authCode.setUpdatedAt(LocalDateTime.now());
        authCodeMapper.updateById(authCode);

        return "重置密码成功";
    }
}