package com.example.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.book.common.BusinessException;
import com.example.book.entity.Book;
import com.example.book.entity.Category;
import com.example.book.mapper.CategoryMapper;
import com.example.book.service.BookService;
import com.example.book.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 图书分类 Service 实现
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Resource
    private BookService bookService;

    @Override
    public void removeCategory(Long id) {
        Category category = this.getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在或已被删除");
        }
        // 分类下存在图书时禁止删除, 保证数据完整性
        long count = bookService.count(new LambdaQueryWrapper<Book>().eq(Book::getCategoryId, id));
        if (count > 0) {
            throw new BusinessException("该分类下还有 " + count + " 本图书, 无法删除");
        }
        this.removeById(id);
    }
}
