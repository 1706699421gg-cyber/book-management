package com.example.book.controller;

import com.example.book.auth.PermissionCodes;
import com.example.book.auth.RequirePermission;
import com.example.book.common.Result;
import com.example.book.entity.Category;
import com.example.book.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 图书分类管理接口
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    /**
     * 分类列表(图书表单下拉框亦使用此接口)
     * GET /api/category/list
     */
    @GetMapping("/list")
    @RequirePermission(PermissionCodes.CATEGORY_VIEW)
    public Result<List<Category>> list() {
        return Result.success(categoryService.list());
    }

    /**
     * 新增分类
     * POST /api/category
     */
    @PostMapping
    @RequirePermission(PermissionCodes.CATEGORY_ADD)
    public Result<Void> add(@RequestBody Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            return Result.error("分类名称不能为空");
        }
        boolean ok = categoryService.save(category);
        return ok ? Result.success("新增成功", null) : Result.error("新增失败");
    }

    /**
     * 编辑分类
     * PUT /api/category
     */
    @PutMapping
    @RequirePermission(PermissionCodes.CATEGORY_EDIT)
    public Result<Void> update(@RequestBody Category category) {
        if (category.getId() == null) {
            return Result.error("分类ID不能为空");
        }
        boolean ok = categoryService.updateById(category);
        return ok ? Result.success("修改成功", null) : Result.error("修改失败, 分类可能不存在");
    }

    /**
     * 删除分类(分类下存在图书时后端拦截)
     * DELETE /api/category/{id}
     */
    @DeleteMapping("/{id}")
    @RequirePermission(PermissionCodes.CATEGORY_DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.removeCategory(id);
        return Result.success("删除成功", null);
    }
}
