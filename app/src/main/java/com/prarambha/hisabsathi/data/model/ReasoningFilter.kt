package com.prarambha.hisabsathi.data.model

/**
 * Hides `<think>…</think>` reasoning that models like DeepSeek R1 write before their answer.
 *
 * Feed streamed chunks to [accept]; it returns only the newly visible text. Text inside an
 * unfinished `<think>` block, or a trailing partial tag such as `<thi`, is held back until it
 * can be classified. [visible] gives the cleaned full text for saving.
 */
class ReasoningFilter {
    private val raw = StringBuilder()
    private var emitted = ""

    fun accept(chunk: String): String {
        raw.append(chunk)
        val visible = visibleSoFar(raw.toString())
        // If earlier output turns out to have been reasoning (a reply that closes </think>
        // without opening it), stop streaming; the saved message uses visible() and is clean.
        if (!visible.startsWith(emitted) || visible.length == emitted.length) return ""
        val delta = visible.substring(emitted.length)
        emitted = visible
        return delta
    }

    fun visible(): String = strip(raw.toString())

    companion object {
        private const val OPEN = "<think>"
        private const val CLOSE = "</think>"
        private val BLOCK = Regex("""<think>.*?</think>""", RegexOption.DOT_MATCHES_ALL)

        /** Complete text with all reasoning removed (an unclosed block is dropped too). */
        fun strip(text: String): String {
            var s = BLOCK.replace(text, "")
            // R1 sometimes omits the opening tag and only closes it.
            s.indexOf(CLOSE).takeIf { it >= 0 }?.let { s = s.substring(it + CLOSE.length) }
            s.indexOf(OPEN).takeIf { it >= 0 }?.let { s = s.substring(0, it) }
            return s.trimStart()
        }

        private fun visibleSoFar(text: String): String {
            // Until we know whether the reply starts with reasoning, show nothing.
            val trimmed = text.trimStart()
            if (trimmed.isNotEmpty() && OPEN.startsWith(trimmed) && trimmed.length < OPEN.length) return ""
            var s = strip(text)
            if (!text.contains(CLOSE) && !text.contains(OPEN)) {
                // Hold back a trailing partial "<think>" or "</think>".
                for (tag in listOf(OPEN, CLOSE)) {
                    for (n in tag.length - 1 downTo 1) {
                        if (s.endsWith(tag.substring(0, n))) { s = s.dropLast(n); break }
                    }
                }
            }
            return s
        }
    }
}
