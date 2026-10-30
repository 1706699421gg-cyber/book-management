package com.example.book.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.book.entity.Book;

/**
 * 图书 Service
 */
public interface BookService extends IService<Book> {

    /**
     * 分页查询图书, 支持按书名/作者模糊搜索
     *
     * @param current  当前页(从 1 开始)
     * @param size     每页条数
     * @param keyword  关键字(书名/作者), 可为空
     * @return 分页结果
     */
    Page<Book> pageBooks(long current, long size, String keyword);
}
