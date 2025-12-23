package org.example.expert.domain.todo.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class TodoSearchResponse {

    private final String title;           // 일정 제목
    private final Long managerCount;      // 담당자 수
    private final Long commentCount;      // 댓글 수

    // @QueryProjection으로 타입 안전한 Projection
    @QueryProjection
    public TodoSearchResponse(String title, Long managerCount, Long commentCount) {
        this.title = title;
        this.managerCount = managerCount;
        this.commentCount = commentCount;
    }
}
