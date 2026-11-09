package com.prarambha.hisabsathi.presentation.ui.features.settings.unrecognized

import com.prarambha.hisabsathi.BuildConfig

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prarambha.hisabsathi.core.Constants
import com.prarambha.hisabsathi.data.database.entity.UnrecognizedSmsEntity
import com.prarambha.hisabsathi.data.manager.SmsTransactionProcessor
import com.prarambha.hisabsathi.data.preferences.TaughtShapesStore
import com.prarambha.hisabsathi.data.repository.UnrecognizedSmsRepository
import com.prarambha.hisabsathi.utils.DeviceEncryption
import com.prarambha.parser.core.ParsedTransaction
import com.prarambha.parser.core.SmsShapes
import com.prarambha.parser.core.TapChoice
import com.prarambha.parser.core.TaughtShape
import com.prarambha.parser.core.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import java.net.URLEncoder
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class UnrecognizedSmsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val unrecognizedSmsRepository: UnrecognizedSmsRepository,
    private val taughtShapesStore: TaughtShapesStore,
    private val smsTransactionProcessor: SmsTransactionProcessor
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _showReported = MutableStateFlow(true)
    val showReported: StateFlow<Boolean> = _showReported.asStateFlow()
    
    private val allMessages = unrecognizedSmsRepository.getAllVisible()
    
    val unrecognizedMessages: StateFlow<List<UnrecognizedSmsEntity>> = 
        combine(allMessages, _showReported) { messages, showReported ->
            if (showReported) {
                messages
            } else {
                messages.filter { !it.reported }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val taughtShapes: StateFlow<List<TaughtShape>> = taughtShapesStore.shapes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Readings produced by a taught shape, keyed by the unrecognized message they came from. */
    val matchedTransactions: StateFlow<Map<Long, ParsedTransaction>> =
        combine(unrecognizedMessages, taughtShapes) { messages, shapes ->
            if (shapes.isEmpty()) {
                emptyMap<Long, ParsedTransaction>()
            } else {
                messages.mapNotNull { message ->
                    readWithTaughtShape(message, shapes)?.let { message.id to it }
                }.toMap()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap<Long, ParsedTransaction>()
        )

    /** The message the teaching sheet is open for, if any. */
    private val _teachTarget = MutableStateFlow<UnrecognizedSmsEntity?>(null)
    val teachTarget: StateFlow<UnrecognizedSmsEntity?> = _teachTarget.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun startTeaching(message: UnrecognizedSmsEntity) {
        _teachTarget.value = message
    }

    fun cancelTeaching() {
        _teachTarget.value = null
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun toggleShowReported() {
        _showReported.value = !_showReported.value
    }
    
    fun reportMessage(message: UnrecognizedSmsEntity) {
        viewModelScope.launch {
            try {
                val issueTitle = "[Unrecognized SMS] From: ${message.sender}"
                val issueBody = """
                    ### Unrecognized SMS Details
                    - **Sender:** ${message.sender}
                    - **Date:** ${message.receivedAt}
                    
                    ### Original SMS
                    ```
                    ${message.smsBody}
                    ```
                    
                    ### Expected Behavior
                    _Describe how this SMS should have been parsed (e.g., this is a transaction of 500 INR from HDFC bank)_
                """.trimIndent()

                val encodedTitle = URLEncoder.encode(issueTitle, "UTF-8")
                val encodedBody = URLEncoder.encode(issueBody, "UTF-8")

                val url = "https://github.com/Prarambha369/HisabSathi/issues/new?title=$encodedTitle&body=$encodedBody"
                
                // Open in browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                
                // Mark as reported
                unrecognizedSmsRepository.markAsReported(listOf(message.id))
                
                if (BuildConfig.DEBUG) Log.d("UnrecognizedSmsViewModel", "Opened report for message from: ${message.sender}")
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e("UnrecognizedSmsViewModel", "Error opening report", e)
            }
        }
    }
    
    fun deleteMessage(message: UnrecognizedSmsEntity) {
        viewModelScope.launch {
            try {
                // Delete the specific message
                unrecognizedSmsRepository.deleteMessage(message.id)
                if (BuildConfig.DEBUG) Log.d("UnrecognizedSmsViewModel", "Deleted message from: ${message.sender}")
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e("UnrecognizedSmsViewModel", "Error deleting message", e)
            }
        }
    }
    
    fun deleteAllMessages() {
        viewModelScope.launch {
            try {
                unrecognizedSmsRepository.deleteAll()
                if (BuildConfig.DEBUG) Log.d("UnrecognizedSmsViewModel", "Deleted all unrecognized messages")
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e("UnrecognizedSmsViewModel", "Error deleting all messages", e)
            }
        }
    }

    /**
     * Stores a shape from the parts the user tapped.
     *
     * Nothing is recorded as a transaction here: the shape only lets later messages from this
     * sender be read, and each reading still has to be confirmed on the screen.
     */
    fun saveTaughtShape(
        amount: IntRange,
        merchant: IntRange?,
        account: IntRange?,
        direction: TransactionType,
        currency: String
    ) {
        val message = _teachTarget.value ?: return
        viewModelScope.launch {
            val shape = SmsShapes.learn(
                id = "${message.id}-${System.currentTimeMillis()}",
                senderKey = message.sender,
                example = message.smsBody,
                taps = TapChoice(amount = amount, merchant = merchant, account = account),
                direction = direction,
                bankLabel = message.sender,
                currency = currency
            )
            _teachTarget.value = null
            if (shape == null) {
                _userMessage.value = "That message can't be taught. Try another amount."
            } else {
                taughtShapesStore.add(shape)
                _userMessage.value = "Shape saved. Matching messages are now suggested."
            }
        }
    }

    /**
     * Saves a reading the user confirmed. The message leaves the unrecognized list once it is
     * either recorded or already present, so the same suggestion is not offered again.
     */
    fun addMatchedTransaction(message: UnrecognizedSmsEntity, parsed: ParsedTransaction) {
        viewModelScope.launch {
            val result = smsTransactionProcessor.saveParsedTransaction(parsed, message.smsBody)
            when {
                result.success -> {
                    unrecognizedSmsRepository.deleteMessage(message.id)
                    _userMessage.value = "Transaction added"
                }
                result.reason == "Duplicate transaction" ||
                    result.reason == "Transaction was previously deleted" -> {
                    unrecognizedSmsRepository.deleteMessage(message.id)
                    _userMessage.value = "Already in your transactions"
                }
                else -> _userMessage.value = result.reason ?: "Could not add that transaction"
            }
        }
    }

    private fun readWithTaughtShape(
        message: UnrecognizedSmsEntity,
        shapes: List<TaughtShape>
    ): ParsedTransaction? = try {
        SmsShapes.readConsistent(
            shapes = shapes,
            message = message.smsBody,
            sender = message.sender,
            timestamp = message.receivedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
    } catch (e: Exception) {
        if (BuildConfig.DEBUG) Log.e("UnrecognizedSmsViewModel", "Error reading a taught shape", e)
        null
    }
}
