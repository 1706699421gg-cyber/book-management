package com.example.book.service;

import com.example.book.vo.CategoryCountVO;

import java.util.List;
import java.util.Map;

/**
 * 数据统计 Service
 */
public interface StatsService {

    /**
     * 汇总统计: 图书总数 / 分类总数 / 库存总量
     */
    Map<String, Object> summary();

    /**
     * 按分类统计图书数量(用于柱状图)
     */
    List<CategoryCountVO> categoryCount();
}
