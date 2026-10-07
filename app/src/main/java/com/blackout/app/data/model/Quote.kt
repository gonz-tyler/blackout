package com.blackout.app.data.model

data class Quote(
    val id: String,
    val category: String,
    val author: String,
    val text: Map<String, String>
) {
    fun getText(languageCode: String = "en"): String {
        val lang = if (languageCode == "system") {
            java.util.Locale.getDefault().language // e.g. "en" or "es"
        } else {
            languageCode
        }
        return text[lang] ?: text["en"] ?: text.values.firstOrNull().orEmpty()
    }
}
