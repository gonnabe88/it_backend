package com.kdb.it.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI 3.0) 문서화 설정 클래스
 *
 * <p>springdoc-openapi 라이브러리를 사용하여 REST API 명세서를 자동으로 생성합니다.</p>
 *
 * <p>Swagger UI 접근 URL:</p>
 * <ul>
 *   <li>Swagger UI: {@code http://localhost:8080/swagger-ui/index.html}</li>
 *   <li>API 명세 JSON: {@code http://localhost:8080/v3/api-docs}</li>
 * </ul>
 *
 * <p>인증 없이 접근 가능 (SecurityConfig에서 permitAll 처리됨)</p>
 */
@Configuration // Spring 설정 클래스로 등록
public class SwaggerConfig {

    /**
     * OpenAPI 명세서 메타데이터 빈 등록
     *
     * <p>Swagger UI 상단에 표시되는 API 제목, 설명, 버전 정보를 설정합니다.</p>
     *
     * <p>설정 항목:</p>
     * <ul>
     *   <li>title: API 문서 제목 ({@code "IT Portal API"})</li>
     *   <li>description: API 전체 설명 ({@code "IT Portal 프로젝트 API 명세서"})</li>
     *   <li>version: API 버전 ({@code "1.0.0"})</li>
     * </ul>
     *
     * @return {@link OpenAPI} 인스턴스 (Swagger UI에 표시될 메타데이터 포함)
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IT Portal API")                   // Swagger UI 제목
                        .description("IT Portal 프로젝트 API 명세서") // Swagger UI 설명
                        .version("1.0.0"));                        // API 버전
    }
}
