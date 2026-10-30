package com.example.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.book.entity.Book;
import com.example.book.mapper.BookMapper;
import com.example.book.service.BookService;
import org.springframework.stereotype.Service;

/**
 * 图书 Service 实现
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Override
    public Page<Book> pageBooks(long current, long size, String keyword) {
        Page<Book> page = new Page<>(current, size);
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        // 关键字 -> 书名/作者模糊匹配(and 嵌套保证条件括号正确)
        if (StringUtils.isNotBlank(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Book::getName, kw).or().like(Book::getAuthor, kw));
        }
        // 按ID正序
        wrapper.orderByAsc(Book::getId);
        return this.page(page, wrapper);
    }
}
