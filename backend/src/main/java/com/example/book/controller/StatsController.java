package com.example.book.controller;

import com.example.book.auth.PermissionCodes;
import com.example.book.auth.RequirePermission;
import com.example.book.common.Result;
import com.example.book.service.StatsService;
import com.example.book.vo.CategoryCountVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 数据统计接口(首页仪表盘)
 */
@RestController
@RequestMapping("/api/stats")
@RequirePermission(PermissionCodes.STATS_VIEW)
public class StatsController {

    @Resource
    private StatsService statsService;

    /**
     * 汇总: 图书总数 / 分类总数 / 库存总量
     * GET /api/stats/summary
     */
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        return Result.success(statsService.summary());
    }

    /**
     * 按分类统计图书数量(柱状图)
     * GET /api/stats/category-count
     */
    @GetMapping("/category-count")
    public Result<List<CategoryCountVO>> categoryCount() {
        return Result.success(statsService.categoryCount());
    }
}
