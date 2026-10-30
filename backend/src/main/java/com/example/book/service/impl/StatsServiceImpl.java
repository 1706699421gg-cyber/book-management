package com.example.book.service.impl;

import com.example.book.mapper.BookMapper;
import com.example.book.mapper.CategoryMapper;
import com.example.book.service.StatsService;
import com.example.book.vo.CategoryCountVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据统计 Service 实现
 */
@Service
public class StatsServiceImpl implements StatsService {

    @Resource
    private BookMapper bookMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public Map<String, Object> summary() {
        Map<String, Object> data = new HashMap<>();
        // 图书总数
        data.put("bookTotal", bookMapper.selectCount(null));
        // 分类总数
        data.put("categoryTotal", categoryMapper.selectCount(null));
        // 库存总量
        Long stockTotal = bookMapper.sumStock();
        data.put("stockTotal", stockTotal == null ? 0L : stockTotal);
        return data;
    }

    @Override
    public List<CategoryCountVO> categoryCount() {
        return bookMapper.selectCategoryCount();
    }
}
