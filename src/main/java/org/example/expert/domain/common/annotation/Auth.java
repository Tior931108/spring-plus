package org.example.expert.domain.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auth {

    // Spring Security를 적용했기때문에,
    // @AuthenticationPrincipal 사용으로 전환이 가능하지만,
    // 과제 리팩토링이 우선이기에 AuthUserArgumentResolver 사용방식은 남겨두었습니다.
    // 따라서 커스텀 어노테이션인 @Auth도 남겨두었습니다.
}
