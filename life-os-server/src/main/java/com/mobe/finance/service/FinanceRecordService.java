package com.mobe.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mobe.common.exception.BizException;
import com.mobe.finance.dto.request.FinanceRecordBatchCreateRequest;
import com.mobe.finance.dto.request.FinanceRecordCreateRequest;
import com.mobe.finance.dto.request.FinanceRecordPageRequest;
import com.mobe.finance.dto.request.FinanceRecordUpdateRequest;
import com.mobe.finance.dto.response.FinanceRecordResponse;
import com.mobe.finance.dto.response.PageResponse;
import com.mobe.finance.entity.FinanceRecordEntity;
import com.mobe.finance.mapper.FinanceRecordMapper;
import com.mobe.user.dto.UserMeResponse;
import com.mobe.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * 财务记录服务类
 * <p>
 * 功能：提供财务记录相关的业务逻辑，包括创建、查询、删除财务记录
 * 说明：使用 @Service 注解标记为服务类
 */
@Service
public class FinanceRecordService {

    /**
     * 财务记录Mapper
     */
    private final FinanceRecordMapper financeRecordMapper;

    /**
     * 用户服务
     */
    private final UserService userService;

    /**
     * 构造方法
     * <p>
     * 功能：注入财务记录Mapper、用户会话Mapper、用户服务实例
     * 
     * @param financeRecordMapper 财务记录Mapper实例
     */
    public FinanceRecordService(FinanceRecordMapper financeRecordMapper, UserService userService) {
        this.financeRecordMapper = financeRecordMapper;
        this.userService = userService;
    }

    /**
     * 创建财务记录
     * <p>
     * 功能：创建新的财务记录
     * 
     * @param request            创建财务记录请求参数
     * @param httpServletRequest HttpServletRequest HTTP请求对象
     */
    public void createRecord(FinanceRecordCreateRequest request, HttpServletRequest httpServletRequest) {
        UserMeResponse userMeResponse = getLoginUser(httpServletRequest);
        LocalDateTime now = LocalDateTime.now();

        FinanceRecordEntity entity = new FinanceRecordEntity();
        entity.setUserId(userMeResponse.getId());
        entity.setType(request.getType());
        entity.setCategory(request.getCategory());
        entity.setAmount(request.getAmount());
        entity.setRecordDate(request.getRecordDate());
        entity.setRemark(request.getRemark());
        entity.setIsDeleted(0);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        financeRecordMapper.insert(entity);
    }

    /**
     * 分页查询财务记录
     * <p>
     * 功能：根据条件分页查询财务记录
     * 
     * @param request 分页查询请求参数
     * @param session HTTP会话对象
     * @return IPage<FinanceRecordResponse> 分页查询结果
     */
    public PageResponse<FinanceRecordResponse> pageRecords(FinanceRecordPageRequest request,
            HttpServletRequest httpServletRequest) {
        UserMeResponse userMeResponse = getLoginUser(httpServletRequest);

        long pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        long pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();

        Page<FinanceRecordEntity> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<FinanceRecordEntity> wrapper = new LambdaQueryWrapper<FinanceRecordEntity>()
                .eq(FinanceRecordEntity::getUserId, userMeResponse.getId())
                .eq(FinanceRecordEntity::getIsDeleted, 0)
                .orderByDesc(FinanceRecordEntity::getRecordDate)
                .orderByDesc(FinanceRecordEntity::getCreatedAt);

        // 类型筛选
        if (StringUtils.hasText(request.getType())) {
            wrapper.eq(FinanceRecordEntity::getType, request.getType());
        }

        // 分类精确筛选
        if (StringUtils.hasText(request.getCategory())) {
            wrapper.eq(FinanceRecordEntity::getCategory, request.getCategory());
        }

        // 时间筛选
        applyDateFilter(wrapper, request);

        // 模糊搜索
        applyKeywordFilter(wrapper, request.getKeyword());

        IPage<FinanceRecordEntity> entityPage = financeRecordMapper.selectPage(page, wrapper);

        PageResponse<FinanceRecordResponse> response = new PageResponse<>();
        response.setTotal(entityPage.getTotal());
        response.setPageNum(entityPage.getCurrent());
        response.setPageSize(entityPage.getSize());
        response.setRecords(
                entityPage.getRecords().stream().map(entity -> {
                    FinanceRecordResponse item = new FinanceRecordResponse();
                    BeanUtils.copyProperties(entity, item);
                    return item;
                }).toList());

        return response;
    }

