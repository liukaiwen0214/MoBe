package com.mobe.checklist.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mobe.checklist.dto.request.ChecklistCreateRequest;
import com.mobe.checklist.dto.request.ChecklistPageRequest;
import com.mobe.checklist.dto.request.ChecklistUpdateRequest;
import com.mobe.checklist.dto.response.ChecklistItemResponse;
import com.mobe.checklist.entity.ChecklistItemEntity;
import com.mobe.checklist.entity.ChecklistInstanceEntity;
import com.mobe.checklist.mapper.ChecklistInstanceMapper;
import com.mobe.checklist.mapper.ChecklistItemMapper;
import com.mobe.common.exception.BizException;
import com.mobe.finance.dto.response.PageResponse;
import com.mobe.user.dto.UserMeResponse;
import com.mobe.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 清单服务类
 */
@Service
public class ChecklistService {

    private final ChecklistItemMapper checklistItemMapper;
    private final ChecklistInstanceMapper checklistInstanceMapper;
    private final UserService userService;

    public ChecklistService(ChecklistItemMapper checklistItemMapper,
                            ChecklistInstanceMapper checklistInstanceMapper,
                            UserService userService) {
        this.checklistItemMapper = checklistItemMapper;
        this.checklistInstanceMapper = checklistInstanceMapper;
        this.userService = userService;
    }

    /**
     * 新增普通清单
     */
    public void createChecklist(ChecklistCreateRequest request, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        ChecklistItemEntity itemEntity = new ChecklistItemEntity();
        itemEntity.setUserId(currentUser.getId());
        itemEntity.setTaskId(request.getTaskId());
        itemEntity.setTaskName(request.getTaskName());
        itemEntity.setTitle(request.getTitle());
        itemEntity.setDescription(request.getDescription());
        itemEntity.setPriority(normalizePriority(request.getPriority()));
        itemEntity.setReminderText(request.getReminderText());
        itemEntity.setActionText(request.getActionText());
        itemEntity.setActionType(request.getActionType());
        itemEntity.setStatus("PENDING");
        itemEntity.setSort(request.getSort() == null ? 0 : request.getSort());
        itemEntity.setCompletedAt(null);

        checklistItemMapper.insert(itemEntity);

        ChecklistInstanceEntity instanceEntity = new ChecklistInstanceEntity();
        instanceEntity.setUserId(currentUser.getId());
        instanceEntity.setSourceType("CHECKLIST");
        instanceEntity.setSourceId(itemEntity.getId());
        instanceEntity.setTaskId(request.getTaskId());
        instanceEntity.setTaskName(request.getTaskName());
        instanceEntity.setTitle(request.getTitle());
        instanceEntity.setDescription(request.getDescription());
        instanceEntity.setPriority(normalizePriority(request.getPriority()));
        instanceEntity.setFrequency("ONCE");
        instanceEntity.setReminderText(request.getReminderText());
        instanceEntity.setActionText(request.getActionText());
        instanceEntity.setActionType(request.getActionType());
        instanceEntity.setStatus("PENDING");
        instanceEntity.setInstanceDate(LocalDate.now());
        instanceEntity.setSort(request.getSort() == null ? 0 : request.getSort());
        instanceEntity.setCompletedAt(null);

        checklistInstanceMapper.insert(instanceEntity);
    }

