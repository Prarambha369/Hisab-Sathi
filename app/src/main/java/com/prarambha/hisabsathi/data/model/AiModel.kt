package com.prarambha.hisabsathi.data.model

/**
 * On-device chat models the app can download. All are MediaPipe LLM Inference `.task` bundles
 * from Hugging Face's litert-community, chosen because they are openly licensed and download
 * without a Hugging Face login (Android's DownloadManager can't sign in).
 *
 * Not included:
 * - Gemma 3 (1B, 270M): Google's licence makes Hugging Face require a login, so the file URL
 *   returns 401 to the app.
 * - Qwen 3: only published as `.litertlm`, which needs the LiteRT-LM runtime, not MediaPipe.
 *
 * Sizes are the exact byte counts reported by Hugging Face, so a partial download is never
 * mistaken for a finished one. Update [sizeBytes] if a file is replaced upstream.
 */
data class AiModel(
    val id: String,
    val displayName: String,
    val family: String,
    val trait: Trait,
    val description: String,
    val url: String,
    val fileName: String,
    val sizeBytes: Long,
    /** KV-cache size the file was exported with ("ekvN"); also the max tokens MediaPipe accepts. */
    val contextTokens: Int,
    /**
     * Estimates, in GB of RAM as Android reports it: noticeably below the advertised size
     * (a "4 GB" phone reports ~3.6, "6 GB" ~5.6, "8 GB" ~7.3, "12 GB" ~11.2). Not measured on
     * devices yet; tune these once real phones have been tested.
     */
    val minRamGb: Double,
    val comfortableRamGb: Double,
    /** Higher = better answers. Used to pick the recommended model. */
    val qualityRank: Int,
    /** Can be recommended as the default chat model (reasoning models are slower and verbose). */
    val generalPurpose: Boolean = true,
    /** Emits `<think>…</think>` blocks before its answer. */
    val emitsReasoning: Boolean = false,
    val license: String,
) {
    val sizeMb: Long get() = sizeBytes / 1_000_000

    enum class Trait(val label: String) {
        LIGHTEST("Lightest"),
        FAST("Fast"),
        BALANCED("Balanced"),
        REASONING("Reasoning"),
        POWERFUL("Most powerful"),
    }
}

object AiModelCatalog {
    private const val HF = "https://huggingface.co/litert-community"

    val QWEN_1_5B = AiModel(
        id = "qwen2.5-1.5b",
        displayName = "Qwen 2.5 1.5B",
        family = "Qwen",
        trait = AiModel.Trait.BALANCED,
        description = "Good answers at a reasonable speed. Best all-rounder for most phones.",
        url = "$HF/Qwen2.5-1.5B-Instruct/resolve/main/Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.task?download=true",
        // Same file name as the app's original single model, so existing downloads keep working.
        fileName = "qwen2.5-1.5b-instruct.task",
        sizeBytes = 1_598_556_720L,
        contextTokens = 4096,
        minRamGb = 3.4,
        comfortableRamGb = 5.2,
        qualityRank = 3,
        license = "Apache 2.0",
    )

