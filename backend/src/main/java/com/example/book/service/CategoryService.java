package com.example.book.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.book.entity.Category;

/**
 * 图书分类 Service
 */
public interface CategoryService extends IService<Category> {

    /**
     * 删除分类: 若该分类下仍存在图书, 抛出业务异常阻止删除
     *
     * @param id 分类ID
     */
    void removeCategory(Long id);
}
