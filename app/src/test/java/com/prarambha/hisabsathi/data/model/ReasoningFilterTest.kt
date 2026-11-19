package com.prarambha.hisabsathi.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ReasoningFilterTest {

    private fun stream(vararg chunks: String): Pair<String, String> {
        val f = ReasoningFilter()
        val shown = chunks.joinToString("") { f.accept(it) }
        return shown to f.visible()
    }

    @Test
    fun `reasoning block is hidden while streaming and when saved`() {
        val (shown, saved) = stream("<thi", "nk>Let me add 300 and 200", "</think>", "\n\nYou spent ", "रु 500.")
        assertEquals("You spent रु 500.", shown)
        assertEquals("You spent रु 500.", saved)
    }

    @Test
    fun `plain replies pass through unchanged`() {
        val (shown, saved) = stream("Hello", " world")
        assertEquals("Hello world", shown)
        assertEquals("Hello world", saved)
    }

    @Test
    fun `a trailing angle bracket is held back until it is clearly not a tag`() {
        val f = ReasoningFilter()
        assertEquals("Total is 5 ", f.accept("Total is 5 <"))
        assertEquals("<b>", f.accept("b>"))
    }

    @Test
    fun `reply that only closes the think tag is saved without the reasoning`() {
        val (_, saved) = stream("Hmm, thinking", "</think>Answer: रु 1,200")
        assertEquals("Answer: रु 1,200", saved)
    }

    @Test
    fun `unfinished reasoning shows nothing`() {
        val (shown, saved) = stream("<think>still working")
        assertEquals("", shown)
        assertEquals("", saved)
    }
}
