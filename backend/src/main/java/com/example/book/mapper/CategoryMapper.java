package com.example.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.book.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图书分类 Mapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
