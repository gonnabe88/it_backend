package com.kdb.it.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * HTML 새니타이징 유틸리티
 *
 * <p>
 * Quill 에디터에서 생성한 Rich Text(HTML)를 서버 측에서 새니타이징합니다.
 * 프론트엔드(DOMPurify)와 함께 이중 방어 체계를 구성합니다.
 * </p>
 *
 * <p>
 * 허용 태그/속성은 Quill 에디터의 기본 포맷 기준으로 설정되어 있습니다.
 * {@code <script>}, {@code onclick} 등 위험 요소는 자동으로 제거됩니다.
 * </p>
 *
 * <p>
 * 허용 태그 목록:
 * </p>
 * <ul>
 * <li>블록: {@code p, blockquote, h1~h6, pre, ol, ul, li}</li>
 * <li>인라인: {@code strong, em, u, s, br, span, code}</li>
 * <li>테이블: {@code table, thead, tbody, tfoot, tr, th, td, colgroup, col}</li>
 * <li>미디어/링크:
 * {@code a(href), img(src, alt, data-scene), figure(data-type)}</li>
 * </ul>
 */
public final class HtmlSanitizer {

    /** Quill 에디터 허용 태그/속성 기반 Safelist (불변 싱글턴) */
    private static final Safelist QUILL_SAFELIST = createQuillSafelist();

    /** 유틸리티 클래스이므로 인스턴스 생성 방지 */
    private HtmlSanitizer() {
        throw new UnsupportedOperationException("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    /**
     * 리치에디터 허용 태그/속성 Safelist 생성
     *
     * <p>
     * {@link Safelist#none()} 기반으로 시작하여 리치에디터에서
     * 사용하는 태그와 속성만 허용 목록에 추가합니다.
     * </p>
     *
     * @return 리치에디터 전용 Safelist
     */
    private static Safelist createQuillSafelist() {
        return new Safelist()
                // 블록 요소
                .addTags("p", "br", "blockquote", "pre",
                        "h1", "h2", "h3", "h4", "h5", "h6",
                        "ol", "ul", "li")
                // 인라인 서식 요소
                .addTags("strong", "em", "u", "s", "span", "code")
                // 테이블 요소
                .addTags("table", "thead", "tbody", "tfoot", "tr", "th", "td",
                        "colgroup", "col")
                // 테이블 관련 스타일/속성 허용 (셀 병합, 너비 등)
                .addAttributes("table", "style", "class", "border", "cellpadding", "cellspacing", "width")
                .addAttributes("thead", "style", "class")
                .addAttributes("tbody", "style", "class")
                .addAttributes("tfoot", "style", "class")
                .addAttributes("tr", "style", "class")
                .addAttributes("th", "colspan", "rowspan", "style", "class", "width", "height", "align", "valign")
                .addAttributes("td", "colspan", "rowspan", "style", "class", "width", "height", "align", "valign")
                .addAttributes("col", "span", "style", "width")
                .addAttributes("colgroup", "span")
                // 링크: href 속성 허용
                .addTags("a")
                .addAttributes("a", "href")
                .addProtocols("a", "href", "http", "https", "mailto")
                // Excalidraw / 미디어 래퍼: figure 태그 허용
                .addTags("figure")
                .addAttributes("figure", "data-type", "class", "style")
                // 이미지: src, alt, data-scene(Excalidraw 재편집용), width, height 속성 허용
                .addTags("img")
                .addAttributes("img", "src", "alt", "data-scene", "width", "height")
                .addProtocols("img", "src", "http", "https", "data")
                // span: style, class 속성 허용 (서식용)
                .addAttributes("span", "style", "class");
    }

    /**
     * HTML 문자열에서 위험 요소를 제거하고 안전한 HTML만 반환
     *
     * <p>
     * {@code null} 또는 빈 문자열은 입력값 그대로 반환합니다.
     * Quill 에디터 허용 태그/속성 외의 모든 요소는 자동 제거됩니다.
     * (예: {@code <script>}, {@code onclick}, {@code onerror} 등)
     * </p>
     *
     * @param html 새니타이징할 HTML 문자열
     * @return 안전한 HTML 문자열 (위험 요소 제거됨)
     */
    public static String sanitize(String html) {
        // null 또는 빈 문자열은 그대로 반환
        if (html == null || html.isEmpty()) {
            return html;
        }
        // prettyPrint=false: Jsoup 자동 줄바꿈/공백 삽입 방지 (테이블 구조 보존)
        Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);
        return Jsoup.clean(html, "", QUILL_SAFELIST, outputSettings);
    }
}
