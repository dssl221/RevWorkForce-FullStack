package com.revworkforce.revworkforce_web.dao;

import com.revworkforce.revworkforce_web.model.PerformanceReview;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class PerformanceReviewDao {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    private final RowMapper<PerformanceReview> rowMapper = (rs, rowNum) -> PerformanceReview.builder()
            .id(rs.getLong("id"))
            .employeeId(rs.getLong("employee_id"))
            .deliverables(rs.getString("deliverables"))
            .accomplishments(rs.getString("accomplishments"))
            .improvements(rs.getString("improvements"))
            .selfRating(rs.getObject("self_rating") != null ? rs.getInt("self_rating") : null)
            .managerRating(rs.getObject("manager_rating") != null ? rs.getInt("manager_rating") : null)
            .managerFeedback(rs.getString("manager_feedback"))
            .status(rs.getString("status"))
            .reviewYear(rs.getObject("review_year") != null ? rs.getInt("review_year") : null)
            .build();

    public PerformanceReview save(PerformanceReview review) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_create_review");
        Map<String, Object> params = new HashMap<>();
        params.put("p_employee_id", review.getEmployeeId());
        params.put("p_deliverables", review.getDeliverables());
        params.put("p_accomplishments", review.getAccomplishments());
        params.put("p_improvements", review.getImprovements());
        params.put("p_self_rating", review.getSelfRating());
        params.put("p_review_year", review.getReviewYear());

        Map<String, Object> result = jdbcCall.execute(params);
        review.setId(((Number) result.get("P_REVIEW_ID")).longValue());
        review.setStatus("DRAFT");
        return review;
    }

    public Optional<PerformanceReview> findById(Long id) {
        List<PerformanceReview> list = jdbcTemplate.query(
                "SELECT * FROM performance_reviews WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<PerformanceReview> findByEmployeeId(Long employeeId) {
        return jdbcTemplate.query(
                "SELECT * FROM performance_reviews WHERE employee_id = ? ORDER BY id DESC",
                rowMapper, employeeId);
    }

    public List<PerformanceReview> findByEmployeeIdIn(List<Long> employeeIds) {
        if (employeeIds.isEmpty())
            return Collections.emptyList();
        String placeholders = String.join(",", Collections.nCopies(employeeIds.size(), "?"));
        return jdbcTemplate.query(
                "SELECT * FROM performance_reviews WHERE employee_id IN (" + placeholders + ") ORDER BY id DESC",
                rowMapper, employeeIds.toArray());
    }

    public void submitReview(Long reviewId) {
        new SimpleJdbcCall(dataSource).withProcedureName("sp_submit_review")
                .execute(Map.of("p_review_id", reviewId));
    }

    public void provideFeedback(Long reviewId, int managerRating, String feedback) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_provide_feedback");
        Map<String, Object> params = new HashMap<>();
        params.put("p_review_id", reviewId);
        params.put("p_manager_rating", managerRating);
        params.put("p_manager_feedback", feedback);
        jdbcCall.execute(params);
    }
}
