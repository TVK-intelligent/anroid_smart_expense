package com.example.smart_expense.repository;

import com.example.smart_expense.model.Category;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class CategoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public CategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Category> categoryRowMapper = (rs, rowNum) -> Category.builder()
            .categoryId(rs.getInt("category_id"))
            .userId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null)
            .name(rs.getString("name"))
            .type(rs.getString("type"))
            .icon(rs.getString("icon"))
            .build();

    public Category save(Category category) {
        String sql = "INSERT INTO categories (user_id, name, type, icon) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (category.getUserId() != null) {
                ps.setInt(1, category.getUserId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, category.getName());
            ps.setString(3, category.getType());
            ps.setString(4, category.getIcon() != null ? category.getIcon() : "default_icon");
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            category.setCategoryId(keyHolder.getKey().intValue());
        }
        return category;
    }

    public Optional<Category> findById(Integer categoryId) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";
        try {
            Category category = jdbcTemplate.queryForObject(sql, categoryRowMapper, categoryId);
            return Optional.ofNullable(category);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Category> findActiveCategoriesForUser(Integer userId) {
        String sql = "SELECT * FROM categories WHERE user_id IS NULL OR user_id = ?";
        return jdbcTemplate.query(sql, categoryRowMapper, userId);
    }
}
