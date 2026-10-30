package com.example.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体 -> 表 t_user
 */
@Data
@TableName("t_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号 */
    private String username;

    /** 密码(加盐哈希), 不参与 JSON 序列化, 避免泄露 */
    @JsonIgnore
    private String password;

    /** 真实姓名 / 昵称 */
    private String realName;

    /** 角色: ADMIN 管理员 / USER 普通用户 */
    private String role;

    /** 状态: 1 启用 / 0 禁用 */
    private Integer status;

    /** 创建时间(数据库默认值填充) */
    private LocalDateTime createTime;

    /** 更新时间(数据库自动刷新) */
    private LocalDateTime updateTime;
}
