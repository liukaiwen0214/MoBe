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
@Slf4j
@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserSessionMapper userSessionMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserPreferencesMapper userPreferencesMapper;
    private final AuthCodeMapper authCodeMapper;

    public UserService(UserMapper userMapper,
                       UserSessionMapper userSessionMapper,
                       PasswordEncoder passwordEncoder, UserPreferencesMapper userPreferencesMapper, AuthCodeMapper authCodeMapper) {
        this.userMapper = userMapper;
        this.userSessionMapper = userSessionMapper;
        this.passwordEncoder = passwordEncoder;
        this.userPreferencesMapper = userPreferencesMapper;
        this.authCodeMapper = authCodeMapper;
    }

    public String register(UserRegisterRequest request) {
        Long usernameCount = userMapper.selectCount(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getUsername, request.getUsername())
                        .eq(UserEntity::getIsDeleted, 0)
        );

        if (usernameCount != null && usernameCount > 0) {
            return "用户名已存在";
        }

        Long emailCount = userMapper.selectCount(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getEmail, request.getEmail())
                        .eq(UserEntity::getIsDeleted, 0)
        );

        if (emailCount != null && emailCount > 0) {
            return "邮箱已存在";
        }

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

    public UserLoginResponse login(UserLoginRequest request, HttpServletRequest httpServletRequest) {
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

    public UserMeResponse getCurrentUser(HttpServletRequest request) {
        UserEntity user = getCurrentUserEntityByRequest(request);

        UserSessionEntity session = getCurrentActiveSessionByRequest(request);
        session.setLastAccessedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        userSessionMapper.updateById(session);

        return buildUserMeResponse(user);
    }

    public String logout(HttpServletRequest request) {
        UserSessionEntity session = getValidSessionByRequest(request);

        session.setStatus("LOGOUT");
        session.setUpdatedAt(LocalDateTime.now());
        userSessionMapper.updateById(session);

        return "退出登录成功";
    }

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

    public String updatePassword(UserPasswordUpdateRequest request, HttpServletRequest httpServletRequest) {
        UserEntity user = getCurrentUserEntityByRequest(httpServletRequest);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BizException("旧密码错误");
        }

        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BizException("新密码不能与旧密码相同");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
        return "修改密码成功";
    }

    public UserMeResponse getUserById(String userId) {
        UserEntity user = getUserByIdOrThrow(userId);
        return buildUserMeResponse(user);
    }

    public List<UserSessionResponse> getSessionList(HttpServletRequest request) {
        UserSessionEntity currentSession = getCurrentActiveSessionByRequest(request);

        List<UserSessionEntity> sessions = userSessionMapper.selectList(
                new LambdaQueryWrapper<UserSessionEntity>()
                        .eq(UserSessionEntity::getUserId, currentSession.getUserId())
                        .eq(UserSessionEntity::getIsDeleted, 0)
                        .orderByDesc(UserSessionEntity::getCreatedAt)
        );

        List<UserSessionResponse> result = new ArrayList<>();
        for (UserSessionEntity session : sessions) {
            result.add(buildSessionResponse(session, currentSession));
        }
        return result;
    }

    public String updateProfile(UserProfileUpdateRequest request, HttpServletRequest httpServletRequest) {
        UserEntity user = getCurrentUserEntityByRequest(httpServletRequest);

        user.setNickname(request.getNickname());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
        return "更新个人资料成功";
    }

    public String deleteSession(String sessionId, HttpServletRequest request) {
        UserSessionEntity currentSession = getCurrentActiveSessionByRequest(request);

        UserSessionEntity targetSession = userSessionMapper.selectOne(
                new LambdaQueryWrapper<UserSessionEntity>()
                        .eq(UserSessionEntity::getId, sessionId)
                        .eq(UserSessionEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (targetSession == null) {
            throw new BizException("会话不存在");
        }

        if (!currentSession.getUserId().equals(targetSession.getUserId())) {
            throw new BizException("无权操作该会话");
        }

        targetSession.setStatus("LOGOUT");
        targetSession.setUpdatedAt(LocalDateTime.now());
        userSessionMapper.updateById(targetSession);

        return "会话已下线";
    }

    private UserEntity getCurrentUserEntityByRequest(HttpServletRequest request) {
        UserSessionEntity session = getCurrentActiveSessionByRequest(request);
        return getUserByIdOrThrow(session.getUserId());
    }

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

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr();
    }

    public UserPreferencesResponse getMyPreferences(HttpServletRequest request) {
        UserEntity user = getCurrentUserEntityByRequest(request);

        UserPreferencesEntity preferences = getUserPreferencesByUserId(user.getId());

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

    public String updateMyPreferences(UserPreferencesUpdateRequest request, HttpServletRequest httpServletRequest) {
        UserEntity user = getCurrentUserEntityByRequest(httpServletRequest);

        UserPreferencesEntity preferences = getUserPreferencesByUserId(user.getId());

        if (preferences == null) {
            preferences = new UserPreferencesEntity();
            preferences.setUserId(user.getId());
            preferences.setIsDeleted(0);
            preferences.setCreatedAt(LocalDateTime.now());
        }

        applyPreferencesUpdate(preferences, request);
        preferences.setUpdatedAt(LocalDateTime.now());

        if (preferences.getId() == null) {
            userPreferencesMapper.insert(preferences);
        } else {
            userPreferencesMapper.updateById(preferences);
        }

        return "更新用户偏好成功";
    }

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

    private UserSessionEntity getCurrentActiveSessionByRequest(HttpServletRequest request) {
        UserSessionEntity session = getValidSessionByRequest(request);
        validateSessionStatus(session);
        return session;
    }

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

    private UserPreferencesEntity getUserPreferencesByUserId(String userId) {
        return userPreferencesMapper.selectOne(
                new LambdaQueryWrapper<UserPreferencesEntity>()
                        .eq(UserPreferencesEntity::getUserId, userId)
                        .eq(UserPreferencesEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );
    }

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
    public CaptchaResponse generateCaptcha() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(5);
        String captchaCode = CaptchaUtil.generateCaptchaCode(4);

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

        CaptchaResponse response = new CaptchaResponse();
        response.setCaptchaId(authCode.getId());
        response.setCaptchaCode(captchaCode);
        response.setExpiresAt(expiresAt);

        return response;
    }

    public SendCodeResponse sendRegisterCode(SendCodeRequest request) {
        Long emailCount = userMapper.selectCount(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getEmail, request.getEmail())
                        .eq(UserEntity::getIsDeleted, 0)
        );

        if (emailCount != null && emailCount > 0) {
            throw new BizException("邮箱已被注册");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(10);
        String code = CodeUtil.generateNumericCode(6);

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

        SendCodeResponse response = new SendCodeResponse();
        response.setCodeId(authCode.getId());
        response.setCode(code);
        response.setExpiresAt(expiresAt);

        return response;
    }

    public SendCodeResponse sendPasswordResetCode(SendCodeRequest request) {
        UserEntity user = userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getEmail, request.getEmail())
                        .eq(UserEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (user == null) {
            throw new BizException("邮箱未注册");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(10);
        String code = CodeUtil.generateNumericCode(6);

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

        SendCodeResponse response = new SendCodeResponse();
        response.setCodeId(authCode.getId());
        response.setCode(code);
        response.setExpiresAt(expiresAt);

        return response;
    }

    public String resetPassword(PasswordResetRequest request) {
        UserEntity user = userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getEmail, request.getEmail())
                        .eq(UserEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (user == null) {
            throw new BizException("邮箱未注册");
        }

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

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BizException("新密码不能与旧密码相同");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        authCode.setUsed(1);
        authCode.setUsedAt(LocalDateTime.now());
        authCode.setStatus("USED");
        authCode.setUpdatedAt(LocalDateTime.now());
        authCodeMapper.updateById(authCode);

        return "重置密码成功";
    }
}