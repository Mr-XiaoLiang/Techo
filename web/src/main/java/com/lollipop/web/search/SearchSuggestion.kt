package com.lollipop.web.search

data class SearchSuggestion(
    val name: String,
    val url: String,
    val type: SearchSuggestionType,
    val score: Int
) {

    companion object {
        const val SCORE_MAX = 100
        const val SCORE_MIN = 1
    }

}

/**
 * 搜索引擎的建议类型
 */
enum class SearchSuggestionType {
    /**
     * 普通搜索操作，返回的URL会使用引擎搜索
     */
    SEARCH,

    /**
     * 站点，返回的URL将会是某个网站的地址
     */
    SITE,

    /**
     * 历史，返回的URL是历史记录中的
     */
    HISTORY,

    /**
     * 设置，这个URl会是内部的设置页面的路径
     */
    SETTINGS
}