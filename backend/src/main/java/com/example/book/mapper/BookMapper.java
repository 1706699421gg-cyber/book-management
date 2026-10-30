package com.example.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.book.entity.Book;
import com.example.book.vo.CategoryCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 图书 Mapper
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {

    /**
     * 库存总量
     */
    @Select("SELECT IFNULL(SUM(stock), 0) FROM t_book")
    Long sumStock();

    /**
     * 按分类统计图书数量(LEFT JOIN, 数量为 0 的分类也返回)
     */
    @Select("SELECT c.id AS categoryId, c.name AS name, COUNT(b.id) AS total " +
            "FROM t_category c LEFT JOIN t_book b ON b.category_id = c.id " +
            "GROUP BY c.id, c.name " +
            "ORDER BY total DESC")
    List<CategoryCountVO> selectCategoryCount();
}
