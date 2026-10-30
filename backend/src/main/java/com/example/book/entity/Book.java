package com.example.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 图书实体 -> 表 t_book
 */
@Data
@TableName("t_book")
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 书名 */
    private String name;

    /** 作者 */
    private String author;

    /** ISBN编号 */
    private String isbn;

    /** 分类ID */
    private Long categoryId;

    /** 价格(元) */
    private BigDecimal price;

    /** 库存 */
    private Integer stock;

    /** 出版日期 */
    private LocalDate publishDate;

    /** 创建时间(数据库默认值填充) */
    private LocalDateTime createTime;

    /** 更新时间(数据库 ON UPDATE 自动刷新) */
    private LocalDateTime updateTime;
}
