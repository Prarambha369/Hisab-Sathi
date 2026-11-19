package com.prarambha.hisabsathi.data.preferences

import android.content.Context
import androidx.annotation.Keep
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.prarambha.parser.core.DecimalStyle
import com.prarambha.parser.core.ShapeSegment
import com.prarambha.parser.core.ShapeSlot
import com.prarambha.parser.core.TaughtShape
import com.prarambha.parser.core.TransactionType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.taughtShapesDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "taught_shapes")

/**
 * The SMS shapes a user has taught by tapping an unrecognized message.
 *
 * Only the shape is stored — the fixed wording and which parts are the amount, merchant and
 * account — never the message itself. New readings are produced from a stored shape and
 * still need the user's confirmation before anything is saved.
 */
@Singleton
class TaughtShapesStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    /** Every taught shape, in the order it was taught. */
    val shapes: Flow<List<TaughtShape>> = context.taughtShapesDataStore.data
        .map { preferences -> decode(preferences[Keys.SHAPES]) }

    suspend fun add(shape: TaughtShape) {
        context.taughtShapesDataStore.edit { preferences ->
            val current = decode(preferences[Keys.SHAPES])
            preferences[Keys.SHAPES] = encode(current.filterNot { it.id == shape.id } + shape)
        }
    }

    private object Keys {
        val SHAPES = stringPreferencesKey("shapes_json")
    }

    // A stored shape is kept as plain names so a future rename of an enum value shows up as
    // one skipped shape rather than a crash on startup. @Keep is what lets Gson read the
    // field names back after R8 renames everything else in a release build.
    @Keep
    private data class StoredShape(
        val id: String? = null,
        val senderKey: String? = null,
        val segments: List<StoredSegment>? = null,
        val direction: String? = null,
        val currency: String? = null,
        val bankLabel: String? = null,
        val decimalStyle: String? = null,
        val defaultMerchant: String? = null
    )

    @Keep
    private data class StoredSegment(val slot: String? = null, val literal: String? = null)

    private fun encode(shapes: List<TaughtShape>): String =
        gson.toJson(shapes.map { it.stored() })

    private fun decode(raw: String?): List<TaughtShape> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            gson.fromJson(raw, Array<StoredShape>::class.java)
                ?.mapNotNull { it.toShape() }
                .orEmpty()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun TaughtShape.stored() = StoredShape(
        id = id,
        senderKey = senderKey,
        segments = segments.map { StoredSegment(it.slot.name, it.literal) },
        direction = direction.name,
        currency = currency,
        bankLabel = bankLabel,
        decimalStyle = decimalStyle?.name,
        defaultMerchant = defaultMerchant
    )

    private fun StoredShape.toShape(): TaughtShape? {
        val id = id ?: return null
        val senderKey = senderKey ?: return null
        val currency = currency ?: return null
        val bankLabel = bankLabel ?: return null
        val direction = direction?.let { runCatching { TransactionType.valueOf(it) }.getOrNull() } ?: return null

        val shapeSegments = segments.orEmpty().map { stored ->
            val slot = stored.slot?.let { runCatching { ShapeSlot.valueOf(it) }.getOrNull() } ?: return null
            ShapeSegment(slot, stored.literal.orEmpty())
        }
        val style = decimalStyle?.let { runCatching { DecimalStyle.valueOf(it) }.getOrNull() }

        return TaughtShape(
            id = id,
            senderKey = senderKey,
            segments = shapeSegments,
            direction = direction,
            currency = currency,
            bankLabel = bankLabel,
            decimalStyle = style,
            defaultMerchant = defaultMerchant
        )
    }
}
