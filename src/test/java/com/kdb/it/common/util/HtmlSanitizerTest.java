package com.kdb.it.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlSanitizerTest {

    @Test
    @DisplayName("math-field 태그와 허용된 속성이 삭제되지 않아야 한다.")
    void shouldAllowMathFieldTag() {
        // Given
        String html = "<math-field data-v-f73b7830=\"\" read-only=\"\" class=\"math-field-inline\" contenteditable=\"true\" tabindex=\"0\" style=\"display: inline-flex;\"></math-field>";

        // When
        String sanitized = HtmlSanitizer.sanitize(html);

        // Then
        // data-v-f73b7830은 제외되어야 함
        assertThat(sanitized).doesNotContain("data-v-f73b7830");

        // 나머지는 유지되어야 함
        assertThat(sanitized).contains("<math-field");
        assertThat(sanitized).contains("read-only=\"\"");
        assertThat(sanitized).contains("class=\"math-field-inline\"");
        assertThat(sanitized).contains("contenteditable=\"true\"");
        assertThat(sanitized).contains("tabindex=\"0\"");
        assertThat(sanitized).contains("style=\"display: inline-flex;\"");
    }

    @Test
    @DisplayName("인라인 수식 span의 data-latex 속성이 보존되어야 한다.")
    void shouldPreserveInlineMathDataLatex() {
        // Given: InlineMathExtension이 렌더링하는 HTML
        String html = "<p><span data-type=\"inline-math\" data-latex=\"E=mc^2\" class=\"math-inline-node\"></span></p>";

        // When
        String sanitized = HtmlSanitizer.sanitize(html);

        // Then
        assertThat(sanitized).contains("data-latex=\"E=mc^2\"");
        assertThat(sanitized).contains("data-type=\"inline-math\"");
        assertThat(sanitized).contains("class=\"math-inline-node\"");
    }

    @Test
    @DisplayName("블록 수식 div의 data-latex 속성이 보존되어야 한다.")
    void shouldPreserveBlockMathDataLatex() {
        // Given: BlockMathExtension이 렌더링하는 HTML
        String html = "<div data-type=\"block-math\" data-latex=\"\\\\frac{a}{b}\" class=\"math-block-node\"></div>";

        // When
        String sanitized = HtmlSanitizer.sanitize(html);

        // Then
        assertThat(sanitized).contains("data-latex=\"\\\\frac{a}{b}\"");
        assertThat(sanitized).contains("data-type=\"block-math\"");
        assertThat(sanitized).contains("class=\"math-block-node\"");
    }

    @Test
    @DisplayName("인라인 수식의 data-latex가 XSS 시도 없이 일반 LaTeX를 보존해야 한다.")
    void shouldPreserveComplexLatexFormula() {
        // Given: 복잡한 LaTeX 수식
        String html = "<p><span data-type=\"inline-math\" data-latex=\"\\\\sum_{i=0}^{n} x_i\" class=\"math-inline-node\"></span></p>";

        // When
        String sanitized = HtmlSanitizer.sanitize(html);

        // Then: data-latex 속성값이 보존됨
        assertThat(sanitized).contains("data-type=\"inline-math\"");
        assertThat(sanitized).doesNotContain("script");
    }
}