    val all: List<AiModel> = listOf(
        AiModel(
            id = "smollm-135m",
            displayName = "SmolLM 135M",
            family = "SmolLM",
            trait = AiModel.Trait.LIGHTEST,
            description = "Tiny and very quick. Basic answers only; for older or 2–3 GB phones.",
            url = "$HF/SmolLM-135M-Instruct/resolve/main/SmolLM-135M-Instruct_multi-prefill-seq_q8_ekv1280.task?download=true",
            fileName = "smollm-135m-instruct.task",
            sizeBytes = 166_754_726L,
            contextTokens = 1280,
            minRamGb = 1.2,
            comfortableRamGb = 2.2,
            qualityRank = 1,
            license = "Apache 2.0",
        ),
        AiModel(
            id = "qwen2.5-0.5b",
            displayName = "Qwen 2.5 0.5B",
            family = "Qwen",
            trait = AiModel.Trait.FAST,
            description = "Fast replies and a small download. Fine for quick questions about your spending.",
            url = "$HF/Qwen2.5-0.5B-Instruct/resolve/main/Qwen2.5-0.5B-Instruct_multi-prefill-seq_q8_ekv1280.task?download=true",
            fileName = "qwen2.5-0.5b-instruct.task",
            sizeBytes = 546_660_344L,
            contextTokens = 1280,
            minRamGb = 2.2,
            comfortableRamGb = 2.7,
            qualityRank = 2,
            license = "Apache 2.0",
        ),
        QWEN_1_5B,
        AiModel(
            id = "deepseek-r1-1.5b",
            displayName = "DeepSeek R1 Distill 1.5B",
            family = "DeepSeek",
            trait = AiModel.Trait.REASONING,
            description = "Thinks step by step before answering. Better at calculations, slower to reply.",
            url = "$HF/DeepSeek-R1-Distill-Qwen-1.5B/resolve/main/DeepSeek-R1-Distill-Qwen-1.5B_multi-prefill-seq_q8_ekv4096.task?download=true",
            fileName = "deepseek-r1-distill-qwen-1.5b.task",
            sizeBytes = 1_834_078_546L,
            contextTokens = 4096,
            minRamGb = 5.2,
            comfortableRamGb = 7.0,
            qualityRank = 3,
            generalPurpose = false,
            emitsReasoning = true,
            license = "MIT",
        ),
        AiModel(
            id = "phi-4-mini",
            displayName = "Phi-4 mini 3.8B",
            family = "Phi",
            trait = AiModel.Trait.POWERFUL,
            description = "The smartest option. Large download; needs a flagship phone with 8 GB+ RAM.",
            url = "$HF/Phi-4-mini-instruct/resolve/main/Phi-4-mini-instruct_multi-prefill-seq_q8_ekv4096.task?download=true",
            fileName = "phi-4-mini-instruct.task",
            sizeBytes = 3_910_050_199L,
            contextTokens = 4096,
            minRamGb = 7.0,
            comfortableRamGb = 10.5,
            qualityRank = 4,
            license = "MIT",
        ),
    )

    fun byId(id: String?): AiModel? = all.firstOrNull { it.id == id }
}

/** How well a model suits this phone. */
enum class ModelFit(val label: String) {
    RECOMMENDED("Recommended for your phone"),
    RUNS_WELL("Runs well"),
    MAY_BE_SLOW("May be slow"),
    TOO_HEAVY("Too heavy for this phone"),
    NOT_ENOUGH_STORAGE("Not enough storage"),
}

data class DeviceSpecs(val totalRamGb: Double, val freeStorageBytes: Long)

object ModelAdvisor {
    /** Space needed to download a model: the file plus 10% headroom. */
    fun requiredStorage(model: AiModel): Long = model.sizeBytes + model.sizeBytes / 10

    /**
     * Rates every model for this phone. Exactly one model is [ModelFit.RECOMMENDED] whenever any
     * model fits: the best general-purpose model that runs comfortably, else the best one that
     * runs at all. [downloaded] models never get [ModelFit.NOT_ENOUGH_STORAGE].
     */
    fun rate(
        specs: DeviceSpecs,
        models: List<AiModel> = AiModelCatalog.all,
        downloaded: Set<String> = emptySet(),
    ): Map<String, ModelFit> {
        fun hasSpace(m: AiModel) = m.id in downloaded || specs.freeStorageBytes >= requiredStorage(m)

        val base = models.associate { m ->
            m.id to when {
                specs.totalRamGb < m.minRamGb -> ModelFit.TOO_HEAVY
                !hasSpace(m) -> ModelFit.NOT_ENOUGH_STORAGE
                specs.totalRamGb < m.comfortableRamGb -> ModelFit.MAY_BE_SLOW
                else -> ModelFit.RUNS_WELL
            }
        }
        val candidates = models.filter { it.generalPurpose }
        val recommended =
            candidates.filter { base[it.id] == ModelFit.RUNS_WELL }.maxByOrNull { it.qualityRank }
                ?: candidates.filter { base[it.id] == ModelFit.MAY_BE_SLOW }.minByOrNull { it.minRamGb }
        return if (recommended == null) base else base + (recommended.id to ModelFit.RECOMMENDED)
    }
}
