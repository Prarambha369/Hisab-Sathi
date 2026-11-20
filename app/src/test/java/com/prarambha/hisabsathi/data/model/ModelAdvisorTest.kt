package com.prarambha.hisabsathi.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelAdvisorTest {

    private val plenty = 50_000_000_000L

    private fun recommended(ramGb: Double, free: Long = plenty, downloaded: Set<String> = emptySet()): String? =
        ModelAdvisor.rate(DeviceSpecs(ramGb, free), downloaded = downloaded)
            .filterValues { it == ModelFit.RECOMMENDED }.keys.singleOrNull()

    @Test
    fun `exactly one model is recommended on every phone that can run one`() {
        listOf(1.8, 2.8, 3.6, 5.6, 7.3, 11.2, 15.0).forEach { ram ->
            val fits = ModelAdvisor.rate(DeviceSpecs(ram, plenty))
            assertEquals("RAM $ram", 1, fits.values.count { it == ModelFit.RECOMMENDED })
        }
    }

    @Test
    fun `recommendation grows with the phone (Android-reported RAM)`() {
        assertEquals("smollm-135m", recommended(1.8))   // "2 GB" phone
        assertEquals("qwen2.5-0.5b", recommended(2.8))  // "3 GB"
        assertEquals("qwen2.5-0.5b", recommended(3.6))  // "4 GB"
        assertEquals("qwen2.5-1.5b", recommended(5.6))  // "6 GB"
        assertEquals("qwen2.5-1.5b", recommended(7.3))  // "8 GB": Phi only "may be slow"
        assertEquals("phi-4-mini", recommended(11.2))   // "12 GB"
    }

    @Test
    fun `reasoning model is labelled but never the default recommendation`() {
        val fits = ModelAdvisor.rate(DeviceSpecs(15.0, plenty))
        assertEquals(ModelFit.RUNS_WELL, fits["deepseek-r1-1.5b"])
    }

    @Test
    fun `heavy models are flagged on small phones`() {
        val fits = ModelAdvisor.rate(DeviceSpecs(3.6, plenty))
        assertEquals(ModelFit.MAY_BE_SLOW, fits["qwen2.5-1.5b"])
        assertEquals(ModelFit.TOO_HEAVY, fits["deepseek-r1-1.5b"])
        assertEquals(ModelFit.TOO_HEAVY, fits["phi-4-mini"])
        assertEquals(ModelFit.MAY_BE_SLOW, ModelAdvisor.rate(DeviceSpecs(7.3, plenty))["phi-4-mini"])
    }

    @Test
    fun `nothing is recommended when no model fits`() {
        val fits = ModelAdvisor.rate(DeviceSpecs(1.0, plenty))
        assertTrue(fits.values.all { it == ModelFit.TOO_HEAVY })
    }

    @Test
    fun `low storage blocks big downloads but not models already on the phone`() {
        val oneGb = 1_000_000_000L
        val fits = ModelAdvisor.rate(DeviceSpecs(7.3, oneGb))
        assertEquals(ModelFit.NOT_ENOUGH_STORAGE, fits["qwen2.5-1.5b"])
        assertEquals("qwen2.5-0.5b", recommended(7.3, oneGb))
        assertEquals("qwen2.5-1.5b", recommended(7.3, oneGb, downloaded = setOf("qwen2.5-1.5b")))
    }

    @Test
    fun `catalog keeps the original Qwen file so existing downloads still work`() {
        assertEquals("qwen2.5-1.5b-instruct.task", AiModelCatalog.QWEN_1_5B.fileName)
        assertEquals(AiModelCatalog.all.size, AiModelCatalog.all.map { it.fileName }.toSet().size)
        assertEquals(AiModelCatalog.all.size, AiModelCatalog.all.map { it.id }.toSet().size)
        AiModelCatalog.all.forEach {
            assertTrue(it.url.startsWith("https://huggingface.co/litert-community/"))
            assertTrue(it.url.contains(".task"))
            assertTrue(it.contextTokens == 1280 || it.contextTokens == 4096)
            assertTrue("ekv${it.contextTokens}" in it.url)
        }
    }
}
