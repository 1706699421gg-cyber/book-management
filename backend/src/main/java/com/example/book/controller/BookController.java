package com.example.book.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.book.auth.PermissionCodes;
import com.example.book.auth.RequirePermission;
import com.example.book.common.Result;
import com.example.book.entity.Book;
import com.example.book.service.BookService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 图书管理接口
 */
@RestController
@RequestMapping("/api/book")
public class BookController {

    @Resource
    private BookService bookService;

    /**
     * 分页查询 + 关键字搜索(书名/作者模糊匹配)
     * GET /api/book/page?current=1&size=10&keyword=三体
     */
    @GetMapping("/page")
    @RequirePermission(PermissionCodes.BOOK_VIEW)
    public Result<Page<Book>> page(@RequestParam(defaultValue = "1") long current,
                                   @RequestParam(defaultValue = "10") long size,
                                   @RequestParam(required = false) String keyword) {
        return Result.success(bookService.pageBooks(current, size, keyword));
    }

    /**
     * 新增图书
     * POST /api/book
     */
    @PostMapping
    @RequirePermission(PermissionCodes.BOOK_ADD)
    public Result<Void> add(@RequestBody Book book) {
        boolean ok = bookService.save(book);
        return ok ? Result.success("新增成功", null) : Result.error("新增失败");
    }

    /**
     * 编辑图书
     * PUT /api/book
     */
    @PutMapping
    @RequirePermission(PermissionCodes.BOOK_EDIT)
    public Result<Void> update(@RequestBody Book book) {
        if (book.getId() == null) {
            return Result.error("图书ID不能为空");
        }
        boolean ok = bookService.updateById(book);
        return ok ? Result.success("修改成功", null) : Result.error("修改失败, 图书可能不存在");
    }

    /**
     * 删除图书
     * DELETE /api/book/{id}
     */
    @DeleteMapping("/{id}")
    @RequirePermission(PermissionCodes.BOOK_DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        boolean ok = bookService.removeById(id);
        return ok ? Result.success("删除成功", null) : Result.error("删除失败, 图书可能不存在");
    }
}