    /**
     * 分页查询清单
     */
    public PageResponse<ChecklistItemResponse> pageChecklists(ChecklistPageRequest request,
                                                              HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        long pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        long pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();

        Page<ChecklistInstanceEntity> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<ChecklistInstanceEntity> wrapper = new LambdaQueryWrapper<ChecklistInstanceEntity>()
                .eq(ChecklistInstanceEntity::getUserId, currentUser.getId())
                .eq(ChecklistInstanceEntity::getIsDeleted, 0)
                .orderByAsc(ChecklistInstanceEntity::getStatus)
                .orderByDesc(ChecklistInstanceEntity::getPriority)
                .orderByAsc(ChecklistInstanceEntity::getSort)
                .orderByDesc(ChecklistInstanceEntity::getCreatedAt);

        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(q -> q
                    .like(ChecklistInstanceEntity::getTaskName, request.getKeyword())
                    .or()
                    .like(ChecklistInstanceEntity::getTitle, request.getKeyword())
                    .or()
                    .like(ChecklistInstanceEntity::getDescription, request.getKeyword())
                    .or()
                    .like(ChecklistInstanceEntity::getReminderText, request.getKeyword())
                    .or()
                    .like(ChecklistInstanceEntity::getActionText, request.getKeyword()));
        }

        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(ChecklistInstanceEntity::getStatus, request.getStatus());
        }

        if (StringUtils.hasText(request.getPriority())) {
            wrapper.eq(ChecklistInstanceEntity::getPriority, request.getPriority());
        }

        if (StringUtils.hasText(request.getFrequency())) {
            wrapper.eq(ChecklistInstanceEntity::getFrequency, request.getFrequency());
        }

        if (request.getReminderOnly() != null && request.getReminderOnly() == 1) {
            wrapper.isNotNull(ChecklistInstanceEntity::getReminderText)
                    .ne(ChecklistInstanceEntity::getReminderText, "");
        }

        if (request.getActionOnly() != null && request.getActionOnly() == 1) {
            wrapper.isNotNull(ChecklistInstanceEntity::getActionText)
                    .ne(ChecklistInstanceEntity::getActionText, "");
        }

        Page<ChecklistInstanceEntity> entityPage = checklistInstanceMapper.selectPage(page, wrapper);

        List<ChecklistItemResponse> records = entityPage.getRecords().stream().map(entity -> {
            ChecklistItemResponse response = new ChecklistItemResponse();
            BeanUtils.copyProperties(entity, response);
            return response;
        }).toList();

        PageResponse<ChecklistItemResponse> response = new PageResponse<>();
        response.setTotal(entityPage.getTotal());
        response.setPageNum(entityPage.getCurrent());
        response.setPageSize(entityPage.getSize());
        response.setRecords(records);
        return response;
    }

    /**
     * 更新普通清单
     */
    public void updateChecklist(ChecklistUpdateRequest request, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        ChecklistItemEntity itemEntity = checklistItemMapper.selectOne(
                new LambdaQueryWrapper<ChecklistItemEntity>()
                        .eq(ChecklistItemEntity::getId, request.getId())
                        .eq(ChecklistItemEntity::getUserId, currentUser.getId())
                        .eq(ChecklistItemEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (itemEntity == null) {
            throw new BizException("清单不存在");
        }

        itemEntity.setTaskId(request.getTaskId());
        itemEntity.setTaskName(request.getTaskName());
        itemEntity.setTitle(request.getTitle());
        itemEntity.setDescription(request.getDescription());
        itemEntity.setPriority(normalizePriority(request.getPriority()));
        itemEntity.setReminderText(request.getReminderText());
        itemEntity.setActionText(request.getActionText());
        itemEntity.setActionType(request.getActionType());
        itemEntity.setSort(request.getSort() == null ? 0 : request.getSort());

        checklistItemMapper.updateById(itemEntity);

        ChecklistInstanceEntity instanceEntity = checklistInstanceMapper.selectOne(
                new LambdaQueryWrapper<ChecklistInstanceEntity>()
                        .eq(ChecklistInstanceEntity::getSourceType, "CHECKLIST")
                        .eq(ChecklistInstanceEntity::getSourceId, request.getId())
                        .eq(ChecklistInstanceEntity::getUserId, currentUser.getId())
                        .eq(ChecklistInstanceEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (instanceEntity != null) {
            instanceEntity.setTaskId(request.getTaskId());
            instanceEntity.setTaskName(request.getTaskName());
            instanceEntity.setTitle(request.getTitle());
            instanceEntity.setDescription(request.getDescription());
            instanceEntity.setPriority(normalizePriority(request.getPriority()));
            instanceEntity.setReminderText(request.getReminderText());
            instanceEntity.setActionText(request.getActionText());
            instanceEntity.setActionType(request.getActionType());
            instanceEntity.setSort(request.getSort() == null ? 0 : request.getSort());

            checklistInstanceMapper.updateById(instanceEntity);
        }
    }

    /**
     * 完成清单
     */
    public void completeChecklist(String id, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);
        LocalDateTime now = LocalDateTime.now();

        ChecklistInstanceEntity instanceEntity = checklistInstanceMapper.selectOne(
                new LambdaQueryWrapper<ChecklistInstanceEntity>()
                        .eq(ChecklistInstanceEntity::getId, id)
                        .eq(ChecklistInstanceEntity::getUserId, currentUser.getId())
                        .eq(ChecklistInstanceEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (instanceEntity == null) {
            throw new BizException("清单不存在");
        }

        instanceEntity.setStatus("DONE");
        instanceEntity.setCompletedAt(now);
        checklistInstanceMapper.updateById(instanceEntity);

        if ("CHECKLIST".equals(instanceEntity.getSourceType())) {
            ChecklistItemEntity itemEntity = checklistItemMapper.selectOne(
                    new LambdaQueryWrapper<ChecklistItemEntity>()
                            .eq(ChecklistItemEntity::getId, instanceEntity.getSourceId())
                            .eq(ChecklistItemEntity::getUserId, currentUser.getId())
                            .eq(ChecklistItemEntity::getIsDeleted, 0)
                            .last("LIMIT 1")
            );

            if (itemEntity != null) {
                itemEntity.setStatus("DONE");
                itemEntity.setCompletedAt(now);
                checklistItemMapper.updateById(itemEntity);
            }
        }
    }

    /**
     * 恢复为未完成
     */
    public void restoreChecklist(String id, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        ChecklistInstanceEntity instanceEntity = checklistInstanceMapper.selectOne(
                new LambdaQueryWrapper<ChecklistInstanceEntity>()
                        .eq(ChecklistInstanceEntity::getId, id)
                        .eq(ChecklistInstanceEntity::getUserId, currentUser.getId())
                        .eq(ChecklistInstanceEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (instanceEntity == null) {
            throw new BizException("清单不存在");
        }

        instanceEntity.setStatus("PENDING");
        instanceEntity.setCompletedAt(null);
        checklistInstanceMapper.updateById(instanceEntity);

        if ("CHECKLIST".equals(instanceEntity.getSourceType())) {
            ChecklistItemEntity itemEntity = checklistItemMapper.selectOne(
                    new LambdaQueryWrapper<ChecklistItemEntity>()
                            .eq(ChecklistItemEntity::getId, instanceEntity.getSourceId())
                            .eq(ChecklistItemEntity::getUserId, currentUser.getId())
                            .eq(ChecklistItemEntity::getIsDeleted, 0)
                            .last("LIMIT 1")
            );

            if (itemEntity != null) {
                itemEntity.setStatus("PENDING");
                itemEntity.setCompletedAt(null);
                checklistItemMapper.updateById(itemEntity);
            }
        }
    }

    /**
     * 删除清单
     */
    public void deleteChecklist(String id, HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = getLoginUserEntity(httpServletRequest);

        ChecklistInstanceEntity instanceEntity = checklistInstanceMapper.selectOne(
                new LambdaQueryWrapper<ChecklistInstanceEntity>()
                        .eq(ChecklistInstanceEntity::getId, id)
                        .eq(ChecklistInstanceEntity::getUserId, currentUser.getId())
                        .eq(ChecklistInstanceEntity::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        if (instanceEntity == null) {
            throw new BizException("清单不存在");
        }

        instanceEntity.setIsDeleted(1);
        checklistInstanceMapper.updateById(instanceEntity);

        if ("CHECKLIST".equals(instanceEntity.getSourceType())) {
            ChecklistItemEntity itemEntity = checklistItemMapper.selectOne(
                    new LambdaQueryWrapper<ChecklistItemEntity>()
                            .eq(ChecklistItemEntity::getId, instanceEntity.getSourceId())
                            .eq(ChecklistItemEntity::getUserId, currentUser.getId())
                            .eq(ChecklistItemEntity::getIsDeleted, 0)
                            .last("LIMIT 1")
            );

            if (itemEntity != null) {
                itemEntity.setIsDeleted(1);
                checklistItemMapper.updateById(itemEntity);
            }
        }
    }

    private String normalizePriority(String priority) {
        if (!StringUtils.hasText(priority)) {
            return "MEDIUM";
        }

        return switch (priority.toUpperCase()) {
            case "HIGH" -> "HIGH";
            case "LOW" -> "LOW";
            default -> "MEDIUM";
        };
    }

    private UserMeResponse getLoginUserEntity(HttpServletRequest httpServletRequest) {
        UserMeResponse currentUser = userService.getCurrentUser(httpServletRequest);
        if (currentUser == null || !StringUtils.hasText(currentUser.getId())) {
            throw new BizException("用户未登录");
        }
        return currentUser;
    }
}