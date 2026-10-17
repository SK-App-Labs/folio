// File: app/src/main/java/com/folioledger/app/ui/components/FolioCommonComponents.kt
package com.folioledger.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.NetWorthOverview
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.PeriodLedgerSummary
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

    companion object {
        fun fromTransactionType(type: com.folioledger.app.domain.model.TransactionType): MoneyColorMode {
            return when (type) {
                com.folioledger.app.domain.model.TransactionType.INCOME,
                com.folioledger.app.domain.model.TransactionType.REFUND -> INCOME
                com.folioledger.app.domain.model.TransactionType.EXPENSE -> EXPENSE
                com.folioledger.app.domain.model.TransactionType.TRANSFER,
                com.folioledger.app.domain.model.TransactionType.CC_SETTLEMENT -> TRANSFER
            }
        }
    }
}

fun MoneyColorMode.fromTransactionType(type: TransactionType): MoneyColorMode =
    when (type) {
        TransactionType.INCOME, TransactionType.REFUND -> MoneyColorMode.INCOME
        TransactionType.EXPENSE -> MoneyColorMode.EXPENSE
        TransactionType.TRANSFER, TransactionType.CC_SETTLEMENT -> MoneyColorMode.TRANSFER
    }

@Composable
fun MoneyText(
    money: MinorMoney,
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
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val formattedText = remember(
        money,
        currencySpec,
        grouping,
        includeSymbol,
        showPlusSignForPositive
    ) {
        MoneyFormatterAndCalculator.formatMinorMoney(
            money = money,
            currencySpec = currencySpec,
            grouping = grouping,
            includeSymbol = includeSymbol,
            showPlusSignForPositive = showPlusSignForPositive
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

    val tabularStyle = tabularMoneyStyle(style).let { base ->
        if (fontWeight != null) base.copy(fontWeight = fontWeight) else base
    }

    Text(
        text = formattedText,
        color = resolvedColor,
        style = tabularStyle,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
fun PeriodSelectorHeader(
    periodTitle: String,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    modifier: Modifier = Modifier,
    periodSubtitle: String? = null,
    onPeriodTitleClick: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    onFilterClick: (() -> Unit)? = null,
    activeFilterCount: Int = 0,
    onBookmarksClick: (() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                IconButton(
                    onClick = onPreviousPeriod,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Period",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (onPeriodTitleClick != null) {
                                Modifier.clickable(onClick = onPeriodTitleClick)
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = periodTitle,
                            style = tabularMoneyStyle(MaterialTheme.typography.titleMedium),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!periodSubtitle.isNullOrBlank()) {
                            Text(
                                text = periodSubtitle,
                                style = tabularMoneyStyle(MaterialTheme.typography.labelSmall),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onNextPeriod,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Period",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (onBookmarksClick != null) {
                    IconButton(
                        onClick = onBookmarksClick,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Favorite Bookmarks",
                            tint = LocalFolioSemanticColors.current.warningAmber
                        )
                    }
                }

                if (onFilterClick != null) {
                    IconButton(
                        onClick = onFilterClick,
                        modifier = Modifier.size(38.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (activeFilterCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text(
                                            text = activeFilterCount.toString(),
                                            style = tabularMoneyStyle(MaterialTheme.typography.labelSmall)
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = "Filter Transactions",
                                tint = if (activeFilterCount > 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                if (onSearchClick != null) {
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search Ledger",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThreeColumnSummaryBanner(
    firstLabel: String,
    firstAmount: MinorMoney,
    secondLabel: String,
    secondAmount: MinorMoney,
    thirdLabel: String,
    thirdAmount: MinorMoney,
    currencySpec: CurrencySpec = SupportedCurrencies.INR,
    grouping: NumberGroupingSystem = currencySpec.defaultGrouping,
    firstColorMode: MoneyColorMode = MoneyColorMode.INCOME,
    secondColorMode: MoneyColorMode = MoneyColorMode.EXPENSE,
    thirdColorMode: MoneyColorMode = MoneyColorMode.AUTO_SIGN,
    carryOverAmount: MinorMoney? = null,
    onColumnClick: ((columnIndex: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, semanticColors.cardStrokeSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(vertical = 12.dp)
            ) {
                SummaryColumnCell(
                    label = firstLabel,
                    amount = firstAmount,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    colorMode = firstColorMode,
                    onClick = if (onColumnClick != null) ({ onColumnClick(0) }) else null,
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(
                    color = semanticColors.cardStrokeSubtle,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                )

                SummaryColumnCell(
                    label = secondLabel,
                    amount = secondAmount,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    colorMode = secondColorMode,
                    onClick = if (onColumnClick != null) ({ onColumnClick(1) }) else null,
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(
                    color = semanticColors.cardStrokeSubtle,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                )

                SummaryColumnCell(
                    label = thirdLabel,
                    amount = thirdAmount,
                    currencySpec = currencySpec,
                    grouping = grouping,
                    colorMode = thirdColorMode,
                    showPlusSignForPositive = false,
                    onClick = if (onColumnClick != null) ({ onColumnClick(2) }) else null,
                    modifier = Modifier.weight(1f)
                )
            }

            if (carryOverAmount != null && !carryOverAmount.isZero) {
                HorizontalDivider(
                    color = semanticColors.cardStrokeSubtle,
                    thickness = 1.dp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Carry-over from previous period",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        money = carryOverAmount,
                        currencySpec = currencySpec,
                        grouping = grouping,
                        colorMode = MoneyColorMode.AUTO_SIGN,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        showPlusSignForPositive = true
                    )
                }
            }
        }
    }
}

@Composable
fun ThreeColumnSummaryBanner(
    periodSummary: PeriodLedgerSummary,
    currencySpec: CurrencySpec = SupportedCurrencies.INR,
    grouping: NumberGroupingSystem = currencySpec.defaultGrouping,
    showCarryOver: Boolean = false,
    onColumnClick: ((columnIndex: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ThreeColumnSummaryBanner(
        firstLabel = "Income",
        firstAmount = periodSummary.totalIncome,
        secondLabel = "Expenses",
        secondAmount = periodSummary.totalExpense,
        thirdLabel = "Total",
        thirdAmount = periodSummary.netCashflow,
        currencySpec = currencySpec,
        grouping = grouping,
        firstColorMode = MoneyColorMode.INCOME,
        secondColorMode = MoneyColorMode.EXPENSE,
        thirdColorMode = MoneyColorMode.AUTO_SIGN,
        carryOverAmount = if (showCarryOver) periodSummary.carryOverFromPreviousPeriod else null,
        onColumnClick = onColumnClick,
        modifier = modifier
    )
}

@Composable
fun ThreeColumnSummaryBanner(
    netWorthOverview: NetWorthOverview,
    currencySpec: CurrencySpec = SupportedCurrencies.INR,
    grouping: NumberGroupingSystem = currencySpec.defaultGrouping,
    onColumnClick: ((columnIndex: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ThreeColumnSummaryBanner(
        firstLabel = "Assets",
        firstAmount = netWorthOverview.totalAssets,
        secondLabel = "Liabilities",
        secondAmount = netWorthOverview.totalLiabilities,
        thirdLabel = "Net Worth",
        thirdAmount = netWorthOverview.netWorth,
        currencySpec = currencySpec,
        grouping = grouping,
        firstColorMode = MoneyColorMode.INCOME,
        secondColorMode = MoneyColorMode.EXPENSE,
        thirdColorMode = MoneyColorMode.AUTO_SIGN,
        carryOverAmount = null,
        onColumnClick = onColumnClick,
        modifier = modifier
    )
}

@Composable
private fun SummaryColumnCell(
    label: String,
    amount: MinorMoney,
    currencySpec: CurrencySpec,
    grouping: NumberGroupingSystem,
    colorMode: MoneyColorMode,
    showPlusSignForPositive: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        MoneyText(
            money = amount,
            currencySpec = currencySpec,
            grouping = grouping,
            colorMode = colorMode,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            showPlusSignForPositive = showPlusSignForPositive
        )
    }
}

fun parseHexColorSafely(colorHex: String?, fallback: Color = Color(0xFF059669)): Color {
    if (colorHex.isNullOrBlank()) return fallback
    val normalized = if (colorHex.trim().startsWith("#")) {
        colorHex.trim()
    } else {
        "#${colorHex.trim()}"
    }
    return try {
        Color(android.graphics.Color.parseColor(normalized))
    } catch (_: IllegalArgumentException) {
        fallback
    }
}

fun resolveFolioIcon(iconName: String?): ImageVector {
    val key = iconName?.trim()?.lowercase() ?: return Icons.Filled.Category
    return when (key) {
        "restaurant", "food", "dining", "utensils" -> Icons.Filled.Restaurant
        "fastfood", "snacks", "street_food", "burger" -> Icons.Filled.Fastfood
        "local_cafe", "cafe", "coffee", "tea" -> Icons.Filled.LocalCafe
        "shopping_cart", "groceries", "shopping", "cart", "supermarket" -> Icons.Filled.ShoppingCart
        "checkroom", "clothing", "apparel", "fashion" -> Icons.Filled.Checkroom
        "home", "rent", "housing", "mortgage", "house" -> Icons.Filled.Home
        "bolt", "electricity", "power", "utilities" -> Icons.Filled.Bolt
        "water_drop", "water" -> Icons.Filled.WaterDrop
        "wifi", "internet", "broadband" -> Icons.Filled.Wifi
        "phone_android", "mobile", "recharge", "telecom" -> Icons.Filled.PhoneAndroid
        "directions_car", "car", "vehicle", "transport", "cab", "taxi" -> Icons.Filled.DirectionsCar
        "local_gas_station", "fuel", "petrol", "gas", "diesel" -> Icons.Filled.LocalGasStation
        "directions_bus", "bus", "metro", "train", "public_transit" -> Icons.Filled.DirectionsBus
        "flight", "travel", "vacation", "airfare" -> Icons.Filled.Flight
        "local_hospital", "health", "medical", "pharmacy", "doctor" -> Icons.Filled.LocalHospital
        "fitness_center", "gym", "fitness", "sports" -> Icons.Filled.FitnessCenter
        "spa", "personal_care", "grooming", "salon" -> Icons.Filled.Spa
        "movie", "entertainment", "streaming", "cinema" -> Icons.Filled.Movie
        "school", "education", "books", "courses", "tuition" -> Icons.Filled.School
        "child_care", "baby", "kids", "family" -> Icons.Filled.ChildCare
        "pets", "pet", "vet" -> Icons.Filled.Pets
        "build", "maintenance", "repair", "service" -> Icons.Filled.Build
        "account_balance", "bank", "bank_account", "tax" -> Icons.Filled.AccountBalance
        "account_balance_wallet", "wallet", "cash", "prepaid_wallet" -> Icons.Filled.AccountBalanceWallet
        "credit_card", "card", "debit_card", "emi" -> Icons.Filled.CreditCard
        "payments", "salary", "paycheck", "cashflow" -> Icons.Filled.Payments
        "work", "business", "freelance", "office" -> Icons.Filled.Work
        "trending_up", "investment", "stocks", "mutual_fund", "dividends" -> Icons.AutoMirrored.Filled.TrendingUp
        "trending_down", "loss", "loan" -> Icons.AutoMirrored.Filled.TrendingDown
        "savings", "piggy_bank", "deposit", "emergency_fund" -> Icons.Filled.Savings
        "card_giftcard", "gift", "bonus", "charity" -> Icons.Filled.CardGiftcard
        "receipt_long", "bills", "invoice", "subscription" -> Icons.AutoMirrored.Filled.ReceiptLong
        "swap_horiz", "transfer", "cc_settlement" -> Icons.Filled.SwapHoriz
        "compare_arrows", "exchange" -> Icons.AutoMirrored.Filled.CompareArrows
        "autorenew", "recurring", "refund" -> Icons.Filled.Autorenew
        "security", "insurance", "protection" -> Icons.Filled.Security
        "more_horiz", "other", "miscellaneous" -> Icons.Filled.MoreHoriz
        else -> Icons.Filled.Category
    }
}

@Composable
fun CategoryIconBadge(
    iconName: String?,
    colorHex: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp
) {
    val primaryFallback = MaterialTheme.colorScheme.primary
    val baseColor = remember(colorHex, primaryFallback) {
        parseHexColorSafely(colorHex, fallback = primaryFallback)
    }
    val iconVector = remember(iconName) {
        resolveFolioIcon(iconName)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(baseColor.copy(alpha = 0.16f))
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = iconName ?: "Category",
            tint = baseColor,
            modifier = Modifier.size(iconSize)
        )
    }
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

            if (!actionLabel.isNullOrBlank() && onActionClick != null) {
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