package com.mobe.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mobe.common.exception.BizException;
import com.mobe.finance.dto.request.FinanceRecordCreateRequest;
import com.mobe.finance.dto.request.FinanceRecordPageRequest;
import com.mobe.finance.dto.response.FinanceRecordResponse;
import com.mobe.finance.entity.FinanceRecordEntity;
import com.mobe.finance.mapper.FinanceRecordMapper;
import com.mobe.user.entity.UserEntity;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

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
     * 构造方法
     * <p>
     * 功能：注入财务记录Mapper
     * @param financeRecordMapper 财务记录Mapper实例
     */
    public FinanceRecordService(FinanceRecordMapper financeRecordMapper) {
        this.financeRecordMapper = financeRecordMapper;
    }

    /**
     * 创建财务记录
     * <p>
     * 功能：创建新的财务记录
     * @param request 创建财务记录请求参数
     * @param session HTTP会话对象
     */
    public void createRecord(FinanceRecordCreateRequest request, HttpSession session) {
        UserEntity loginUser = getLoginUser(session);
        LocalDateTime now = LocalDateTime.now();

        FinanceRecordEntity entity = new FinanceRecordEntity();
        entity.setUserId(loginUser.getId());
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
     * @param request 分页查询请求参数
     * @param session HTTP会话对象
     * @return IPage<FinanceRecordResponse> 分页查询结果
     */
    public IPage<FinanceRecordResponse> pageRecords(FinanceRecordPageRequest request, HttpSession session) {
        UserEntity loginUser = getLoginUser(session);

        Page<FinanceRecordEntity> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<FinanceRecordEntity> wrapper = new LambdaQueryWrapper<FinanceRecordEntity>()
                .eq(FinanceRecordEntity::getUserId, loginUser.getId())
                .eq(FinanceRecordEntity::getIsDeleted, 0)
                .orderByDesc(FinanceRecordEntity::getRecordDate)
                .orderByDesc(FinanceRecordEntity::getCreatedAt);

        if (StringUtils.hasText(request.getType())) {
            wrapper.eq(FinanceRecordEntity::getType, request.getType());
        }

        if (StringUtils.hasText(request.getCategory())) {
            wrapper.eq(FinanceRecordEntity::getCategory, request.getCategory());
        }

        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(FinanceRecordEntity::getRemark, request.getKeyword());
        }

        IPage<FinanceRecordEntity> entityPage = financeRecordMapper.selectPage(page, wrapper);

        return entityPage.convert(entity -> {
            FinanceRecordResponse response = new FinanceRecordResponse();
            BeanUtils.copyProperties(entity, response);
            return response;
        });
    }

    /**
     * 删除财务记录
     * <p>
     * 功能：删除指定的财务记录（软删除）
     * @param id 财务记录ID
     * @param session HTTP会话对象
     */
    public void deleteRecord(String id, HttpSession session) {
        UserEntity loginUser = getLoginUser(session);

        FinanceRecordEntity entity = financeRecordMapper.selectOne(
                new LambdaQueryWrapper<FinanceRecordEntity>()
                        .eq(FinanceRecordEntity::getId, id)
                        .eq(FinanceRecordEntity::getUserId, loginUser.getId())
                        .eq(FinanceRecordEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (entity == null) {
            throw new BizException("流水不存在");
        }

        financeRecordMapper.update(
                null,
                new LambdaUpdateWrapper<FinanceRecordEntity>()
                        .eq(FinanceRecordEntity::getId, id)
                        .set(FinanceRecordEntity::getIsDeleted, 1)
                        .set(FinanceRecordEntity::getUpdatedAt, LocalDateTime.now())
        );
    }

    /**
     * 获取当前登录用户
     * <p>
     * 功能：从会话中获取当前登录用户
     * @param session HTTP会话对象
     * @return UserEntity 当前登录用户实体
     */
    private UserEntity getLoginUser(HttpSession session) {
        Object loginUserObj = session.getAttribute("loginUser");
        if (loginUserObj == null) {
            throw new BizException("用户未登录");
        }
        return (UserEntity) loginUserObj;
    }
}