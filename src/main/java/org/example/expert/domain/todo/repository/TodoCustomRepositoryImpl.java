package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.TodoSearchCondition;
import org.example.expert.domain.todo.dto.response.QTodoSearchResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class TodoCustomRepositoryImpl implements TodoCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        Todo result = queryFactory
                .selectFrom(todo)
                // N + 1 문제 해결 위해서 FETCH JOIN 사용
                .leftJoin(todo.user, user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> searchTodosList(TodoSearchCondition condition, Pageable pageable) {
        /**
         * SELECT
         *     t.title,
         *     COUNT(DISTINCT m.id) AS managerCount,
         *     COUNT(DISTINCT c.id) AS commentCount
         * FROM todos t
         * LEFT JOIN managers m ON t.id = m.todo_id
         * LEFT JOIN comments c ON t.id = c.todo_id
         * WHERE
         *     t.title LIKE '%검색어%'
         *     AND m.user_id IN (SELECT id FROM users WHERE nickname LIKE '%닉네임%')
         *     AND t.created_at BETWEEN '2025-01-01' AND '2025-12-31'
         * GROUP BY t.id
         * ORDER BY t.created_at DESC
         * LIMIT 10 OFFSET 0
         */
        // 메인 쿼리
        List<TodoSearchResponse> content = queryFactory
                .select(new QTodoSearchResponse( // Projection을 활용하여 필요한 필드만 검색
                        todo.title,
                        manager.count(),
                        comment.count()
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(todo.comments, comment)
                .where(
                        titleContains(condition.getTitle()), // 일정 제목 부분적 일치
                        managerNicknameContains(condition.getManagerNickname()), // 담당자 닉네임 부분적 일치
                        createdAtBetween(condition.getStartDate(), condition.getEndDate()) // 생성일 기준 시작일~종료일
                )
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        /**
         * SELECT COUNT(DISTINCT t.id)
         * FROM todos t
         * LEFT JOIN managers m ON t.id = m.todo_id
         * WHERE
         *     t.title LIKE '%검색어%'
         *     AND m.user_id IN (SELECT id FROM users WHERE nickname LIKE '%닉네임%')
         *     AND t.created_at BETWEEN '2025-01-01' AND '2025-12-31'
         */
        // 카운트 쿼리 - 성능 최적화
        JPAQuery<Long> countQuery = queryFactory
                .select(todo.countDistinct())
                .from(todo)
                .leftJoin(todo.managers, manager)
                .where(
                        titleContains(condition.getTitle()),
                        managerNicknameContains(condition.getManagerNickname()),
                        createdAtBetween(condition.getStartDate(), condition.getEndDate())
                );
        // PageableExecutionUtils로 count 쿼리 최적화
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // 동적 쿼리 조건 메서드
    private BooleanExpression titleContains(String title) {
        return title != null ? todo.title.contains(title) : null;
    }

    private BooleanExpression managerNicknameContains(String nickname) {
        return nickname != null ? manager.user.nickname.contains(nickname) : null;
    }

    // LocalDate → LocalDateTime 변환 로직
    private BooleanExpression createdAtBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            // 시작일 00:00:00 ~ 종료일 23:59:59.999999999
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);
            return todo.createdAt.between(start, end);

        } else if (startDate != null) {
            // 시작일 00:00:00 이후
            LocalDateTime start = startDate.atStartOfDay();
            return todo.createdAt.goe(start);

        } else if (endDate != null) {
            // 종료일 23:59:59.999999999 이전
            LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);
            return todo.createdAt.loe(end);
        }
        return null;
    }
}
