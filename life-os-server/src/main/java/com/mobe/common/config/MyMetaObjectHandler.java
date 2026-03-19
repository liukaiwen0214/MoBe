package com.mobe.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 自定义元对象处理器
 * <p>
 * 功能：实现 MyBatis Plus 的 MetaObjectHandler 接口，用于自动填充实体类的公共字段
 * 说明：在插入和更新操作时，自动填充创建时间、更新时间和删除状态字段
 */
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作时的字段填充
     * <p>
     * 功能：在插入新记录时，自动填充创建时间、更新时间和删除状态字段
     * @param metaObject 元对象，包含实体类的属性信息
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        // 填充创建时间
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        // 填充更新时间
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
        // 填充删除状态，默认为 0（未删除）
        this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
    }

    /**
     * 更新操作时的字段填充
     * <p>
     * 功能：在更新记录时，自动填充更新时间字段
     * @param metaObject 元对象，包含实体类的属性信息
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 填充更新时间
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}