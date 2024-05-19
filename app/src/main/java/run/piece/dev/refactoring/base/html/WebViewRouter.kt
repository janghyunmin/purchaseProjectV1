package run.piece.dev.refactoring.base.html

enum class WebViewRouter(val id: Int, val viewName: String) {
    COMMON(0, "공통 상세"),
    LOUNGE(1, "라운지 상세"),
    STORY(2, "스토리 상세"),
    CONSENT(3, "약관 상세"),
    DISCLOSURE(4, "투자공시 상세"),
    NOTICE(5, "공지사항 상세"),
    EVENT(6, "이벤트 상세")
}