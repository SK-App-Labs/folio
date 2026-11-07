package com.folioledger.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.folioledger.app.domain.model.CurrencySpec
import com.folioledger.app.ui.components.FolioBrandLogoBadge as ExtractedFolioBrandLogoBadge
import com.folioledger.app.ui.settings.sheets.TipJarBottomSheet as ExtractedTipJarBottomSheet
import com.folioledger.app.ui.settings.sheets.UserGuideBottomSheet as ExtractedUserGuideBottomSheet

typealias GuideCategoryTab = com.folioledger.app.ui.settings.guides.GuideCategoryTab

/**
 * Backward-compatible delegating wrapper for Folio's signature app icon badge.
 */
@Composable
fun FolioBrandLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 60.dp
) {
    ExtractedFolioBrandLogoBadge(modifier = modifier, size = size)
}

/**
 * Backward-compatible delegating wrapper for the Folio User Guide & FAQs bottom sheet.
 */
@Composable
fun UserGuideBottomSheet(
    onDismiss: () -> Unit
) {
    ExtractedUserGuideBottomSheet(onDismiss = onDismiss)
}

/**
 * Backward-compatible delegating wrapper for the Folio Tip Jar & Supporter bottom sheet.
 */
@Composable
fun TipJarBottomSheet(
    currencySpec: CurrencySpec,
    isSupporter: Boolean = false,
    supporterTier: String? = null,
    onTipSelected: (skuId: String, tierTitle: String) -> Unit,
    onDismiss: () -> Unit
) {
    ExtractedTipJarBottomSheet(
        currencySpec = currencySpec,
        isSupporter = isSupporter,
        supporterTier = supporterTier,
        onTipSelected = onTipSelected,
        onDismiss = onDismiss
    )
}
