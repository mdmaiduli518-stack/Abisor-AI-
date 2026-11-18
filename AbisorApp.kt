package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ModelTier
import com.example.firebase.attemptSilentGoogleSignIn
import com.example.i18n.AppLanguage
import com.example.i18n.BengaliStrings
import com.example.i18n.EnglishStrings
import com.example.i18n.LocalAppStrings
import com.example.ui.components.ChatScreen
import com.example.ui.components.HistoryDrawerContent
import com.example.ui.components.LoginDialog
import com.example.ui.components.SettingsScreen
import com.example.ui.components.StudyPlannerScreen
import com.example.ui.theme.AbisorTheme
import com.example.ui.theme.DeepGreen
import com.example.ui.theme.PremiumAmberBg
import com.example.ui.theme.PremiumAmberText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbisorApp() {
    val context = LocalContext.current
    val viewModel: AbisorViewModel = viewModel(factory = AbisorViewModel.provideFactory(context))

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val sessions by viewModel.chatSessions.collectAsStateWithLifecycle()
    val currentChatId by viewModel.currentChatId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val messages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val studyRows by viewModel.studyPlanRows.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val pendingAttachment by viewModel.pendingAttachment.collectAsStateWithLifecycle()
    val isImageCreationMode by viewModel.isImageCreationMode.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val isGeneratingImage by viewModel.isGeneratingImage.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val speakingMessageId by viewModel.speakingMessageId.collectAsStateWithLifecycle()
    val showLoginDialog by viewModel.showLoginDialog.collectAsStateWithLifecycle()
    val isSyncingCloud by viewModel.isSyncingCloud.collectAsStateWithLifecycle()

    val strings = if (settings.language == AppLanguage.BENGALI) BengaliStrings else EnglishStrings
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Non-blocking silent Google Sign-In check if user previously signed in
    LaunchedEffect(Unit) {
        val cm = CredentialManager.create(context)
        attemptSilentGoogleSignIn(context, cm, scope) {}
    }

    LaunchedEffect(Unit) {
        viewModel.uiMessages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    AbisorTheme(darkTheme = settings.isDarkTheme) {
        CompositionLocalProvider(LocalAppStrings provides strings) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    HistoryDrawerContent(
                        sessions = sessions,
                        currentChatId = currentChatId,
                        searchQuery = searchQuery,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onNewChat = {
                            viewModel.createNewChat()
                            scope.launch { drawerState.close() }
                        },
                        onSelectChat = { chatId ->
                            viewModel.selectChat(chatId)
                            scope.launch { drawerState.close() }
                        },
                        onDeleteChat = viewModel::deleteChat
                    )
                }
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val useNavigationRail = maxWidth >= 840.dp

                    Row(modifier = Modifier.fillMaxSize()) {
                        if (useNavigationRail) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Spacer(modifier = Modifier.weight(1f))
                                AbisorTab.entries.forEach { tab ->
                                    val selected = selectedTab == tab
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { viewModel.selectTab(tab) },
                                        icon = { TabIcon(tab, selected) },
                                        label = { Text(tabLabel(tab, strings)) },
                                        modifier = Modifier.testTag("nav_rail_${tab.name.lowercase()}")
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }

                        Scaffold(
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.background,
                            contentWindowInsets = WindowInsets.safeDrawing,
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            topBar = {
                                AbisorTopBar(
                                    modelTier = settings.modelTier,
                                    language = settings.language,
                                    autoRead = settings.autoReadReplies,
                                    isSignedIn = currentUser != null,
                                    onOpenDrawer = { scope.launch { drawerState.open() } },
                                    onSelectModelTier = viewModel::setModelTier,
                                    onToggleLanguage = {
                                        val next = if (settings.language == AppLanguage.BENGALI) {
                                            AppLanguage.ENGLISH
                                        } else {
                                            AppLanguage.BENGALI
                                        }
                                        viewModel.setLanguage(next)
                                    },
                                    onOpenLogin = { viewModel.setShowLoginDialog(true) },
                                    onToggleAutoRead = viewModel::toggleAutoRead
                                )
                            },
                            bottomBar = {
                                if (!useNavigationRail) {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ) {
                                        AbisorTab.entries.forEach { tab ->
                                            val selected = selectedTab == tab
                                            NavigationBarItem(
                                                selected = selected,
                                                onClick = { viewModel.selectTab(tab) },
                                                icon = { TabIcon(tab, selected) },
                                                label = { Text(tabLabel(tab, strings)) },
                                                modifier = Modifier.testTag("bottom_nav_${tab.name.lowercase()}")
                                            )
                                        }
                                    }
                                }
                            }
                        ) { innerPadding ->
                            when (selectedTab) {
                                AbisorTab.CHAT -> ChatScreen(
                                    messages = messages,
                                    inputText = inputText,
                                    pendingAttachment = pendingAttachment,
                                    isImageCreationMode = isImageCreationMode,
                                    isGenerating = isGenerating,
                                    isGeneratingImage = isGeneratingImage,
                                    isListening = isListening,
                                    speakingMessageId = speakingMessageId,
                                    isDarkTheme = settings.isDarkTheme,
                                    language = settings.language,
                                    onInputChange = viewModel::updateInputText,
                                    onSend = { viewModel.sendMessage() },
                                    onAttachImageUri = viewModel::attachImage,
                                    onAttachDocumentUri = viewModel::attachDocument,
                                    onClearAttachment = viewModel::clearAttachment,
                                    onToggleImageMode = viewModel::toggleImageCreationMode,
                                    onToggleVoiceInput = viewModel::toggleVoiceInput,
                                    onSpeakMessage = viewModel::toggleSpeakMessage,
                                    onSelectKnowledgeTopic = viewModel::sendBangladeshKnowledgeTopic,
                                    onAskGeminiMore = { snippet ->
                                        val prompt = if (settings.language == AppLanguage.BENGALI) {
                                            "এই বিষয়ে আরও বিস্তারিত বলো: $snippet"
                                        } else {
                                            "Tell me more details about: $snippet"
                                        }
                                        viewModel.sendMessage(prompt)
                                    },
                                    onDownloadGeneratedImage = viewModel::downloadGeneratedImage,
                                    onNotify = viewModel::emitNotice,
                                    modifier = Modifier.padding(innerPadding)
                                )

                                AbisorTab.STUDY_PLAN -> StudyPlannerScreen(
                                    rows = studyRows,
                                    isDarkTheme = settings.isDarkTheme,
                                    onUpdateRow = viewModel::updateStudyPlanRow,
                                    onAddDay = viewModel::addStudyPlanDay,
                                    modifier = Modifier.padding(innerPadding)
                                )

                                AbisorTab.SETTINGS -> SettingsScreen(
                                    settings = settings,
                                    currentUser = currentUser,
                                    isSyncing = isSyncingCloud,
                                    onToggleDarkTheme = viewModel::setDarkTheme,
                                    onSelectLanguage = viewModel::setLanguage,
                                    onToggleAutoRead = viewModel::setAutoRead,
                                    onOpenLogin = { viewModel.setShowLoginDialog(true) },
                                    onClearAllHistory = viewModel::clearAllHistory,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }

            if (showLoginDialog) {
                LoginDialog(
                    currentUser = currentUser,
                    isSyncing = isSyncingCloud,
                    onDismiss = { viewModel.setShowLoginDialog(false) },
                    onPrepareSignOut = { viewModel.prepareSignOutCleanup() },
                    onSyncNow = viewModel::syncLocalChatsToCloud
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AbisorTopBar(
    modelTier: ModelTier,
    language: AppLanguage,
    autoRead: Boolean,
    isSignedIn: Boolean,
    onOpenDrawer: () -> Unit,
    onSelectModelTier: (ModelTier) -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenLogin: () -> Unit,
    onToggleAutoRead: () -> Unit
) {
    val strings = LocalAppStrings.current
    var showModelMenu by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DeepGreen,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        navigationIcon = {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("hamburger_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = strings.menuDescription
                )
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = strings.appName,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                // Model dropdown: Fast vs Smart (Premium placeholder)
                Box {
                    Surface(
                        color = Color.White.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showModelMenu = true }
                            .testTag("model_dropdown_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (modelTier == ModelTier.FAST) strings.modelFastLabel
                                else strings.modelSmartLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                            if (modelTier == ModelTier.SMART) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = PremiumAmberBg,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = strings.premiumBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PremiumAmberText,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showModelMenu,
                        onDismissRequest = { showModelMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(strings.modelFastLabel) },
                            onClick = {
                                showModelMenu = false
                                onSelectModelTier(ModelTier.FAST)
                            },
                            modifier = Modifier.testTag("model_option_fast")
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(strings.modelSmartLabel)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = PremiumAmberBg,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = strings.premiumBadge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PremiumAmberText,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                showModelMenu = false
                                onSelectModelTier(ModelTier.SMART)
                            },
                            modifier = Modifier.testTag("model_option_smart")
                        )
                    }
                }
            }
        },
        actions = {
            // Quick Language toggle pill (বাং / EN)
            Surface(
                color = Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onToggleLanguage)
                    .testTag("language_toggle_button")
            ) {
                Text(
                    text = if (language == AppLanguage.BENGALI) "বাং" else "EN",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }

            IconButton(
                onClick = onOpenLogin,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("login_top_button")
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = strings.loginButtonDescription
                    )
                    if (isSignedIn) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF4ADE80))
                        )
                    }
                }
            }

            IconButton(
                onClick = onToggleAutoRead,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("auto_read_toggle_button")
            ) {
                Icon(
                    imageVector = if (autoRead) Icons.Outlined.VolumeUp else Icons.Outlined.VolumeOff,
                    contentDescription = if (autoRead) strings.autoReadOn else strings.autoReadOff
                )
            }
        }
    )
}

@Composable
private fun TabIcon(tab: AbisorTab, selected: Boolean) {
    val icon = when (tab) {
        AbisorTab.CHAT -> if (selected) Icons.Rounded.ChatBubble else Icons.Outlined.ChatBubbleOutline
        AbisorTab.STUDY_PLAN -> if (selected) Icons.Rounded.CalendarMonth else Icons.Outlined.CalendarMonth
        AbisorTab.SETTINGS -> if (selected) Icons.Rounded.Settings else Icons.Outlined.Settings
    }
    Icon(imageVector = icon, contentDescription = null)
}

private fun tabLabel(tab: AbisorTab, strings: com.example.i18n.AppStrings): String = when (tab) {
    AbisorTab.CHAT -> strings.chatTab
    AbisorTab.STUDY_PLAN -> strings.studyPlanTab
    AbisorTab.SETTINGS -> strings.settingsTab
}
