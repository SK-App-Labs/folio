package com.folioledger.app.ui.settings.sheets

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.folioledger.app.BuildConfig
import com.folioledger.app.ui.theme.LocalFolioSemanticColors

private const val SUPPORT_EMAIL = "support.folioledger@gmail.com"

enum class FeedbackCategory(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val subjectTag: String,
    val defaultTopics: List<String>
) {
    BUG(
        title = "Bug Report",
        subtitle = "Something isn't working",
        emoji = "🐛",
        subjectTag = "Bug",
        defaultTopics = listOf(
            "Passbook & Balance",
            "CSV Import / Backup",
            "Biometric Lock",
            "Transaction Entry",
            "Charts & Analytics",
            "Scrolling Delay",
            "Other"
        )
    ),
    FEATURE(
        title = "Feature Idea",
        subtitle = "Suggest an addition",
        emoji = "💡",
        subjectTag = "Feature",
        defaultTopics = listOf(
            "New Currency Request",
            "Home Screen Widget",
            "More Charts & Trends",
            "Receipt Attachment",
            "Split Rules & Automation",
            "Theme Palette",
            "Other"
        )
    ),
    CURRENCY(
        title = "Currency / Format",
        subtitle = "ISO or decimal tweak",
        emoji = "💱",
        subjectTag = "Currency",
        defaultTopics = listOf(
            "Missing ISO Currency",
            "Exchange Rate Setting",
            "Lakh/Crore vs Million Format",
            "Decimal Precision",
            "Other"
        )
    ),
    GENERAL(
        title = "General Note",
        subtitle = "Praise or question",
        emoji = "💬",
        subjectTag = "Feedback",
        defaultTopics = listOf(
            "General Compliment",
            "App Navigation / UI",
            "Question on Accounting",
            "Other"
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedbackBottomSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var selectedCategory by rememberSaveable { mutableStateOf(FeedbackCategory.BUG) }
    var selectedTopic by rememberSaveable { mutableStateOf(FeedbackCategory.BUG.defaultTopics.first()) }
    var noteText by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp, top = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Send Feedback & Ideas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Let us know what to improve. We read every response.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Step 1: Category Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "1. WHAT WOULD YOU LIKE TO SHARE?",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeedbackCategoryCard(
                        category = FeedbackCategory.BUG,
                        isSelected = selectedCategory == FeedbackCategory.BUG,
                        onClick = {
                            selectedCategory = FeedbackCategory.BUG
                            selectedTopic = FeedbackCategory.BUG.defaultTopics.first()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FeedbackCategoryCard(
                        category = FeedbackCategory.FEATURE,
                        isSelected = selectedCategory == FeedbackCategory.FEATURE,
                        onClick = {
                            selectedCategory = FeedbackCategory.FEATURE
                            selectedTopic = FeedbackCategory.FEATURE.defaultTopics.first()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeedbackCategoryCard(
                        category = FeedbackCategory.CURRENCY,
                        isSelected = selectedCategory == FeedbackCategory.CURRENCY,
                        onClick = {
                            selectedCategory = FeedbackCategory.CURRENCY
                            selectedTopic = FeedbackCategory.CURRENCY.defaultTopics.first()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FeedbackCategoryCard(
                        category = FeedbackCategory.GENERAL,
                        isSelected = selectedCategory == FeedbackCategory.GENERAL,
                        onClick = {
                            selectedCategory = FeedbackCategory.GENERAL
                            selectedTopic = FeedbackCategory.GENERAL.defaultTopics.first()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Step 2: Contextual Topic Area
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "2. PICK THE RELEVANT AREA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedCategory.defaultTopics.forEach { topic ->
                        val isTopicSelected = topic == selectedTopic
                        FilterChip(
                            selected = isTopicSelected,
                            onClick = { selectedTopic = topic },
                            label = {
                                Text(
                                    text = topic,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isTopicSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isTopicSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Step 3: Optional Short Note
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "3. SHORT NOTE (OPTIONAL)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = {
                        Text(
                            text = when (selectedCategory) {
                                FeedbackCategory.BUG -> "e.g., When I settle credit card, balance doesn't update..."
                                FeedbackCategory.FEATURE -> "e.g., A home screen widget showing monthly expense..."
                                FeedbackCategory.CURRENCY -> "e.g., Need support for AED with 2 decimal places..."
                                FeedbackCategory.GENERAL -> "e.g., Loving the app! Could we also..."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Privacy Reassurance Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "100% Private & Transparent",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Your financial data never leaves your device. Only basic app & OS version are included to help test bug fixes. You can review or edit everything before sending.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // 1-Tap Action Button
            Button(
                onClick = {
                    sendFeedbackEmail(
                        context = context,
                        category = selectedCategory,
                        topic = selectedTopic,
                        noteText = noteText,
                        onSuccess = onDismiss
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Send via Email (1 Tap)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FeedbackCategoryCard(
    category: FeedbackCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                semanticColors.cardStrokeSubtle
            }
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = category.emoji,
                fontSize = 20.sp
            )
            Column {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = category.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

private fun sendFeedbackEmail(
    context: Context,
    category: FeedbackCategory,
    topic: String,
    noteText: String,
    onSuccess: () -> Unit
) {
    val subject = "[Folio ${category.subjectTag}] $topic (v${BuildConfig.VERSION_NAME})"
    val deviceManufacturer = Build.MANUFACTURER.orEmpty().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
    val deviceModel = Build.MODEL.orEmpty()
    val androidRelease = Build.VERSION.RELEASE.orEmpty()
    val apiLevel = Build.VERSION.SDK_INT
    val appVersion = BuildConfig.VERSION_NAME
    val appBuild = BuildConfig.VERSION_CODE

    val cleanNote = noteText.trim().ifEmpty { "(No additional note entered)" }

    val body = buildString {
        appendLine("Feedback Type: ${category.title}")
        appendLine("Topic / Area: $topic")
        appendLine()
        appendLine("Note:")
        appendLine(cleanNote)
        appendLine()
        appendLine("----------------------------------------------")
        appendLine("--- App & Device Context (Helps troubleshoot) ---")
        appendLine("App Version: $appVersion (Build $appBuild)")
        appendLine("Device: $deviceManufacturer $deviceModel")
        appendLine("Android Version: $androidRelease (API Level $apiLevel)")
        append("----------------------------------------------")
    }

    val mailtoUri = Uri.parse("mailto:$SUPPORT_EMAIL")
    val emailIntent = Intent(Intent.ACTION_SENDTO, mailtoUri).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    val handled = runCatching {
        context.startActivity(emailIntent)
        true
    }.getOrElse {
        runCatching {
            val chooser = Intent.createChooser(emailIntent, "Send feedback via email")
            context.startActivity(chooser)
            true
        }.getOrDefault(false)
    }

    if (handled) {
        onSuccess()
    } else {
        Toast.makeText(
            context,
            "No email app found. Please send your feedback to $SUPPORT_EMAIL",
            Toast.LENGTH_LONG
        ).show()
    }
}
