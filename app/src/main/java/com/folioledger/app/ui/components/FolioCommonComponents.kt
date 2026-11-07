// File: app/src/main/java/com/folioledger/app/ui/components/FolioCommonComponents.kt
package com.folioledger.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupportedCurrencies
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.domain.util.MoneyFormatterAndCalculator
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle

enum class MoneyColorMode {
    AUTO_SIGN,
    INCOME,
    EXPENSE,
    TRANSFER,
    NEUTRAL;

    companion object
}

fun MoneyColorMode.Companion.fromTransactionType(type: TransactionType): MoneyColorMode =
    when (type) {
        TransactionType.INCOME, TransactionType.REFUND -> MoneyColorMode.INCOME
        TransactionType.EXPENSE -> MoneyColorMode.EXPENSE
        TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT -> MoneyColorMode.TRANSFER
        TransactionType.OPENING_BALANCE, TransactionType.RECONCILIATION -> MoneyColorMode.NEUTRAL
    }

@Composable
fun MoneyText(
    money: MinorMoney,
    modifier: Modifier = Modifier,
    currencySpec: CurrencySpec = SupportedCurrencies.INR,
    grouping: NumberGroupingSystem = currencySpec.defaultGrouping,
    colorMode: MoneyColorMode = MoneyColorMode.AUTO_SIGN,
    colorOverride: Color? = null,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    includeSymbol: Boolean = true,
    showPlusSignForPositive: Boolean = false,
    maxLines: Int = 1,
) {
    val semanticColors = LocalFolioSemanticColors.current
    val formattedText = remember(
        money,
        currencySpec,
        grouping,
        includeSymbol,
        showPlusSignForPositive,
    ) {
        MoneyFormatterAndCalculator.formatMinorMoney(
            money = money,
            currencySpec = currencySpec,
            grouping = grouping,
            includeSymbol = includeSymbol,
            showPlusSignForPositive = showPlusSignForPositive,
        )
    }

    val resolvedColor = colorOverride ?: when (colorMode) {
        MoneyColorMode.AUTO_SIGN -> when {
            money.isPositive -> semanticColors.incomeEmerald
            money.isNegative -> semanticColors.expenseCoral
            else -> LocalContentColor.current
        }
        MoneyColorMode.INCOME -> semanticColors.incomeEmerald
        MoneyColorMode.EXPENSE -> semanticColors.expenseCoral
        MoneyColorMode.TRANSFER -> semanticColors.transferBlue
        MoneyColorMode.NEUTRAL -> LocalContentColor.current
    }

    val tabularStyle = remember(style, fontWeight) {
        val base = tabularMoneyStyle(style)
        if (fontWeight != null) base.copy(fontWeight = fontWeight) else base
    }

    Text(
        text = formattedText,
        color = resolvedColor,
        style = tabularStyle,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.AutoMirrored.Filled.ReceiptLong,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if ((!actionLabel.isNullOrBlank()) && (onActionClick != null)) {
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