    /**
     * 删除财务记录
     * <p>
     * 功能：删除指定的财务记录（软删除）
     * 
     * @param id      财务记录ID
     * @param session HTTP会话对象
     */
    public void deleteRecord(String id, HttpServletRequest httpServletRequest) {
        UserMeResponse userMeResponse = getLoginUser(httpServletRequest);
        FinanceRecordEntity entity = financeRecordMapper.selectOne(
                new LambdaQueryWrapper<FinanceRecordEntity>()
                        .eq(FinanceRecordEntity::getId, id)
                        .eq(FinanceRecordEntity::getUserId, userMeResponse.getId())
                        .eq(FinanceRecordEntity::getIsDeleted, 0)
                        .last("LIMIT 1"));

        if (entity == null) {
            throw new BizException("流水不存在");
        }

        financeRecordMapper.update(
                null,
                new LambdaUpdateWrapper<FinanceRecordEntity>()
                        .eq(FinanceRecordEntity::getId, id)
                        .set(FinanceRecordEntity::getIsDeleted, 1)
                        .set(FinanceRecordEntity::getUpdatedAt, LocalDateTime.now()));
    }

    public void updateRecord(String id, FinanceRecordUpdateRequest request, HttpServletRequest httpServletRequest) {
        UserMeResponse userMeResponse = getLoginUser(httpServletRequest);
        FinanceRecordEntity entity = financeRecordMapper.selectOne(
                new LambdaQueryWrapper<FinanceRecordEntity>()
                        .eq(FinanceRecordEntity::getId, id)
                        .eq(FinanceRecordEntity::getUserId, userMeResponse.getId())
                        .eq(FinanceRecordEntity::getIsDeleted, 0)
                        .last("LIMIT 1"));

        if (entity == null) {
            throw new BizException("流水不存在");
        }

        FinanceRecordEntity updateEntity = new FinanceRecordEntity();
        updateEntity.setType(request.getType());
        updateEntity.setCategory(request.getCategory());
        updateEntity.setAmount(request.getAmount());
        updateEntity.setRecordDate(request.getRecordDate());
        updateEntity.setRemark(request.getRemark());
        updateEntity.setUpdatedAt(LocalDateTime.now());

        financeRecordMapper.update(
                updateEntity,
                new LambdaUpdateWrapper<FinanceRecordEntity>()
                        .eq(FinanceRecordEntity::getId, id)
                        .eq(FinanceRecordEntity::getUserId, userMeResponse.getId())
                        .eq(FinanceRecordEntity::getIsDeleted, 0));
    }

    /**
     * 获取当前登录用户
     * <p>
     * 功能：从会话中获取当前登录用户
     * 
     * @param session HTTP会话对象
     * @return UserEntity 当前登录用户实体
     */
    private UserMeResponse getLoginUser(HttpServletRequest request) {
        return userService.getCurrentUser(request);
    }

    public UserMeResponse getLoginUserEntity(HttpServletRequest request) {
    
        UserMeResponse currentUser = getLoginUser(request);

        UserMeResponse user = userService.getUserById(currentUser.getId());
        if (user == null) {
            throw new BizException("用户不存在");
        }

        return user;
    }

