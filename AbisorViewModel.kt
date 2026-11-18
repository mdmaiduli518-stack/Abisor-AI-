package com.example.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.AppSettings
import com.example.data.AppSettingsRepository
import com.example.data.BangladeshKnowledge
import com.example.data.GeminiAiRepository
import com.example.data.KnowledgeTopic
import com.example.data.ModelTier
import com.example.data.local.AppDatabase
import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.StudyPlanDao
import com.example.data.local.StudyPlanRowEntity
import com.example.data.local.seedInitialWeekIfEmpty
import com.example.data.remote.FirestoreChatSyncRepository
import com.example.firebase.authStateFlow
import com.example.i18n.AppLanguage
import com.example.i18n.BengaliStrings
import com.example.i18n.EnglishStrings
import com.example.util.MediaAttachmentHelper
import com.example.util.ProcessedAttachment
import com.example.util.VoiceHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

private const val TAG = "AbisorViewModel"

enum class AbisorTab {
    CHAT,
    STUDY_PLAN,
    SETTINGS
}

@OptIn(ExperimentalCoroutinesApi::class)
class AbisorViewModel(
    private val appContext: Context,
    private val chatDao: ChatDao,
    private val studyPlanDao: StudyPlanDao,
    private val settingsRepo: AppSettingsRepository,
    private val geminiRepo: GeminiAiRepository,
    private val cloudSyncRepo: FirestoreChatSyncRepository,
    private val voiceHelper: VoiceHelper,
    private val auth: FirebaseAuth = Firebase.auth
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepo.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = AppSettings()
        )

    private val _selectedTab = MutableStateFlow(AbisorTab.CHAT)
    val selectedTab: StateFlow<AbisorTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val chatSessions: StateFlow<List<ChatSessionEntity>> = _searchQuery
        .flatMapLatest { query ->
            val trimmed = query.trim()
            if (trimmed.isEmpty()) chatDao.observeAllSessions()
            else chatDao.searchSessions(trimmed)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _currentChatId = MutableStateFlow<String?>(null)
    val currentChatId: StateFlow<String?> = _currentChatId.asStateFlow()

    val currentMessages: StateFlow<List<ChatMessageEntity>> = _currentChatId
        .flatMapLatest { chatId ->
            if (chatId == null) flowOf(emptyList())
            else chatDao.observeMessages(chatId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val studyPlanRows: StateFlow<List<StudyPlanRowEntity>> = studyPlanDao.observeAllRows()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val currentUser: StateFlow<FirebaseUser?> = auth.authStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = auth.currentUser
        )

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _pendingAttachment = MutableStateFlow<ProcessedAttachment?>(null)
    val pendingAttachment: StateFlow<ProcessedAttachment?> = _pendingAttachment.asStateFlow()

    private val _isImageCreationMode = MutableStateFlow(false)
    val isImageCreationMode: StateFlow<Boolean> = _isImageCreationMode.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage: StateFlow<Boolean> = _isGeneratingImage.asStateFlow()

    private val _isSyncingCloud = MutableStateFlow(false)
    val isSyncingCloud: StateFlow<Boolean> = _isSyncingCloud.asStateFlow()

    private val _showLoginDialog = MutableStateFlow(false)
    val showLoginDialog: StateFlow<Boolean> = _showLoginDialog.asStateFlow()

    val speakingMessageId: StateFlow<String?> = voiceHelper.speakingMessageId
    val isListening: StateFlow<Boolean> = voiceHelper.isListening

    private val _uiMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val uiMessages: SharedFlow<String> = _uiMessages.asSharedFlow()

    private var cloudObserverJob: Job? = null
    private var generationJob: Job? = null

    init {
        viewModelScope.launch {
            val sessions = chatDao.getAllSessionsOnce()
            if (sessions.isEmpty()) {
                createNewChat()
            } else {
                _currentChatId.value = sessions.first().id
            }
        }

        viewModelScope.launch {
            val isBn = settings.value.language == AppLanguage.BENGALI
            studyPlanDao.seedInitialWeekIfEmpty(isBengali = isBn)
        }

        // Observe Firebase Auth changes cleanly; only start Firestore listener when signed in.
        viewModelScope.launch {
            currentUser
                .distinctUntilChanged { old, new -> old?.uid == new?.uid }
                .collectLatest { user ->
                    cloudObserverJob?.cancel()
                    cloudObserverJob = null
                    if (user != null) {
                        syncLocalChatsToCloud()
                        startCloudChatListener(user.uid)
                    }
                }
        }
    }

    private fun currentStrings() =
        if (settings.value.language == AppLanguage.BENGALI) BengaliStrings else EnglishStrings

    private fun startCloudChatListener(uid: String) {
        cloudObserverJob = viewModelScope.launch {
            cloudSyncRepo.observeCloudSessions(uid)
                .catch { error ->
                    Log.w(TAG, "Cloud chat sync stream ended: ${error.message}")
                }
                .collect { cloudSessions ->
                    for (remote in cloudSessions) {
                        val local = chatDao.getSessionById(remote.id)
                        if (local == null || remote.updatedAtMs > local.updatedAtMs) {
                            chatDao.upsertSession(
                                ChatSessionEntity(
                                    id = remote.id,
                                    title = remote.title,
                                    lastMessagePreview = remote.lastMessagePreview,
                                    createdAtMs = remote.createdAtMs,
                                    updatedAtMs = remote.updatedAtMs
                                )
                            )
                            val msgsResult = cloudSyncRepo.fetchCloudMessages(uid, remote.id)
                            msgsResult.getOrNull()?.let { remoteMsgs ->
                                val entities = remoteMsgs.map { rm ->
                                    ChatMessageEntity(
                                        id = rm.id,
                                        chatId = remote.id,
                                        role = rm.role,
                                        text = rm.text,
                                        attachmentPath = null,
                                        attachmentName = rm.attachmentName,
                                        attachmentType = rm.attachmentType,
                                        isError = false,
                                        timestampMs = rm.timestampMs
                                    )
                                }
                                if (entities.isNotEmpty()) {
                                    chatDao.upsertMessages(entities)
                                }
                            }
                        }
                    }
                }
        }
    }

    fun selectTab(tab: AbisorTab) {
        _selectedTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun setShowLoginDialog(show: Boolean) {
        _showLoginDialog.value = show
    }

    fun toggleImageCreationMode() {
        _isImageCreationMode.value = !_isImageCreationMode.value
    }

    fun setImageCreationMode(enabled: Boolean) {
        _isImageCreationMode.value = enabled
    }

    fun selectChat(chatId: String) {
        _currentChatId.value = chatId
        _selectedTab.value = AbisorTab.CHAT
    }

    fun createNewChat() {
        viewModelScope.launch {
            val strings = currentStrings()
            val now = System.currentTimeMillis()
            val id = "chat_${now}_${UUID.randomUUID().toString().take(6)}"
            val session = ChatSessionEntity(
                id = id,
                title = strings.newChatTitle,
                lastMessagePreview = strings.welcomeGreeting,
                createdAtMs = now,
                updatedAtMs = now
            )
            val welcomeMsg = ChatMessageEntity(
                id = "msg_${now}_welcome",
                chatId = id,
                role = "bot",
                text = strings.welcomeGreeting,
                timestampMs = now
            )
            chatDao.upsertSession(session)
            chatDao.upsertMessage(welcomeMsg)
            _currentChatId.value = id
            _selectedTab.value = AbisorTab.CHAT
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            chatDao.deleteSessionById(chatId)
            if (currentUser.value != null) {
                cloudSyncRepo.deleteCloudChat(chatId)
            }
            if (_currentChatId.value == chatId) {
                val remaining = chatDao.getAllSessionsOnce()
                if (remaining.isEmpty()) {
                    createNewChat()
                } else {
                    _currentChatId.value = remaining.first().id
                }
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            voiceHelper.stopSpeaking()
            chatDao.clearAllSessions()
            if (currentUser.value != null) {
                cloudSyncRepo.clearAllCloudChats()
            }
            createNewChat()
            _uiMessages.tryEmit(currentStrings().historyClearedToast)
        }
    }

    fun attachImage(uri: Uri) {
        viewModelScope.launch {
            val result = MediaAttachmentHelper.processImageUri(appContext, uri)
            result.onSuccess { processed ->
                _pendingAttachment.value = processed
                _isImageCreationMode.value = false
            }.onFailure { err ->
                _uiMessages.tryEmit(err.localizedMessage ?: currentStrings().connectionError)
            }
        }
    }

    fun attachDocument(uri: Uri) {
        viewModelScope.launch {
            val result = MediaAttachmentHelper.processDocumentUri(appContext, uri)
            result.onSuccess { processed ->
                _pendingAttachment.value = processed
                _isImageCreationMode.value = false
            }.onFailure { err ->
                _uiMessages.tryEmit(err.localizedMessage ?: currentStrings().connectionError)
            }
        }
    }

    fun clearAttachment() {
        _pendingAttachment.value = null
    }

    fun sendMessage(overridePrompt: String? = null) {
        val rawInput = (overridePrompt ?: _inputText.value).trim()
        val attachment = _pendingAttachment.value
        if (rawInput.isEmpty() && attachment == null) return
        if (_isGenerating.value) return

        val strings = currentStrings()
        val isCreateImg = _isImageCreationMode.value && attachment == null

        val promptText = when {
            rawInput.isNotEmpty() -> rawInput
            attachment?.type == "image" -> strings.imageDefaultPrompt
            else -> strings.fileDefaultPrompt
        }

        if (overridePrompt == null) {
            _inputText.value = ""
        }
        _pendingAttachment.value = null

        generationJob = viewModelScope.launch {
            val chatId = _currentChatId.value ?: run {
                createNewChat()
                _currentChatId.value
            } ?: return@launch

            val existingSession = chatDao.getSessionById(chatId)
            val now = System.currentTimeMillis()
            val isDefaultTitle = existingSession == null ||
                existingSession.title == BengaliStrings.newChatTitle ||
                existingSession.title == EnglishStrings.newChatTitle
            val updatedTitle = if (isDefaultTitle) promptText.take(36) else existingSession!!.title

            val userMsg = ChatMessageEntity(
                id = "msg_${now}_u",
                chatId = chatId,
                role = "me",
                text = promptText,
                attachmentPath = attachment?.localPath,
                attachmentName = attachment?.displayName,
                attachmentType = attachment?.type,
                timestampMs = now
            )

            val botMsgId = "msg_${now + 1}_b"
            val placeholderBotMsg = ChatMessageEntity(
                id = botMsgId,
                chatId = chatId,
                role = "bot",
                text = "",
                timestampMs = now + 1
            )

            val historyBeforeSend = chatDao.getMessagesOnce(chatId)
            chatDao.upsertMessage(userMsg)
            chatDao.upsertMessage(placeholderBotMsg)

            val updatedSession = ChatSessionEntity(
                id = chatId,
                title = updatedTitle,
                lastMessagePreview = promptText.take(120),
                createdAtMs = existingSession?.createdAtMs ?: now,
                updatedAtMs = now + 1
            )
            chatDao.upsertSession(updatedSession)

            _isGenerating.value = true
            _isGeneratingImage.value = isCreateImg

            try {
                if (isCreateImg) {
                    val imgResult = geminiRepo.generateImage(promptText)
                    imgResult.onSuccess { bitmap ->
                        val savedPath = MediaAttachmentHelper.saveGeneratedBitmap(appContext, bitmap)
                        val replyText = if (settings.value.language == AppLanguage.BENGALI) {
                            "আপনার বর্ণনা অনুযায়ী ছবি তৈরি করা হয়েছে:"
                        } else {
                            "Here is your generated image:"
                        }
                        val finalBotMsg = placeholderBotMsg.copy(
                            text = replyText,
                            attachmentPath = savedPath,
                            attachmentName = "abisor_image.png",
                            attachmentType = "generated_image",
                            isError = false
                        )
                        chatDao.updateMessageContent(
                            messageId = botMsgId,
                            text = finalBotMsg.text,
                            attachmentPath = savedPath,
                            attachmentType = "generated_image",
                            isError = false
                        )
                        val doneSession = updatedSession.copy(
                            lastMessagePreview = replyText,
                            updatedAtMs = System.currentTimeMillis()
                        )
                        chatDao.upsertSession(doneSession)
                        if (currentUser.value != null) {
                            cloudSyncRepo.syncSessionAndMessages(doneSession, listOf(userMsg, finalBotMsg))
                        }
                    }.onFailure { err ->
                        val msg = formatFriendlyError(err, strings)
                        chatDao.updateMessageContent(botMsgId, msg, null, null, true)
                    }
                } else {
                    val accumulated = StringBuilder()
                    geminiRepo.streamReply(
                        tier = settings.value.modelTier,
                        historyMessages = historyBeforeSend,
                        userPrompt = promptText,
                        attachment = attachment?.aiAttachment
                    ).collect { partial ->
                        accumulated.clear()
                        accumulated.append(partial)
                        chatDao.updateMessageContent(
                            messageId = botMsgId,
                            text = partial,
                            attachmentPath = null,
                            attachmentType = null,
                            isError = false
                        )
                    }

                    val finalText = accumulated.toString().trim()
                    if (finalText.isBlank()) {
                        chatDao.updateMessageContent(
                            messageId = botMsgId,
                            text = strings.emptyReplyError,
                            attachmentPath = null,
                            attachmentType = null,
                            isError = true
                        )
                    } else {
                        val finalBotMsg = placeholderBotMsg.copy(text = finalText, isError = false)
                        val doneSession = updatedSession.copy(
                            lastMessagePreview = finalText.take(120),
                            updatedAtMs = System.currentTimeMillis()
                        )
                        chatDao.upsertSession(doneSession)
                        if (currentUser.value != null) {
                            cloudSyncRepo.syncSessionAndMessages(doneSession, listOf(userMsg, finalBotMsg))
                        }
                        if (settings.value.autoReadReplies) {
                            voiceHelper.speak(botMsgId, finalText) {
                                _uiMessages.tryEmit(strings.ttsUnavailable)
                            }
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Gemini timeout", e)
                chatDao.updateMessageContent(botMsgId, strings.timeoutError, null, null, true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Gemini error", e)
                val friendly = formatFriendlyError(e, strings)
                chatDao.updateMessageContent(botMsgId, friendly, null, null, true)
            } finally {
                _isGenerating.value = false
                _isGeneratingImage.value = false
            }
        }
    }

    private fun formatFriendlyError(err: Throwable, strings: com.example.i18n.AppStrings): String {
        val detail = err.localizedMessage?.take(160)
        return if (detail.isNullOrBlank()) strings.connectionError
        else "${strings.connectionError}\n($detail)"
    }

    fun sendBangladeshKnowledgeTopic(topic: KnowledgeTopic) {
        val lang = settings.value.language
        val question = BangladeshKnowledge.getQuestion(topic, lang)
        val answer = BangladeshKnowledge.getAnswer(topic, lang)

        viewModelScope.launch {
            val chatId = _currentChatId.value ?: run {
                createNewChat()
                _currentChatId.value
            } ?: return@launch

            val existingSession = chatDao.getSessionById(chatId)
            val now = System.currentTimeMillis()
            val isDefaultTitle = existingSession == null ||
                existingSession.title == BengaliStrings.newChatTitle ||
                existingSession.title == EnglishStrings.newChatTitle
            val updatedTitle = if (isDefaultTitle) BangladeshKnowledge.getChipLabel(topic, lang) else existingSession!!.title

            val userMsg = ChatMessageEntity(
                id = "msg_${now}_ku",
                chatId = chatId,
                role = "me",
                text = question,
                timestampMs = now
            )
            val botMsg = ChatMessageEntity(
                id = "msg_${now + 1}_kb",
                chatId = chatId,
                role = "bot",
                text = answer,
                timestampMs = now + 1
            )
            val updatedSession = ChatSessionEntity(
                id = chatId,
                title = updatedTitle,
                lastMessagePreview = answer.take(120),
                createdAtMs = existingSession?.createdAtMs ?: now,
                updatedAtMs = now + 1
            )
            chatDao.upsertSession(updatedSession)
            chatDao.upsertMessages(listOf(userMsg, botMsg))
            if (currentUser.value != null) {
                cloudSyncRepo.syncSessionAndMessages(updatedSession, listOf(userMsg, botMsg))
            }
            if (settings.value.autoReadReplies) {
                voiceHelper.speak(botMsg.id, answer) {
                    _uiMessages.tryEmit(currentStrings().ttsUnavailable)
                }
            }
        }
    }

    fun downloadGeneratedImage(filePath: String) {
        viewModelScope.launch {
            val strings = currentStrings()
            val result = MediaAttachmentHelper.saveImageToGallery(appContext, filePath)
            result.onSuccess {
                _uiMessages.tryEmit(strings.imageSavedSuccess)
            }.onFailure {
                _uiMessages.tryEmit(strings.imageSaveError)
            }
        }
    }

    fun toggleSpeakMessage(messageId: String, text: String) {
        voiceHelper.speak(messageId, text) {
            _uiMessages.tryEmit(currentStrings().ttsUnavailable)
        }
    }

    fun toggleVoiceInput() {
        voiceHelper.toggleListening(
            onPartialOrFinalResult = { recognized ->
                _inputText.value = recognized
            },
            onError = {
                _uiMessages.tryEmit(currentStrings().micUnavailable)
            }
        )
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            settingsRepo.setLanguage(language)
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.setDarkTheme(enabled)
        }
    }

    fun toggleAutoRead() {
        viewModelScope.launch {
            val next = !settings.value.autoReadReplies
            settingsRepo.setAutoReadReplies(next)
            if (!next) voiceHelper.stopSpeaking()
        }
    }

    fun setAutoRead(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.setAutoReadReplies(enabled)
            if (!enabled) voiceHelper.stopSpeaking()
        }
    }

    fun setModelTier(tier: ModelTier) {
        viewModelScope.launch {
            settingsRepo.setModelTier(tier)
            if (tier == ModelTier.SMART) {
                _uiMessages.tryEmit(currentStrings().premiumNotice)
            }
        }
    }

    fun updateStudyPlanRow(row: StudyPlanRowEntity) {
        viewModelScope.launch {
            studyPlanDao.upsertRow(row)
        }
    }

    fun addStudyPlanDay() {
        viewModelScope.launch {
            val existing = studyPlanRows.value
            val nextIndex = (existing.maxOfOrNull { it.sortOrder } ?: -1) + 1
            val date = LocalDate.now().plusDays(nextIndex.toLong())
            val isBn = settings.value.language == AppLanguage.BENGALI
            val formatter = DateTimeFormatter.ofPattern(
                "dd MMM (EEE)",
                if (isBn) Locale("bn", "BD") else Locale.ENGLISH
            )
            val isFriday = date.dayOfWeek == DayOfWeek.FRIDAY
            studyPlanDao.upsertRow(
                StudyPlanRowEntity(
                    id = "day_${date}_${System.currentTimeMillis()}",
                    sortOrder = nextIndex,
                    dateLabel = date.format(formatter),
                    dayOfWeekIso = date.dayOfWeek.value,
                    subject = if (isFriday) (if (isBn) "সাপ্তাহিক ছুটি" else "Weekend Off") else (if (isBn) "নতুন বিষয়" else "New Subject"),
                    chapter = if (isFriday) (if (isBn) "বিশ্রাম" else "Rest") else (if (isBn) "অধ্যায় ১" else "Chapter 1"),
                    hours = if (isFriday) "0" else "2",
                    offDay = isFriday
                )
            )
        }
    }

    fun syncLocalChatsToCloud() {
        if (currentUser.value == null) return
        viewModelScope.launch {
            _isSyncingCloud.value = true
            try {
                val sessions = chatDao.getAllSessionsOnce()
                for (session in sessions) {
                    val msgs = chatDao.getMessagesOnce(session.id)
                    cloudSyncRepo.syncSessionAndMessages(session, msgs)
                }
            } finally {
                _isSyncingCloud.value = false
            }
        }
    }

    suspend fun prepareSignOutCleanup() {
        cloudObserverJob?.cancel()
        cloudObserverJob = null
    }

    fun emitNotice(message: String) {
        _uiMessages.tryEmit(message)
    }

    override fun onCleared() {
        super.onCleared()
        voiceHelper.release()
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = context.applicationContext
                val db = AppDatabase.getInstance(app)
                val firestore = FirebaseFirestore.getInstance(app.getString(R.string.firestore_database_id))
                AbisorViewModel(
                    appContext = app,
                    chatDao = db.chatDao(),
                    studyPlanDao = db.studyPlanDao(),
                    settingsRepo = AppSettingsRepository(app),
                    geminiRepo = GeminiAiRepository(),
                    cloudSyncRepo = FirestoreChatSyncRepository(firestore),
                    voiceHelper = VoiceHelper(app)
                )
            }
        }
    }
}
