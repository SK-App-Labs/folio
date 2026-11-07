package com.folioledger.app.ui.entry.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.TransactionType
import com.folioledger.app.ui.components.MoneyColorMode
import com.folioledger.app.ui.components.MoneyText
import com.folioledger.app.ui.components.fromTransactionType
import com.folioledger.app.ui.theme.LocalFolioSemanticColors
import com.folioledger.app.ui.theme.tabularMoneyStyle

@Composable
internal fun EntryAmountDisplayCard(
    isKeypadActive: Boolean,
    isEditingExisting: Boolean,
    mode: TransactionType,
    evaluatedAmount: MinorMoney,
    expressionBuffer: String,
    expressionHasOperator: Boolean,
    currencySpec: CurrencySpec,
    numberGrouping: NumberGroupingSystem,
    amountInput: String,
    amountFocusRequester: FocusRequester,
    onAmountInputChange: (String) -> Unit,
    onCardClick: () -> Unit,
    onToggleKeypadClick: () -> Unit,
    onNextImeAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onCardClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val modePrefix = if (isEditingExisting) "EDITING · " else ""
                Text(
                    text = modePrefix + when (mode) {
                        TransactionType.EXPENSE -> "EXPENSE AMOUNT"
                        TransactionType.INCOME -> "INCOME AMOUNT"
                        TransactionType.TRANSFER -> "TRANSFER AMOUNT"
                        TransactionType.REFUND -> "REFUND AMOUNT"
                        TransactionType.CC_SETTLEMENT -> "SETTLEMENT AMOUNT"
                        TransactionType.OPENING_BALANCE -> "OPENING BALANCE"
                        TransactionType.RECONCILIATION -> "RECONCILIATION AMOUNT"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (expressionHasOperator || (isKeypadActive && expressionBuffer != "0")) {
                    Text(
                        text = "${currencySpec.symbol} $expressionBuffer",
                        style = tabularMoneyStyle(MaterialTheme.typography.bodyMedium),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (isKeypadActive) {
                    MoneyText(
                        money = evaluatedAmount,
                        currencySpec = currencySpec,
                        grouping = numberGrouping,
                        colorMode = MoneyColorMode.fromTransactionType(mode),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    val amountColor = when (mode) {
                        TransactionType.EXPENSE -> semanticColors.expenseCoral
                        TransactionType.INCOME, TransactionType.REFUND -> semanticColors.incomeEmerald
                        TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT -> semanticColors.transferBlue
                        TransactionType.OPENING_BALANCE, TransactionType.RECONCILIATION -> MaterialTheme.colorScheme.primary
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = currencySpec.symbol,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = amountColor.copy(alpha = 0.8f),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        BasicTextField(
                            value = amountInput,
                            onValueChange = onAmountInputChange,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { onNextImeAction() }),
                            singleLine = true,
                            textStyle = tabularMoneyStyle(MaterialTheme.typography.headlineMedium).copy(
                                color = amountColor,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .focusRequester(amountFocusRequester)
                                .widthIn(min = 40.dp, max = 260.dp),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterEnd) {
                                    if (amountInput.isEmpty() || amountInput == "0") {
                                        Text(
                                            text = "0",
                                            style = tabularMoneyStyle(MaterialTheme.typography.headlineMedium),
                                            fontWeight = FontWeight.Bold,
                                            color = amountColor.copy(alpha = 0.35f),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onToggleKeypadClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Calculate,
                        contentDescription = "Toggle Calculator Keypad",
                        tint = if (isKeypadActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