    /**
     * 应用时间筛选
     * <p>
     * 功能：根据请求参数应用时间筛选条件
     * 
     * @param wrapper 查询包装器
     * @param request 分页查询请求参数
     */
    private void applyDateFilter(LambdaQueryWrapper<FinanceRecordEntity> wrapper, FinanceRecordPageRequest request) {
        String dateMode = request.getDateMode();

        if (!StringUtils.hasText(dateMode) || "NONE".equalsIgnoreCase(dateMode)) {
            if (StringUtils.hasText(request.getStartDate())) {
                wrapper.ge(FinanceRecordEntity::getRecordDate, request.getStartDate());
            }
            if (StringUtils.hasText(request.getEndDate())) {
                wrapper.le(FinanceRecordEntity::getRecordDate, request.getEndDate());
            }
            return;
        }

        LocalDateTime start = null;
        LocalDateTime end = null;

        switch (dateMode.toUpperCase()) {
            case "QUICK":
                String quickDate = request.getQuickDate();

                if (!StringUtils.hasText(quickDate)) {
                    break;
                }

                switch (quickDate.toUpperCase()) {
                    case "TODAY":
                        start = LocalDate.now().atStartOfDay();
                        end = LocalDate.now().atTime(LocalTime.MAX);
                        break;
                    case "THIS_WEEK":
                        LocalDate today = LocalDate.now();
                        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
                        LocalDate weekEnd = today.with(java.time.DayOfWeek.SUNDAY);
                        start = weekStart.atStartOfDay();
                        end = weekEnd.atTime(LocalTime.MAX);
                        break;
                    case "THIS_MONTH":
                        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
                        LocalDate lastDayOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
                        start = firstDayOfMonth.atStartOfDay();
                        end = lastDayOfMonth.atTime(LocalTime.MAX);
                        break;
                    case "LAST_30_DAYS":
                        start = LocalDate.now().minusDays(29).atStartOfDay();
                        end = LocalDate.now().atTime(LocalTime.MAX);
                        break;
                    default:
                        break;
                }
                break;

            case "RANGE":
                if (StringUtils.hasText(request.getStartDate())) {
                    start = LocalDateTime.parse(request.getStartDate(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
                if (StringUtils.hasText(request.getEndDate())) {
                    end = LocalDateTime.parse(request.getEndDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
                break;

            case "MONTH":
                if (StringUtils.hasText(request.getMonth())) {
                    YearMonth yearMonth = YearMonth.parse(request.getMonth(), DateTimeFormatter.ofPattern("yyyy-MM"));
                    start = yearMonth.atDay(1).atStartOfDay();
                    end = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);
                }
                break;

            default:
                break;
        }

        if (start != null) {
            wrapper.ge(FinanceRecordEntity::getRecordDate, start);
        }
        if (end != null) {
            wrapper.le(FinanceRecordEntity::getRecordDate, end);
        }
    }

    /**
     * 应用模糊搜索
     * <p>
     * 功能：根据请求参数应用模糊搜索条件
     * 
     * @param wrapper 查询包装器
     * @param keyword 搜索关键词
     */
    private void applyKeywordFilter(LambdaQueryWrapper<FinanceRecordEntity> wrapper, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }

        wrapper.and(q -> q
                .like(FinanceRecordEntity::getRemark, keyword)
                .or()
                .like(FinanceRecordEntity::getCategory, keyword)
                .or()
                .like(FinanceRecordEntity::getType, keyword));
    }
    /**
     * 批量创建财务记录
     * <p>
     * 功能：批量创建新的财务记录
     * 
     * @param request 批量创建财务记录请求参数
     * @param httpServletRequest HTTP会话对象
     */
    public void batchCreateRecords(FinanceRecordBatchCreateRequest request, HttpServletRequest httpServletRequest) {
    UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);
    LocalDateTime now = LocalDateTime.now();

    for (FinanceRecordCreateRequest item : request.getRecords()) {
        FinanceRecordEntity entity = new FinanceRecordEntity();
        entity.setUserId(currentUser.getId());
        entity.setType(item.getType());
        entity.setCategory(item.getCategory());
        entity.setAmount(item.getAmount());
        entity.setRecordDate(item.getRecordDate());
        entity.setRemark(item.getRemark());
        entity.setIsDeleted(0);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        financeRecordMapper.insert(entity);
    }
}
}