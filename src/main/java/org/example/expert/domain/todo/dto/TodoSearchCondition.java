package org.example.expert.domain.todo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TodoSearchCondition {
    private String title;            // 제목 검색
    private String managerNickname;  // 담당자 닉네임 검색
    private LocalDate startDate;     // 생성일 기준 시작일
    private LocalDate endDate;       // 생성일 기준 종료일
}
