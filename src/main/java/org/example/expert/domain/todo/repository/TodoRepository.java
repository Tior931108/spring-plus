package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long>, TodoCustomRepository {

    // 할 일 검색시 weather 조건으로도 검색하거나 수정일 기준 기간의 시작과 끝 검색하도록 JPQL 수정
    @Query("SELECT t FROM Todo t " +
            "WHERE (:weather IS NULL OR t.weather = :weather) " +
            // modifiedAt이 LocalDateTime형식이어서 LocalDate형식 맞추기 위해 JPQL에서 DATE() 함수 사용
            "AND (:startDate IS NULL OR DATE(t.modifiedAt) >= :startDate) " +
            "AND (:endDate IS NULL OR DATE(t.modifiedAt) <= :endDate)")
    Page<Todo> searchTodos(
            @Param("weather") String weather,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
