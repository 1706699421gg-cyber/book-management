package com.example.book.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 按分类统计图书数量 VO
 */
@Data
public class CategoryCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称 */
    private String name;

    /** 该分类下图书数量 */
    private Long total;
}
