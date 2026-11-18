package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.BangladeshKnowledge
import com.example.data.KnowledgeTopic
import com.example.data.local.ChatMessageEntity
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.DarkBotBubble
import com.example.ui.theme.DarkUserBubble
import com.example.ui.theme.MintBubble
import com.example.ui.theme.PremiumAmberBg
import com.example.ui.theme.PremiumAmberText
import com.example.util.MediaAttachmentHelper
import com.example.util.ProcessedAttachment
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    messages: List<ChatMessageEntity>,
    inputText: String,
    pendingAttachment: ProcessedAttachment?,
    isImageCreationMode: Boolean,
    isGenerating: Boolean,
    isGeneratingImage: Boolean,
    isListening: Boolean,
    speakingMessageId: String?,
    isDarkTheme: Boolean,
    language: AppLanguage,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachImageUri: (Uri) -> Unit,
    onAttachDocumentUri: (Uri) -> Unit,
    onClearAttachment: () -> Unit,
    onToggleImageMode: () -> Unit,
    onToggleVoiceInput: () -> Unit,
    onSpeakMessage: (String, String) -> Unit,
    onSelectKnowledgeTopic: (KnowledgeTopic) -> Unit,
    onAskGeminiMore: (String) -> Unit,
    onDownloadGeneratedImage: (String) -> Unit,
    onNotify: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var showPlusMenu by remember { mutableStateOf(false) }
    var showKnowledgeSheet by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onAttachImageUri(uri)
    }

    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) onAttachDocumentUri(uri)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        pendingCameraUri = null
        if (success && uri != null) {
            onAttachImageUri(uri)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val (uri, _) = MediaAttachmentHelper.createCameraImageUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            onNotify(strings.cameraPermissionNeeded)
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onToggleVoiceInput()
        } else {
            onNotify(strings.micPermissionNeeded)
        }
    }

    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Message stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = 820.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.size <= 1) {
                item(key = "bangladesh_knowledge_card") {
                    BangladeshQuickKnowledgeCard(
                        language = language,
                        onSelectTopic = onSelectKnowledgeTopic
                    )
                }
            }

            items(messages, key = { it.id }) { msg ->
                val isLast = msg.id == messages.lastOrNull()?.id
                val showTyping = msg.role == "bot" && msg.text.isBlank() && isGenerating && isLast
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(220)) +
                        slideInVertically(tween(220)) { it / 4 } +
                        scaleIn(initialScale = 0.96f, animationSpec = spring())
                ) {
                    ChatBubble(
                        message = msg,
                        showTypingIndicator = showTyping,
                        isGeneratingImage = showTyping && isGeneratingImage,
                        isSpeaking = speakingMessageId == msg.id,
                        isDarkTheme = isDarkTheme,
                        onSpeakClick = { onSpeakMessage(msg.id, msg.text) },
                        onDownloadImage = { path -> onDownloadGeneratedImage(path) },
                        onAskMoreClick = if (msg.id.endsWith("_kb")) {
                            { onAskGeminiMore(msg.text.take(80)) }
                        } else null
                    )
                }
            }
        }

        // Bottom controls container
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 820.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Mode / quick action strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = isImageCreationMode,
                        onClick = onToggleImageMode,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = strings.createImageModeTitle,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = PremiumAmberBg,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = strings.createImageCostBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PremiumAmberText,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("create_image_mode_chip")
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = { showKnowledgeSheet = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("open_knowledge_sheet_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MenuBook,
                            contentDescription = strings.bangladeshKnowledgeTitle,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Active Image Creation Cost Notice
                AnimatedVisibility(visible = isImageCreationMode) {
                    Text(
                        text = strings.createImageActiveHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = PremiumAmberText,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Pending Attachment Preview Bar
                AnimatedVisibility(visible = pendingAttachment != null) {
                    pendingAttachment?.let { att ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .testTag("pending_attachment_bar")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (att.type == "image" && att.localPath != null) {
                                    AsyncImage(
                                        model = File(att.localPath),
                                        contentDescription = att.displayName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (att.type == "pdf") Icons.Outlined.PictureAsPdf
                                        else Icons.Outlined.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${strings.attachedLabel}: ${att.displayName}",
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = strings.tapToRemove,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = onClearAttachment,
                                    modifier = Modifier.testTag("clear_attachment_button")
                                ) {
                                    Icon(Icons.Rounded.Close, contentDescription = strings.cancel)
                                }
                            }
                        }
                    }
                }

                // Listening Banner
                AnimatedVisibility(visible = isListening) {
                    Surface(
                        color = CrimsonAccent.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = strings.micListening,
                            style = MaterialTheme.typography.labelMedium,
                            color = CrimsonAccent,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Composer Row: [+] [Mic] [TextField] [Send]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledIconButton(
                        onClick = { showPlusMenu = true },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("plus_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = strings.plusButtonDescription
                        )
                    }

                    FilledIconButton(
                        onClick = {
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) onToggleVoiceInput()
                            else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isListening) CrimsonAccent
                            else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (isListening) Color.White
                            else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("mic_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = strings.micButtonDescription
                        )
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = onInputChange,
                        placeholder = {
                            Text(
                                text = if (isImageCreationMode) strings.describeImagePlaceholder
                                else strings.typeMessagePlaceholder,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { onSend() }),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field")
                    )

                    FilledIconButton(
                        onClick = onSend,
                        enabled = !isGenerating && (inputText.isNotBlank() || pendingAttachment != null),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = strings.send
                        )
                    }
                }
            }
        }
    }

    // Plus (+) Menu Bottom Sheet
    if (showPlusMenu) {
        ModalBottomSheet(
            onDismissRequest = { showPlusMenu = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = strings.plusMenuTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                ListItem(
                    headlineContent = { Text(strings.attachGallery) },
                    leadingContent = {
                        Icon(Icons.Outlined.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .clickable {
                            showPlusMenu = false
                            galleryPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("plus_attach_gallery")
                )

                ListItem(
                    headlineContent = { Text(strings.takePhoto) },
                    leadingContent = {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .clickable {
                            showPlusMenu = false
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) {
                                val (uri, _) = MediaAttachmentHelper.createCameraImageUri(context)
                                pendingCameraUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                        .testTag("plus_take_photo")
                )

                ListItem(
                    headlineContent = { Text(strings.attachFile) },
                    leadingContent = {
                        Icon(Icons.Outlined.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .clickable {
                            showPlusMenu = false
                            documentPicker.launch(arrayOf("application/pdf", "text/*"))
                        }
                        .testTag("plus_attach_file")
                )

                ListItem(
                    headlineContent = { Text(strings.toggleImageMode) },
                    supportingContent = { Text(strings.createImageCostBadge, color = PremiumAmberText) },
                    leadingContent = {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = PremiumAmberText)
                    },
                    modifier = Modifier
                        .clickable {
                            showPlusMenu = false
                            onToggleImageMode()
                        }
                        .testTag("plus_toggle_image_mode")
                )
            }
        }
    }

    // Bangladesh Knowledge Bottom Sheet
    if (showKnowledgeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showKnowledgeSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                BangladeshQuickKnowledgeCard(
                    language = language,
                    onSelectTopic = { topic ->
                        showKnowledgeSheet = false
                        onSelectKnowledgeTopic(topic)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BangladeshQuickKnowledgeCard(
    language: AppLanguage,
    onSelectTopic: (KnowledgeTopic) -> Unit
) {
    val strings = LocalAppStrings.current
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF42A41))
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = strings.bangladeshKnowledgeTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.bangladeshKnowledgeSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BangladeshKnowledge.topics.forEach { topic ->
                    AssistChip(
                        onClick = { onSelectTopic(topic) },
                        label = {
                            Text(
                                text = BangladeshKnowledge.getChipLabel(topic, language),
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.testTag("bd_topic_${topic.id}")
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessageEntity,
    showTypingIndicator: Boolean,
    isGeneratingImage: Boolean,
    isSpeaking: Boolean,
    isDarkTheme: Boolean,
    onSpeakClick: () -> Unit,
    onDownloadImage: (String) -> Unit,
    onAskMoreClick: (() -> Unit)?
) {
    val strings = LocalAppStrings.current
    val isUser = message.role == "me"

    val bubbleColor = when {
        isUser -> if (isDarkTheme) DarkUserBubble else MintBubble
        message.isError -> MaterialTheme.colorScheme.errorContainer
        else -> if (isDarkTheme) DarkBotBubble else MaterialTheme.colorScheme.surface
    }

    val bubbleShape = if (isUser) {
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = bubbleShape,
            shadowElevation = if (isUser) 1.dp else 2.dp,
            modifier = Modifier
                .widthIn(max = 340.dp)
                .testTag("chat_bubble_${message.id}")
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                // Attachment preview inside bubble
                val attPath = message.attachmentPath
                val attType = message.attachmentType
                if (attPath != null && (attType == "image" || attType == "generated_image")) {
                    AsyncImage(
                        model = File(attPath),
                        contentDescription = message.attachmentName ?: "Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp, max = 260.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else if (!message.attachmentName.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (attType == "pdf") Icons.Outlined.PictureAsPdf
                                else Icons.Outlined.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.attachmentName,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (showTypingIndicator) {
                    if (isGeneratingImage) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = strings.generatingImage,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        TypingDotsIndicator(label = strings.thinking)
                    }
                } else {
                    if (message.isError) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    } else if (message.text.isNotBlank()) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Download button for generated images
                    if (attType == "generated_image" && attPath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onDownloadImage(attPath) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("download_generated_image_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.downloadImage)
                        }
                    }

                    // Speaker button + Ask Gemini More for bot replies
                    if (!isUser && !message.isError && message.text.isNotBlank()) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = onSpeakClick,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("speak_button_${message.id}")
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Outlined.Stop
                                    else Icons.Outlined.VolumeUp,
                                    contentDescription = if (isSpeaking) strings.stopSpeaking
                                    else strings.speakAloud,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSpeaking) strings.stopSpeaking else strings.speakAloud,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            if (onAskMoreClick != null) {
                                OutlinedButton(
                                    onClick = onAskMoreClick,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = strings.askGeminiMore,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingDotsIndicator(label: String) {
    val transition = rememberInfiniteTransition(label = "typing_dots")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dot_phase"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(3) { index ->
            val active = (phase.toInt() % 3) == index
            Box(
                modifier = Modifier
                    .size(if (active) 9.dp else 7.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = if (active) 1f else 0.35f)
                    )
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
