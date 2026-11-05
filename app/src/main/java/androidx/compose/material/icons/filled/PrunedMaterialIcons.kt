// File: app/src/main/java/androidx/compose/material/icons/filled/PrunedMaterialIcons.kt
package androidx.compose.material.icons.filled

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Filled.AccessTime: ImageVector
    get() {
        if (_accessTime != null) {
            return _accessTime!!
        }
        _accessTime = materialIcon(name = "Filled.AccessTime") {
            materialPath {
                moveTo(11.99f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 9.99f, 10.0f)
                curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
                close()
            }
            materialPath {
                moveTo(12.5f, 7.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(6.0f)
                lineToRelative(5.25f, 3.15f)
                lineToRelative(0.75f, -1.23f)
                lineToRelative(-4.5f, -2.67f)
                close()
            }
        }
        return _accessTime!!
    }

private var _accessTime: ImageVector? = null

public val Icons.Filled.AccountBalance: ImageVector
    get() {
        if (_accountBalance != null) {
            return _accountBalance!!
        }
        _accountBalance = materialIcon(name = "Filled.AccountBalance") {
            materialPath {
                moveTo(4.0f, 10.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(7.0f)
                horizontalLineToRelative(-3.0f)
                close()
            }
            materialPath {
                moveTo(10.5f, 10.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(7.0f)
                horizontalLineToRelative(-3.0f)
                close()
            }
            materialPath {
                moveTo(2.0f, 19.0f)
                horizontalLineToRelative(20.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(-20.0f)
                close()
            }
            materialPath {
                moveTo(17.0f, 10.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(7.0f)
                horizontalLineToRelative(-3.0f)
                close()
            }
            materialPath {
                moveTo(12.0f, 1.0f)
                lineToRelative(-10.0f, 5.0f)
                lineToRelative(0.0f, 2.0f)
                lineToRelative(20.0f, 0.0f)
                lineToRelative(0.0f, -2.0f)
                close()
            }
        }
        return _accountBalance!!
    }

private var _accountBalance: ImageVector? = null

public val Icons.Filled.AccountBalanceWallet: ImageVector
    get() {
        if (_accountBalanceWallet != null) {
            return _accountBalanceWallet!!
        }
        _accountBalanceWallet = materialIcon(name = "Filled.AccountBalanceWallet") {
            materialPath {
                moveTo(21.0f, 18.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                lineTo(5.0f, 21.0f)
                curveToRelative(-1.11f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                lineTo(3.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, 0.89f, -2.0f, 2.0f, -2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-9.0f)
                curveToRelative(-1.11f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(8.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(9.0f)
                close()
                moveTo(12.0f, 16.0f)
                horizontalLineToRelative(10.0f)
                lineTo(22.0f, 8.0f)
                lineTo(12.0f, 8.0f)
                verticalLineToRelative(8.0f)
                close()
                moveTo(16.0f, 13.5f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
            }
        }
        return _accountBalanceWallet!!
    }

private var _accountBalanceWallet: ImageVector? = null

public val Icons.Filled.AcUnit: ImageVector
    get() {
        if (_acUnit != null) {
            return _acUnit!!
        }
        _acUnit = materialIcon(name = "Filled.AcUnit") {
            materialPath {
                moveTo(22.0f, 11.0f)
                horizontalLineToRelative(-4.17f)
                lineToRelative(3.24f, -3.24f)
                lineToRelative(-1.41f, -1.42f)
                lineTo(15.0f, 11.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(9.0f)
                lineToRelative(4.66f, -4.66f)
                lineToRelative(-1.42f, -1.41f)
                lineTo(13.0f, 6.17f)
                verticalLineTo(2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(4.17f)
                lineTo(7.76f, 2.93f)
                lineTo(6.34f, 4.34f)
                lineTo(11.0f, 9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(9.0f)
                lineTo(4.34f, 6.34f)
                lineTo(2.93f, 7.76f)
                lineTo(6.17f, 11.0f)
                horizontalLineTo(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(4.17f)
                lineToRelative(-3.24f, 3.24f)
                lineToRelative(1.41f, 1.42f)
                lineTo(9.0f, 13.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                lineToRelative(-4.66f, 4.66f)
                lineToRelative(1.42f, 1.41f)
                lineTo(11.0f, 17.83f)
                verticalLineTo(22.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-4.17f)
                lineToRelative(3.24f, 3.24f)
                lineToRelative(1.42f, -1.41f)
                lineTo(13.0f, 15.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                lineToRelative(4.66f, 4.66f)
                lineToRelative(1.41f, -1.42f)
                lineTo(17.83f, 13.0f)
                horizontalLineTo(22.0f)
                close()
            }
        }
        return _acUnit!!
    }

private var _acUnit: ImageVector? = null

public val Icons.Filled.Apartment: ImageVector
    get() {
        if (_apartment != null) {
            return _apartment!!
        }
        _apartment = materialIcon(name = "Filled.Apartment") {
            materialPath {
                moveTo(17.0f, 11.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(4.0f)
                horizontalLineTo(3.0f)
                verticalLineToRelative(14.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(8.0f)
                verticalLineTo(11.0f)
                horizontalLineTo(17.0f)
                close()
                moveTo(7.0f, 19.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(7.0f, 15.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(15.0f)
                close()
                moveTo(7.0f, 11.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(11.0f)
                close()
                moveTo(11.0f, 15.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(15.0f)
                close()
                moveTo(11.0f, 11.0f)
                horizontalLineTo(9.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(11.0f)
                close()
                moveTo(11.0f, 7.0f)
                horizontalLineTo(9.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(7.0f)
                close()
                moveTo(15.0f, 15.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(15.0f)
                close()
                moveTo(15.0f, 11.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(11.0f)
                close()
                moveTo(15.0f, 7.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(7.0f)
                close()
                moveTo(19.0f, 19.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(19.0f, 15.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(15.0f)
                close()
            }
        }
        return _apartment!!
    }

private var _apartment: ImageVector? = null

public val Icons.Filled.Archive: ImageVector
    get() {
        if (_archive != null) {
            return _archive!!
        }
        _archive = materialIcon(name = "Filled.Archive") {
            materialPath {
                moveTo(20.54f, 5.23f)
                lineToRelative(-1.39f, -1.68f)
                curveTo(18.88f, 3.21f, 18.47f, 3.0f, 18.0f, 3.0f)
                horizontalLineTo(6.0f)
                curveToRelative(-0.47f, 0.0f, -0.88f, 0.21f, -1.16f, 0.55f)
                lineTo(3.46f, 5.23f)
                curveTo(3.17f, 5.57f, 3.0f, 6.02f, 3.0f, 6.5f)
                verticalLineTo(19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(6.5f)
                curveToRelative(0.0f, -0.48f, -0.17f, -0.93f, -0.46f, -1.27f)
                close()
                moveTo(12.0f, 17.5f)
                lineTo(6.5f, 12.0f)
                horizontalLineTo(10.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(3.5f)
                lineTo(12.0f, 17.5f)
                close()
                moveTo(5.12f, 5.0f)
                lineToRelative(0.81f, -1.0f)
                horizontalLineToRelative(12.0f)
                lineToRelative(0.94f, 1.0f)
                horizontalLineTo(5.12f)
                close()
            }
        }
        return _archive!!
    }

private var _archive: ImageVector? = null

public val Icons.Filled.AttachMoney: ImageVector
    get() {
        if (_attachMoney != null) {
            return _attachMoney!!
        }
        _attachMoney = materialIcon(name = "Filled.AttachMoney") {
            materialPath {
                moveTo(11.8f, 10.9f)
                curveToRelative(-2.27f, -0.59f, -3.0f, -1.2f, -3.0f, -2.15f)
                curveToRelative(0.0f, -1.09f, 1.01f, -1.85f, 2.7f, -1.85f)
                curveToRelative(1.78f, 0.0f, 2.44f, 0.85f, 2.5f, 2.1f)
                horizontalLineToRelative(2.21f)
                curveToRelative(-0.07f, -1.72f, -1.12f, -3.3f, -3.21f, -3.81f)
                verticalLineTo(3.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineToRelative(2.16f)
                curveToRelative(-1.94f, 0.42f, -3.5f, 1.68f, -3.5f, 3.61f)
                curveToRelative(0.0f, 2.31f, 1.91f, 3.46f, 4.7f, 4.13f)
                curveToRelative(2.5f, 0.6f, 3.0f, 1.48f, 3.0f, 2.41f)
                curveToRelative(0.0f, 0.69f, -0.49f, 1.79f, -2.7f, 1.79f)
                curveToRelative(-2.06f, 0.0f, -2.87f, -0.92f, -2.98f, -2.1f)
                horizontalLineToRelative(-2.2f)
                curveToRelative(0.12f, 2.19f, 1.76f, 3.42f, 3.68f, 3.83f)
                verticalLineTo(21.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(-2.15f)
                curveToRelative(1.95f, -0.37f, 3.5f, -1.5f, 3.5f, -3.55f)
                curveToRelative(0.0f, -2.84f, -2.43f, -3.81f, -4.7f, -4.4f)
                close()
            }
        }
        return _attachMoney!!
    }

private var _attachMoney: ImageVector? = null

public val Icons.Filled.AutoAwesome: ImageVector
    get() {
        if (_autoAwesome != null) {
            return _autoAwesome!!
        }
        _autoAwesome = materialIcon(name = "Filled.AutoAwesome") {
            materialPath {
                moveTo(19.0f, 9.0f)
                lineToRelative(1.25f, -2.75f)
                lineTo(23.0f, 5.0f)
                lineToRelative(-2.75f, -1.25f)
                lineTo(19.0f, 1.0f)
                lineToRelative(-1.25f, 2.75f)
                lineTo(15.0f, 5.0f)
                lineToRelative(2.75f, 1.25f)
                lineTo(19.0f, 9.0f)
                close()
                moveTo(11.5f, 9.5f)
                lineTo(9.0f, 4.0f)
                lineTo(6.5f, 9.5f)
                lineTo(1.0f, 12.0f)
                lineToRelative(5.5f, 2.5f)
                lineTo(9.0f, 20.0f)
                lineToRelative(2.5f, -5.5f)
                lineTo(17.0f, 12.0f)
                lineToRelative(-5.5f, -2.5f)
                close()
                moveTo(19.0f, 15.0f)
                lineToRelative(-1.25f, 2.75f)
                lineTo(15.0f, 19.0f)
                lineToRelative(2.75f, 1.25f)
                lineTo(19.0f, 23.0f)
                lineToRelative(1.25f, -2.75f)
                lineTo(23.0f, 19.0f)
                lineToRelative(-2.75f, -1.25f)
                lineTo(19.0f, 15.0f)
                close()
            }
        }
        return _autoAwesome!!
    }

private var _autoAwesome: ImageVector? = null

public val Icons.Filled.Autorenew: ImageVector
    get() {
        if (_autorenew != null) {
            return _autorenew!!
        }
        _autorenew = materialIcon(name = "Filled.Autorenew") {
            materialPath {
                moveTo(12.0f, 6.0f)
                verticalLineToRelative(3.0f)
                lineToRelative(4.0f, -4.0f)
                lineToRelative(-4.0f, -4.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(-4.42f, 0.0f, -8.0f, 3.58f, -8.0f, 8.0f)
                curveToRelative(0.0f, 1.57f, 0.46f, 3.03f, 1.24f, 4.26f)
                lineTo(6.7f, 14.8f)
                curveToRelative(-0.45f, -0.83f, -0.7f, -1.79f, -0.7f, -2.8f)
                curveToRelative(0.0f, -3.31f, 2.69f, -6.0f, 6.0f, -6.0f)
                close()
                moveTo(18.76f, 7.74f)
                lineTo(17.3f, 9.2f)
                curveToRelative(0.44f, 0.84f, 0.7f, 1.79f, 0.7f, 2.8f)
                curveToRelative(0.0f, 3.31f, -2.69f, 6.0f, -6.0f, 6.0f)
                verticalLineToRelative(-3.0f)
                lineToRelative(-4.0f, 4.0f)
                lineToRelative(4.0f, 4.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(4.42f, 0.0f, 8.0f, -3.58f, 8.0f, -8.0f)
                curveToRelative(0.0f, -1.57f, -0.46f, -3.03f, -1.24f, -4.26f)
                close()
            }
        }
        return _autorenew!!
    }

private var _autorenew: ImageVector? = null

public val Icons.Filled.Backup: ImageVector
    get() {
        if (_backup != null) {
            return _backup!!
        }
        _backup = materialIcon(name = "Filled.Backup") {
            materialPath {
                moveTo(19.35f, 10.04f)
                curveTo(18.67f, 6.59f, 15.64f, 4.0f, 12.0f, 4.0f)
                curveTo(9.11f, 4.0f, 6.6f, 5.64f, 5.35f, 8.04f)
                curveTo(2.34f, 8.36f, 0.0f, 10.91f, 0.0f, 14.0f)
                curveToRelative(0.0f, 3.31f, 2.69f, 6.0f, 6.0f, 6.0f)
                horizontalLineToRelative(13.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, -2.24f, 5.0f, -5.0f)
                curveToRelative(0.0f, -2.64f, -2.05f, -4.78f, -4.65f, -4.96f)
                close()
                moveTo(14.0f, 13.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineTo(7.0f)
                lineToRelative(5.0f, -5.0f)
                lineToRelative(5.0f, 5.0f)
                horizontalLineToRelative(-3.0f)
                close()
            }
        }
        return _backup!!
    }

private var _backup: ImageVector? = null

public val Icons.Filled.BakeryDining: ImageVector
    get() {
        if (_bakeryDining != null) {
            return _bakeryDining!!
        }
        _bakeryDining = materialIcon(name = "Filled.BakeryDining") {
            materialPath(pathFillType = EvenOdd) {
                moveTo(19.28f, 16.34f)
                curveTo(18.07f, 15.45f, 17.46f, 15.0f, 17.46f, 15.0f)
                reflectiveCurveToRelative(0.32f, -0.59f, 0.96f, -1.78f)
                curveToRelative(0.38f, -0.59f, 1.22f, -0.59f, 1.6f, 0.0f)
                lineToRelative(0.81f, 1.26f)
                curveToRelative(0.19f, 0.3f, 0.21f, 0.68f, 0.06f, 1.0f)
                lineToRelative(-0.22f, 0.47f)
                curveTo(20.42f, 16.49f, 19.76f, 16.67f, 19.28f, 16.34f)
                close()
                moveTo(4.72f, 16.34f)
                curveToRelative(-0.48f, 0.33f, -1.13f, 0.15f, -1.39f, -0.38f)
                lineTo(3.1f, 15.49f)
                curveToRelative(-0.15f, -0.32f, -0.13f, -0.7f, 0.06f, -1.0f)
                lineToRelative(0.81f, -1.26f)
                curveToRelative(0.38f, -0.59f, 1.22f, -0.59f, 1.6f, 0.0f)
                curveTo(6.22f, 14.41f, 6.54f, 15.0f, 6.54f, 15.0f)
                reflectiveCurveTo(5.93f, 15.45f, 4.72f, 16.34f)
                close()
                moveTo(15.36f, 9.37f)
                curveToRelative(0.09f, -0.68f, 0.73f, -1.06f, 1.27f, -0.75f)
                lineToRelative(1.59f, 0.9f)
                curveToRelative(0.46f, 0.26f, 0.63f, 0.91f, 0.36f, 1.41f)
                lineTo(16.5f, 15.0f)
                horizontalLineToRelative(-1.8f)
                lineTo(15.36f, 9.37f)
                close()
                moveTo(8.63f, 9.37f)
                lineTo(9.3f, 15.0f)
                horizontalLineTo(7.5f)
                lineToRelative(-2.09f, -4.08f)
                curveToRelative(-0.27f, -0.5f, -0.1f, -1.15f, 0.36f, -1.41f)
                lineToRelative(1.59f, -0.9f)
                curveTo(7.89f, 8.31f, 8.54f, 8.69f, 8.63f, 9.37f)
                close()
                moveTo(13.8f, 15.0f)
                horizontalLineToRelative(-3.6f)
                lineTo(9.46f, 8.12f)
                curveTo(9.39f, 7.53f, 9.81f, 7.0f, 10.34f, 7.0f)
                horizontalLineToRelative(3.3f)
                curveToRelative(0.53f, 0.0f, 0.94f, 0.53f, 0.88f, 1.12f)
                lineTo(13.8f, 15.0f)
                close()
            }
        }
        return _bakeryDining!!
    }

private var _bakeryDining: ImageVector? = null

public val Icons.Filled.BeachAccess: ImageVector
    get() {
        if (_beachAccess != null) {
            return _beachAccess!!
        }
        _beachAccess = materialIcon(name = "Filled.BeachAccess") {
            materialPath {
                moveTo(13.127f, 14.56f)
                lineToRelative(1.43f, -1.43f)
                lineToRelative(6.44f, 6.443f)
                lineTo(19.57f, 21.0f)
                close()
                moveTo(17.42f, 8.83f)
                lineToRelative(2.86f, -2.86f)
                curveToRelative(-3.95f, -3.95f, -10.35f, -3.96f, -14.3f, -0.02f)
                curveToRelative(3.93f, -1.3f, 8.31f, -0.25f, 11.44f, 2.88f)
                close()
                moveTo(5.95f, 5.98f)
                curveToRelative(-3.94f, 3.95f, -3.93f, 10.35f, 0.02f, 14.3f)
                lineToRelative(2.86f, -2.86f)
                curveTo(5.7f, 14.29f, 4.65f, 9.91f, 5.95f, 5.98f)
                close()
                moveTo(5.97f, 5.96f)
                lineToRelative(-0.01f, 0.01f)
                curveToRelative(-0.38f, 3.01f, 1.17f, 6.88f, 4.3f, 10.02f)
                lineToRelative(5.73f, -5.73f)
                curveToRelative(-3.13f, -3.13f, -7.01f, -4.68f, -10.02f, -4.3f)
                close()
            }
        }
        return _beachAccess!!
    }

private var _beachAccess: ImageVector? = null

public val Icons.Filled.Bolt: ImageVector
    get() {
        if (_bolt != null) {
            return _bolt!!
        }
        _bolt = materialIcon(name = "Filled.Bolt") {
            materialPath {
                moveTo(11.0f, 21.0f)
                horizontalLineToRelative(-1.0f)
                lineToRelative(1.0f, -7.0f)
                horizontalLineTo(7.5f)
                curveToRelative(-0.58f, 0.0f, -0.57f, -0.32f, -0.38f, -0.66f)
                curveToRelative(0.19f, -0.34f, 0.05f, -0.08f, 0.07f, -0.12f)
                curveTo(8.48f, 10.94f, 10.42f, 7.54f, 13.0f, 3.0f)
                horizontalLineToRelative(1.0f)
                lineToRelative(-1.0f, 7.0f)
                horizontalLineToRelative(3.5f)
                curveToRelative(0.49f, 0.0f, 0.56f, 0.33f, 0.47f, 0.51f)
                lineToRelative(-0.07f, 0.15f)
                curveTo(12.96f, 17.55f, 11.0f, 21.0f, 11.0f, 21.0f)
                close()
            }
        }
        return _bolt!!
    }

private var _bolt: ImageVector? = null

public val Icons.Filled.BusinessCenter: ImageVector
    get() {
        if (_businessCenter != null) {
            return _businessCenter!!
        }
        _businessCenter = materialIcon(name = "Filled.BusinessCenter") {
            materialPath {
                moveTo(10.0f, 16.0f)
                verticalLineToRelative(-1.0f)
                lineTo(3.01f, 15.0f)
                lineTo(3.0f, 19.0f)
                curveToRelative(0.0f, 1.11f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.11f, 0.0f, 2.0f, -0.89f, 2.0f, -2.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(-7.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-4.0f)
                close()
                moveTo(20.0f, 7.0f)
                horizontalLineToRelative(-4.01f)
                lineTo(15.99f, 5.0f)
                lineToRelative(-2.0f, -2.0f)
                horizontalLineToRelative(-4.0f)
                lineToRelative(-2.0f, 2.0f)
                verticalLineToRelative(2.0f)
                lineTo(4.0f, 7.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 1.11f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 9.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(14.0f, 7.0f)
                horizontalLineToRelative(-4.0f)
                lineTo(10.0f, 5.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
        return _businessCenter!!
    }

private var _businessCenter: ImageVector? = null

public val Icons.Filled.Cake: ImageVector
    get() {
        if (_cake != null) {
            return _cake!!
        }
        _cake = materialIcon(name = "Filled.Cake") {
            materialPath {
                moveTo(12.0f, 6.0f)
                curveToRelative(1.11f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                curveToRelative(0.0f, -0.38f, -0.1f, -0.73f, -0.29f, -1.03f)
                lineTo(12.0f, 0.0f)
                lineToRelative(-1.71f, 2.97f)
                curveToRelative(-0.19f, 0.3f, -0.29f, 0.65f, -0.29f, 1.03f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                close()
                moveTo(16.6f, 15.99f)
                lineToRelative(-1.07f, -1.07f)
                lineToRelative(-1.08f, 1.07f)
                curveToRelative(-1.3f, 1.3f, -3.58f, 1.31f, -4.89f, 0.0f)
                lineToRelative(-1.07f, -1.07f)
                lineToRelative(-1.09f, 1.07f)
                curveTo(6.75f, 16.64f, 5.88f, 17.0f, 4.96f, 17.0f)
                curveToRelative(-0.73f, 0.0f, -1.4f, -0.23f, -1.96f, -0.61f)
                lineTo(3.0f, 21.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-4.61f)
                curveToRelative(-0.56f, 0.38f, -1.23f, 0.61f, -1.96f, 0.61f)
                curveToRelative(-0.92f, 0.0f, -1.79f, -0.36f, -2.44f, -1.01f)
                close()
                moveTo(18.0f, 9.0f)
                horizontalLineToRelative(-5.0f)
                lineTo(13.0f, 7.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                lineTo(6.0f, 9.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                verticalLineToRelative(1.54f)
                curveToRelative(0.0f, 1.08f, 0.88f, 1.96f, 1.96f, 1.96f)
                curveToRelative(0.52f, 0.0f, 1.02f, -0.2f, 1.38f, -0.57f)
                lineToRelative(2.14f, -2.13f)
                lineToRelative(2.13f, 2.13f)
                curveToRelative(0.74f, 0.74f, 2.03f, 0.74f, 2.77f, 0.0f)
                lineToRelative(2.14f, -2.13f)
                lineToRelative(2.13f, 2.13f)
                curveToRelative(0.37f, 0.37f, 0.86f, 0.57f, 1.38f, 0.57f)
                curveToRelative(1.08f, 0.0f, 1.96f, -0.88f, 1.96f, -1.96f)
                lineTo(20.99f, 12.0f)
                curveTo(21.0f, 10.34f, 19.66f, 9.0f, 18.0f, 9.0f)
                close()
            }
        }
        return _cake!!
    }

private var _cake: ImageVector? = null

public val Icons.Filled.Calculate: ImageVector
    get() {
        if (_calculate != null) {
            return _calculate!!
        }
        _calculate = materialIcon(name = "Filled.Calculate") {
            materialPath {
                moveTo(19.0f, 3.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 3.0f, 3.0f, 3.9f, 3.0f, 5.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveTo(21.0f, 3.9f, 20.1f, 3.0f, 19.0f, 3.0f)
                close()
                moveTo(13.03f, 7.06f)
                lineTo(14.09f, 6.0f)
                lineToRelative(1.41f, 1.41f)
                lineTo(16.91f, 6.0f)
                lineToRelative(1.06f, 1.06f)
                lineToRelative(-1.41f, 1.41f)
                lineToRelative(1.41f, 1.41f)
                lineToRelative(-1.06f, 1.06f)
                lineTo(15.5f, 9.54f)
                lineToRelative(-1.41f, 1.41f)
                lineToRelative(-1.06f, -1.06f)
                lineToRelative(1.41f, -1.41f)
                lineTo(13.03f, 7.06f)
                close()
                moveTo(6.25f, 7.72f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(1.5f)
                horizontalLineToRelative(-5.0f)
                verticalLineTo(7.72f)
                close()
                moveTo(11.5f, 16.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(-1.5f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(1.5f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(16.0f)
                close()
                moveTo(18.0f, 17.25f)
                horizontalLineToRelative(-5.0f)
                verticalLineToRelative(-1.5f)
                horizontalLineToRelative(5.0f)
                verticalLineTo(17.25f)
                close()
                moveTo(18.0f, 14.75f)
                horizontalLineToRelative(-5.0f)
                verticalLineToRelative(-1.5f)
                horizontalLineToRelative(5.0f)
                verticalLineTo(14.75f)
                close()
            }
        }
        return _calculate!!
    }

private var _calculate: ImageVector? = null

public val Icons.Filled.CalendarMonth: ImageVector
    get() {
        if (_calendarMonth != null) {
            return _calendarMonth!!
        }
        _calendarMonth = materialIcon(name = "Filled.CalendarMonth") {
            materialPath {
                moveTo(19.0f, 4.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineTo(2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(2.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(5.0f)
                curveTo(3.89f, 4.0f, 3.01f, 4.9f, 3.01f, 6.0f)
                lineTo(3.0f, 20.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(6.0f)
                curveTo(21.0f, 4.9f, 20.1f, 4.0f, 19.0f, 4.0f)
                close()
                moveTo(19.0f, 20.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(10.0f)
                horizontalLineToRelative(14.0f)
                verticalLineTo(20.0f)
                close()
                moveTo(9.0f, 14.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(13.0f, 14.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(17.0f, 14.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(9.0f, 18.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(18.0f)
                close()
                moveTo(13.0f, 18.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(18.0f)
                close()
                moveTo(17.0f, 18.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(18.0f)
                close()
            }
        }
        return _calendarMonth!!
    }

private var _calendarMonth: ImageVector? = null

public val Icons.Filled.CalendarToday: ImageVector
    get() {
        if (_calendarToday != null) {
            return _calendarToday!!
        }
        _calendarToday = materialIcon(name = "Filled.CalendarToday") {
            materialPath {
                moveTo(20.0f, 3.0f)
                horizontalLineToRelative(-1.0f)
                lineTo(19.0f, 1.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                lineTo(7.0f, 3.0f)
                lineTo(7.0f, 1.0f)
                lineTo(5.0f, 1.0f)
                verticalLineToRelative(2.0f)
                lineTo(4.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(16.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(20.0f, 21.0f)
                lineTo(4.0f, 21.0f)
                lineTo(4.0f, 8.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(13.0f)
                close()
            }
        }
        return _calendarToday!!
    }

private var _calendarToday: ImageVector? = null

public val Icons.Filled.CardGiftcard: ImageVector
    get() {
        if (_cardGiftcard != null) {
            return _cardGiftcard!!
        }
        _cardGiftcard = materialIcon(name = "Filled.CardGiftcard") {
            materialPath {
                moveTo(20.0f, 6.0f)
                horizontalLineToRelative(-2.18f)
                curveToRelative(0.11f, -0.31f, 0.18f, -0.65f, 0.18f, -1.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                curveToRelative(-1.05f, 0.0f, -1.96f, 0.54f, -2.5f, 1.35f)
                lineToRelative(-0.5f, 0.67f)
                lineToRelative(-0.5f, -0.68f)
                curveTo(10.96f, 2.54f, 10.05f, 2.0f, 9.0f, 2.0f)
                curveTo(7.34f, 2.0f, 6.0f, 3.34f, 6.0f, 5.0f)
                curveToRelative(0.0f, 0.35f, 0.07f, 0.69f, 0.18f, 1.0f)
                lineTo(4.0f, 6.0f)
                curveToRelative(-1.11f, 0.0f, -1.99f, 0.89f, -1.99f, 2.0f)
                lineTo(2.0f, 19.0f)
                curveToRelative(0.0f, 1.11f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.11f, 0.0f, 2.0f, -0.89f, 2.0f, -2.0f)
                lineTo(22.0f, 8.0f)
                curveToRelative(0.0f, -1.11f, -0.89f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(15.0f, 4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                close()
                moveTo(9.0f, 4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                close()
                moveTo(20.0f, 19.0f)
                lineTo(4.0f, 19.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(20.0f, 14.0f)
                lineTo(4.0f, 14.0f)
                lineTo(4.0f, 8.0f)
                horizontalLineToRelative(5.08f)
                lineTo(7.0f, 10.83f)
                lineTo(8.62f, 12.0f)
                lineTo(11.0f, 8.76f)
                lineToRelative(1.0f, -1.36f)
                lineToRelative(1.0f, 1.36f)
                lineTo(15.38f, 12.0f)
                lineTo(17.0f, 10.83f)
                lineTo(14.92f, 8.0f)
                lineTo(20.0f, 8.0f)
                verticalLineToRelative(6.0f)
                close()
            }
        }
        return _cardGiftcard!!
    }

private var _cardGiftcard: ImageVector? = null

public val Icons.Filled.Category: ImageVector
    get() {
        if (_category != null) {
            return _category!!
        }
        _category = materialIcon(name = "Filled.Category") {
            materialPath {
                moveTo(12.0f, 2.0f)
                lineToRelative(-5.5f, 9.0f)
                horizontalLineToRelative(11.0f)
                close()
            }
            materialPath {
                moveTo(17.5f, 17.5f)
                moveToRelative(-4.5f, 0.0f)
                arcToRelative(4.5f, 4.5f, 0.0f, true, true, 9.0f, 0.0f)
                arcToRelative(4.5f, 4.5f, 0.0f, true, true, -9.0f, 0.0f)
            }
            materialPath {
                moveTo(3.0f, 13.5f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(8.0f)
                horizontalLineTo(3.0f)
                close()
            }
        }
        return _category!!
    }

private var _category: ImageVector? = null

public val Icons.Filled.Celebration: ImageVector
    get() {
        if (_celebration != null) {
            return _celebration!!
        }
        _celebration = materialIcon(name = "Filled.Celebration") {
            materialPath {
                moveTo(2.0f, 22.0f)
                lineToRelative(14.0f, -5.0f)
                lineToRelative(-9.0f, -9.0f)
                close()
            }
            materialPath {
                moveTo(14.53f, 12.53f)
                lineToRelative(5.59f, -5.59f)
                curveToRelative(0.49f, -0.49f, 1.28f, -0.49f, 1.77f, 0.0f)
                lineToRelative(0.59f, 0.59f)
                lineToRelative(1.06f, -1.06f)
                lineToRelative(-0.59f, -0.59f)
                curveToRelative(-1.07f, -1.07f, -2.82f, -1.07f, -3.89f, 0.0f)
                lineToRelative(-5.59f, 5.59f)
                lineTo(14.53f, 12.53f)
                close()
            }
            materialPath {
                moveTo(10.06f, 6.88f)
                lineTo(9.47f, 7.47f)
                lineToRelative(1.06f, 1.06f)
                lineToRelative(0.59f, -0.59f)
                curveToRelative(1.07f, -1.07f, 1.07f, -2.82f, 0.0f, -3.89f)
                lineToRelative(-0.59f, -0.59f)
                lineTo(9.47f, 4.53f)
                lineToRelative(0.59f, 0.59f)
                curveTo(10.54f, 5.6f, 10.54f, 6.4f, 10.06f, 6.88f)
                close()
            }
            materialPath {
                moveTo(17.06f, 11.88f)
                lineToRelative(-1.59f, 1.59f)
                lineToRelative(1.06f, 1.06f)
                lineToRelative(1.59f, -1.59f)
                curveToRelative(0.49f, -0.49f, 1.28f, -0.49f, 1.77f, 0.0f)
                lineToRelative(1.61f, 1.61f)
                lineToRelative(1.06f, -1.06f)
                lineToRelative(-1.61f, -1.61f)
                curveTo(19.87f, 10.81f, 18.13f, 10.81f, 17.06f, 11.88f)
                close()
            }
            materialPath {
                moveTo(15.06f, 5.88f)
                lineToRelative(-3.59f, 3.59f)
                lineToRelative(1.06f, 1.06f)
                lineToRelative(3.59f, -3.59f)
                curveToRelative(1.07f, -1.07f, 1.07f, -2.82f, 0.0f, -3.89f)
                lineToRelative(-1.59f, -1.59f)
                lineToRelative(-1.06f, 1.06f)
                lineToRelative(1.59f, 1.59f)
                curveTo(15.54f, 4.6f, 15.54f, 5.4f, 15.06f, 5.88f)
                close()
            }
        }
        return _celebration!!
    }

private var _celebration: ImageVector? = null

public val Icons.Filled.Chair: ImageVector
    get() {
        if (_chair != null) {
            return _chair!!
        }
        _chair = materialIcon(name = "Filled.Chair") {
            materialPath {
                moveTo(7.0f, 11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(-2.0f)
                curveToRelative(0.0f, -1.86f, 1.28f, -3.41f, 3.0f, -3.86f)
                verticalLineTo(6.0f)
                curveToRelative(0.0f, -1.65f, -1.35f, -3.0f, -3.0f, -3.0f)
                horizontalLineTo(7.0f)
                curveTo(5.35f, 3.0f, 4.0f, 4.35f, 4.0f, 6.0f)
                verticalLineToRelative(1.14f)
                curveTo(5.72f, 7.59f, 7.0f, 9.14f, 7.0f, 11.0f)
                close()
            }
            materialPath {
                moveTo(21.0f, 9.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(4.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(5.0f)
                curveToRelative(0.0f, 1.65f, 1.35f, 3.0f, 3.0f, 3.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                curveToRelative(1.65f, 0.0f, 3.0f, -1.35f, 3.0f, -3.0f)
                verticalLineToRelative(-5.0f)
                curveTo(23.0f, 9.9f, 22.1f, 9.0f, 21.0f, 9.0f)
                close()
            }
        }
        return _chair!!
    }

private var _chair: ImageVector? = null

public val Icons.Filled.Checkroom: ImageVector
    get() {
        if (_checkroom != null) {
            return _checkroom!!
        }
        _checkroom = materialIcon(name = "Filled.Checkroom") {
            materialPath {
                moveTo(21.6f, 18.2f)
                lineTo(13.0f, 11.75f)
                verticalLineToRelative(-0.91f)
                curveToRelative(1.65f, -0.49f, 2.8f, -2.17f, 2.43f, -4.05f)
                curveToRelative(-0.26f, -1.31f, -1.3f, -2.4f, -2.61f, -2.7f)
                curveTo(10.54f, 3.57f, 8.5f, 5.3f, 8.5f, 7.5f)
                horizontalLineToRelative(2.0f)
                curveTo(10.5f, 6.67f, 11.17f, 6.0f, 12.0f, 6.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                curveToRelative(0.0f, 0.84f, -0.69f, 1.52f, -1.53f, 1.5f)
                curveTo(11.43f, 8.99f, 11.0f, 9.45f, 11.0f, 9.99f)
                verticalLineToRelative(1.76f)
                lineTo(2.4f, 18.2f)
                curveTo(1.63f, 18.78f, 2.04f, 20.0f, 3.0f, 20.0f)
                horizontalLineToRelative(9.0f)
                horizontalLineToRelative(9.0f)
                curveTo(21.96f, 20.0f, 22.37f, 18.78f, 21.6f, 18.2f)
                close()
                moveTo(6.0f, 18.0f)
                lineToRelative(6.0f, -4.5f)
                lineToRelative(6.0f, 4.5f)
                horizontalLineTo(6.0f)
                close()
            }
        }
        return _checkroom!!
    }

private var _checkroom: ImageVector? = null

public val Icons.Filled.ChevronRight: ImageVector
    get() {
        if (_chevronRight != null) {
            return _chevronRight!!
        }
        _chevronRight = materialIcon(name = "Filled.ChevronRight") {
            materialPath {
                moveTo(10.0f, 6.0f)
                lineTo(8.59f, 7.41f)
                lineTo(13.17f, 12.0f)
                lineToRelative(-4.58f, 4.59f)
                lineTo(10.0f, 18.0f)
                lineToRelative(6.0f, -6.0f)
                close()
            }
        }
        return _chevronRight!!
    }

private var _chevronRight: ImageVector? = null

public val Icons.Filled.ChildCare: ImageVector
    get() {
        if (_childCare != null) {
            return _childCare!!
        }
        _childCare = materialIcon(name = "Filled.ChildCare") {
            materialPath {
                moveTo(14.5f, 10.5f)
                moveToRelative(-1.25f, 0.0f)
                arcToRelative(1.25f, 1.25f, 0.0f, true, true, 2.5f, 0.0f)
                arcToRelative(1.25f, 1.25f, 0.0f, true, true, -2.5f, 0.0f)
            }
            materialPath {
                moveTo(9.5f, 10.5f)
                moveToRelative(-1.25f, 0.0f)
                arcToRelative(1.25f, 1.25f, 0.0f, true, true, 2.5f, 0.0f)
                arcToRelative(1.25f, 1.25f, 0.0f, true, true, -2.5f, 0.0f)
            }
            materialPath {
                moveTo(22.94f, 12.66f)
                curveToRelative(0.04f, -0.21f, 0.06f, -0.43f, 0.06f, -0.66f)
                reflectiveCurveToRelative(-0.02f, -0.45f, -0.06f, -0.66f)
                curveToRelative(-0.25f, -1.51f, -1.36f, -2.74f, -2.81f, -3.17f)
                curveToRelative(-0.53f, -1.12f, -1.28f, -2.1f, -2.19f, -2.91f)
                curveTo(16.36f, 3.85f, 14.28f, 3.0f, 12.0f, 3.0f)
                reflectiveCurveToRelative(-4.36f, 0.85f, -5.94f, 2.26f)
                curveToRelative(-0.92f, 0.81f, -1.67f, 1.8f, -2.19f, 2.91f)
                curveToRelative(-1.45f, 0.43f, -2.56f, 1.65f, -2.81f, 3.17f)
                curveToRelative(-0.04f, 0.21f, -0.06f, 0.43f, -0.06f, 0.66f)
                reflectiveCurveToRelative(0.02f, 0.45f, 0.06f, 0.66f)
                curveToRelative(0.25f, 1.51f, 1.36f, 2.74f, 2.81f, 3.17f)
                curveToRelative(0.52f, 1.11f, 1.27f, 2.09f, 2.17f, 2.89f)
                curveTo(7.62f, 20.14f, 9.71f, 21.0f, 12.0f, 21.0f)
                reflectiveCurveToRelative(4.38f, -0.86f, 5.97f, -2.28f)
                curveToRelative(0.9f, -0.8f, 1.65f, -1.79f, 2.17f, -2.89f)
                curveToRelative(1.44f, -0.43f, 2.55f, -1.65f, 2.8f, -3.17f)
                close()
                moveTo(19.0f, 14.0f)
                curveToRelative(-0.1f, 0.0f, -0.19f, -0.02f, -0.29f, -0.03f)
                curveToRelative(-0.2f, 0.67f, -0.49f, 1.29f, -0.86f, 1.86f)
                curveTo(16.6f, 17.74f, 14.45f, 19.0f, 12.0f, 19.0f)
                reflectiveCurveToRelative(-4.6f, -1.26f, -5.85f, -3.17f)
                curveToRelative(-0.37f, -0.57f, -0.66f, -1.19f, -0.86f, -1.86f)
                curveToRelative(-0.1f, 0.01f, -0.19f, 0.03f, -0.29f, 0.03f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                curveToRelative(0.1f, 0.0f, 0.19f, 0.02f, 0.29f, 0.03f)
                curveToRelative(0.2f, -0.67f, 0.49f, -1.29f, 0.86f, -1.86f)
                curveTo(7.4f, 6.26f, 9.55f, 5.0f, 12.0f, 5.0f)
                reflectiveCurveToRelative(4.6f, 1.26f, 5.85f, 3.17f)
                curveToRelative(0.37f, 0.57f, 0.66f, 1.19f, 0.86f, 1.86f)
                curveToRelative(0.1f, -0.01f, 0.19f, -0.03f, 0.29f, -0.03f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                close()
                moveTo(7.5f, 14.0f)
                curveToRelative(0.76f, 1.77f, 2.49f, 3.0f, 4.5f, 3.0f)
                reflectiveCurveToRelative(3.74f, -1.23f, 4.5f, -3.0f)
                horizontalLineToRelative(-9.0f)
                close()
            }
        }
        return _childCare!!
    }

private var _childCare: ImageVector? = null

public val Icons.Filled.CleaningServices: ImageVector
    get() {
        if (_cleaningServices != null) {
            return _cleaningServices!!
        }
        _cleaningServices = materialIcon(name = "Filled.CleaningServices") {
            materialPath {
                moveTo(16.0f, 11.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineTo(3.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-2.0f)
                curveTo(9.9f, 1.0f, 9.0f, 1.9f, 9.0f, 3.0f)
                verticalLineToRelative(8.0f)
                horizontalLineTo(8.0f)
                curveToRelative(-2.76f, 0.0f, -5.0f, 2.24f, -5.0f, 5.0f)
                verticalLineToRelative(7.0f)
                horizontalLineToRelative(18.0f)
                verticalLineToRelative(-7.0f)
                curveTo(21.0f, 13.24f, 18.76f, 11.0f, 16.0f, 11.0f)
                close()
                moveTo(19.0f, 21.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-5.0f)
                curveToRelative(0.0f, -1.65f, 1.35f, -3.0f, 3.0f, -3.0f)
                horizontalLineToRelative(8.0f)
                curveToRelative(1.65f, 0.0f, 3.0f, 1.35f, 3.0f, 3.0f)
                verticalLineTo(21.0f)
                close()
            }
        }
        return _cleaningServices!!
    }

private var _cleaningServices: ImageVector? = null

public val Icons.Filled.Commute: ImageVector
    get() {
        if (_commute != null) {
            return _commute!!
        }
        _commute = materialIcon(name = "Filled.Commute") {
            materialPath {
                moveTo(12.0f, 4.0f)
                lineTo(5.0f, 4.0f)
                curveTo(3.34f, 4.0f, 2.0f, 5.34f, 2.0f, 7.0f)
                verticalLineToRelative(8.0f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                lineToRelative(-1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(1.0f)
                lineToRelative(2.0f, -2.03f)
                lineTo(9.0f, 18.0f)
                verticalLineToRelative(-5.0f)
                lineTo(4.0f, 13.0f)
                lineTo(4.0f, 5.98f)
                lineTo(13.0f, 6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                lineTo(15.0f, 7.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                close()
                moveTo(5.0f, 14.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                close()
                moveTo(20.57f, 9.66f)
                curveToRelative(-0.14f, -0.4f, -0.52f, -0.66f, -0.97f, -0.66f)
                horizontalLineToRelative(-7.19f)
                curveToRelative(-0.46f, 0.0f, -0.83f, 0.26f, -0.98f, 0.66f)
                lineTo(10.0f, 13.77f)
                lineToRelative(0.01f, 5.51f)
                curveToRelative(0.0f, 0.38f, 0.31f, 0.72f, 0.69f, 0.72f)
                horizontalLineToRelative(0.62f)
                curveToRelative(0.38f, 0.0f, 0.68f, -0.38f, 0.68f, -0.76f)
                lineTo(12.0f, 18.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(1.24f)
                curveToRelative(0.0f, 0.38f, 0.31f, 0.76f, 0.69f, 0.76f)
                horizontalLineToRelative(0.61f)
                curveToRelative(0.38f, 0.0f, 0.69f, -0.34f, 0.69f, -0.72f)
                lineToRelative(0.01f, -1.37f)
                verticalLineToRelative(-4.14f)
                lineToRelative(-1.43f, -4.11f)
                close()
                moveTo(12.41f, 10.0f)
                horizontalLineToRelative(7.19f)
                lineToRelative(1.03f, 3.0f)
                horizontalLineToRelative(-9.25f)
                lineToRelative(1.03f, -3.0f)
                close()
                moveTo(12.0f, 16.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(20.0f, 16.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _commute!!
    }

private var _commute: ImageVector? = null

public val Icons.Filled.Computer: ImageVector
    get() {
        if (_computer != null) {
            return _computer!!
        }
        _computer = materialIcon(name = "Filled.Computer") {
            materialPath {
                moveTo(20.0f, 18.0f)
                curveToRelative(1.1f, 0.0f, 1.99f, -0.9f, 1.99f, -2.0f)
                lineTo(22.0f, 6.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(10.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineTo(0.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(24.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-4.0f)
                close()
                moveTo(4.0f, 6.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(10.0f)
                horizontalLineTo(4.0f)
                verticalLineTo(6.0f)
                close()
            }
        }
        return _computer!!
    }

private var _computer: ImageVector? = null

public val Icons.Filled.ContentCut: ImageVector
    get() {
        if (_contentCut != null) {
            return _contentCut!!
        }
        _contentCut = materialIcon(name = "Filled.ContentCut") {
            materialPath {
                moveTo(9.64f, 7.64f)
                curveToRelative(0.23f, -0.5f, 0.36f, -1.05f, 0.36f, -1.64f)
                curveToRelative(0.0f, -2.21f, -1.79f, -4.0f, -4.0f, -4.0f)
                reflectiveCurveTo(2.0f, 3.79f, 2.0f, 6.0f)
                reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
                curveToRelative(0.59f, 0.0f, 1.14f, -0.13f, 1.64f, -0.36f)
                lineTo(10.0f, 12.0f)
                lineToRelative(-2.36f, 2.36f)
                curveTo(7.14f, 14.13f, 6.59f, 14.0f, 6.0f, 14.0f)
                curveToRelative(-2.21f, 0.0f, -4.0f, 1.79f, -4.0f, 4.0f)
                reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
                reflectiveCurveToRelative(4.0f, -1.79f, 4.0f, -4.0f)
                curveToRelative(0.0f, -0.59f, -0.13f, -1.14f, -0.36f, -1.64f)
                lineTo(12.0f, 14.0f)
                lineToRelative(7.0f, 7.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(-1.0f)
                lineTo(9.64f, 7.64f)
                close()
                moveTo(6.0f, 8.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.89f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.89f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                close()
                moveTo(6.0f, 20.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.89f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.89f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                close()
                moveTo(12.0f, 12.5f)
                curveToRelative(-0.28f, 0.0f, -0.5f, -0.22f, -0.5f, -0.5f)
                reflectiveCurveToRelative(0.22f, -0.5f, 0.5f, -0.5f)
                reflectiveCurveToRelative(0.5f, 0.22f, 0.5f, 0.5f)
                reflectiveCurveToRelative(-0.22f, 0.5f, -0.5f, 0.5f)
                close()
                moveTo(19.0f, 3.0f)
                lineToRelative(-6.0f, 6.0f)
                lineToRelative(2.0f, 2.0f)
                lineToRelative(7.0f, -7.0f)
                lineTo(22.0f, 3.0f)
                close()
            }
        }
        return _contentCut!!
    }

private var _contentCut: ImageVector? = null

public val Icons.Filled.CreditCard: ImageVector
    get() {
        if (_creditCard != null) {
            return _creditCard!!
        }
        _creditCard = materialIcon(name = "Filled.CreditCard") {
            materialPath {
                moveTo(20.0f, 4.0f)
                lineTo(4.0f, 4.0f)
                curveToRelative(-1.11f, 0.0f, -1.99f, 0.89f, -1.99f, 2.0f)
                lineTo(2.0f, 18.0f)
                curveToRelative(0.0f, 1.11f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.11f, 0.0f, 2.0f, -0.89f, 2.0f, -2.0f)
                lineTo(22.0f, 6.0f)
                curveToRelative(0.0f, -1.11f, -0.89f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(20.0f, 18.0f)
                lineTo(4.0f, 18.0f)
                verticalLineToRelative(-6.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(6.0f)
                close()
                moveTo(20.0f, 8.0f)
                lineTo(4.0f, 8.0f)
                lineTo(4.0f, 6.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
        return _creditCard!!
    }

private var _creditCard: ImageVector? = null

public val Icons.Filled.CurrencyRupee: ImageVector
    get() {
        if (_currencyRupee != null) {
            return _currencyRupee!!
        }
        _currencyRupee = materialIcon(name = "Filled.CurrencyRupee") {
            materialPath {
                moveTo(13.66f, 7.0f)
                curveTo(13.1f, 5.82f, 11.9f, 5.0f, 10.5f, 5.0f)
                lineTo(6.0f, 5.0f)
                verticalLineTo(3.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(2.0f)
                lineToRelative(-3.26f, 0.0f)
                curveToRelative(0.48f, 0.58f, 0.84f, 1.26f, 1.05f, 2.0f)
                lineTo(18.0f, 7.0f)
                verticalLineToRelative(2.0f)
                lineToRelative(-2.02f, 0.0f)
                curveToRelative(-0.25f, 2.8f, -2.61f, 5.0f, -5.48f, 5.0f)
                horizontalLineTo(9.77f)
                lineToRelative(6.73f, 7.0f)
                horizontalLineToRelative(-2.77f)
                lineTo(7.0f, 14.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(3.5f)
                curveToRelative(1.76f, 0.0f, 3.22f, -1.3f, 3.46f, -3.0f)
                lineTo(6.0f, 9.0f)
                verticalLineTo(7.0f)
                lineTo(13.66f, 7.0f)
                close()
            }
        }
        return _currencyRupee!!
    }

private var _currencyRupee: ImageVector? = null

public val Icons.Filled.DeleteForever: ImageVector
    get() {
        if (_deleteForever != null) {
            return _deleteForever!!
        }
        _deleteForever = materialIcon(name = "Filled.DeleteForever") {
            materialPath {
                moveTo(6.0f, 19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(8.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(18.0f, 7.0f)
                lineTo(6.0f, 7.0f)
                verticalLineToRelative(12.0f)
                close()
                moveTo(8.46f, 11.88f)
                lineToRelative(1.41f, -1.41f)
                lineTo(12.0f, 12.59f)
                lineToRelative(2.12f, -2.12f)
                lineToRelative(1.41f, 1.41f)
                lineTo(13.41f, 14.0f)
                lineToRelative(2.12f, 2.12f)
                lineToRelative(-1.41f, 1.41f)
                lineTo(12.0f, 15.41f)
                lineToRelative(-2.12f, 2.12f)
                lineToRelative(-1.41f, -1.41f)
                lineTo(10.59f, 14.0f)
                lineToRelative(-2.13f, -2.12f)
                close()
                moveTo(15.5f, 4.0f)
                lineToRelative(-1.0f, -1.0f)
                horizontalLineToRelative(-5.0f)
                lineToRelative(-1.0f, 1.0f)
                lineTo(5.0f, 4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(14.0f)
                lineTo(19.0f, 4.0f)
                close()
            }
        }
        return _deleteForever!!
    }

private var _deleteForever: ImageVector? = null

public val Icons.Filled.Description: ImageVector
    get() {
        if (_description != null) {
            return _description!!
        }
        _description = materialIcon(name = "Filled.Description") {
            materialPath {
                moveTo(14.0f, 2.0f)
                lineTo(6.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(4.0f, 20.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 1.99f, 2.0f)
                lineTo(18.0f, 22.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(20.0f, 8.0f)
                lineToRelative(-6.0f, -6.0f)
                close()
                moveTo(16.0f, 18.0f)
                lineTo(8.0f, 18.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(16.0f, 14.0f)
                lineTo(8.0f, 14.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(13.0f, 9.0f)
                lineTo(13.0f, 3.5f)
                lineTo(18.5f, 9.0f)
                lineTo(13.0f, 9.0f)
                close()
            }
        }
        return _description!!
    }

private var _description: ImageVector? = null

public val Icons.Filled.Diamond: ImageVector
    get() {
        if (_diamond != null) {
            return _diamond!!
        }
        _diamond = materialIcon(name = "Filled.Diamond") {
            materialPath {
                moveTo(12.16f, 3.0f)
                lineToRelative(-0.32f, 0.0f)
                lineToRelative(-2.63f, 5.25f)
                lineToRelative(5.58f, 0.0f)
                close()
            }
            materialPath {
                moveTo(16.46f, 8.25f)
                lineToRelative(5.16f, 0.0f)
                lineToRelative(-2.62f, -5.25f)
                lineToRelative(-5.16f, 0.0f)
                close()
            }
            materialPath {
                moveTo(21.38f, 9.75f)
                lineToRelative(-8.63f, 0.0f)
                lineToRelative(0.0f, 10.35f)
                close()
            }
            materialPath {
                moveTo(11.25f, 20.1f)
                lineToRelative(0.0f, -10.35f)
                lineToRelative(-8.63f, 0.0f)
                close()
            }
            materialPath {
                moveTo(7.54f, 8.25f)
                lineToRelative(2.62f, -5.25f)
                lineToRelative(-5.16f, 0.0f)
                lineToRelative(-2.62f, 5.25f)
                close()
            }
        }
        return _diamond!!
    }

private var _diamond: ImageVector? = null

public val Icons.Filled.DirectionsBus: ImageVector
    get() {
        if (_directionsBus != null) {
            return _directionsBus!!
        }
        _directionsBus = materialIcon(name = "Filled.DirectionsBus") {
            materialPath {
                moveTo(4.0f, 16.0f)
                curveToRelative(0.0f, 0.88f, 0.39f, 1.67f, 1.0f, 2.22f)
                lineTo(5.0f, 20.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.78f)
                curveToRelative(0.61f, -0.55f, 1.0f, -1.34f, 1.0f, -2.22f)
                lineTo(20.0f, 6.0f)
                curveToRelative(0.0f, -3.5f, -3.58f, -4.0f, -8.0f, -4.0f)
                reflectiveCurveToRelative(-8.0f, 0.5f, -8.0f, 4.0f)
                verticalLineToRelative(10.0f)
                close()
                moveTo(7.5f, 17.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(6.67f, 14.0f, 7.5f, 14.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(8.33f, 17.0f, 7.5f, 17.0f)
                close()
                moveTo(16.5f, 17.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
                moveTo(18.0f, 11.0f)
                lineTo(6.0f, 11.0f)
                lineTo(6.0f, 6.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(5.0f)
                close()
            }
        }
        return _directionsBus!!
    }

private var _directionsBus: ImageVector? = null

public val Icons.Filled.DirectionsCar: ImageVector
    get() {
        if (_directionsCar != null) {
            return _directionsCar!!
        }
        _directionsCar = materialIcon(name = "Filled.DirectionsCar") {
            materialPath {
                moveTo(18.92f, 6.01f)
                curveTo(18.72f, 5.42f, 18.16f, 5.0f, 17.5f, 5.0f)
                horizontalLineToRelative(-11.0f)
                curveToRelative(-0.66f, 0.0f, -1.21f, 0.42f, -1.42f, 1.01f)
                lineTo(3.0f, 12.0f)
                verticalLineToRelative(8.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-8.0f)
                lineToRelative(-2.08f, -5.99f)
                close()
                moveTo(6.5f, 16.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(5.67f, 13.0f, 6.5f, 13.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(7.33f, 16.0f, 6.5f, 16.0f)
                close()
                moveTo(17.5f, 16.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
                moveTo(5.0f, 11.0f)
                lineToRelative(1.5f, -4.5f)
                horizontalLineToRelative(11.0f)
                lineTo(19.0f, 11.0f)
                lineTo(5.0f, 11.0f)
                close()
            }
        }
        return _directionsCar!!
    }

private var _directionsCar: ImageVector? = null

public val Icons.Filled.Download: ImageVector
    get() {
        if (_download != null) {
            return _download!!
        }
        _download = materialIcon(name = "Filled.Download") {
            materialPath {
                moveTo(5.0f, 20.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(20.0f)
                close()
                moveTo(19.0f, 9.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(6.0f)
                horizontalLineTo(5.0f)
                lineToRelative(7.0f, 7.0f)
                lineTo(19.0f, 9.0f)
                close()
            }
        }
        return _download!!
    }

private var _download: ImageVector? = null

public val Icons.Filled.EmojiEvents: ImageVector
    get() {
        if (_emojiEvents != null) {
            return _emojiEvents!!
        }
        _emojiEvents = materialIcon(name = "Filled.EmojiEvents") {
            materialPath {
                moveTo(19.0f, 5.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 5.0f, 3.0f, 5.9f, 3.0f, 7.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 2.55f, 1.92f, 4.63f, 4.39f, 4.94f)
                curveToRelative(0.63f, 1.5f, 1.98f, 2.63f, 3.61f, 2.96f)
                verticalLineTo(19.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-3.1f)
                curveToRelative(1.63f, -0.33f, 2.98f, -1.46f, 3.61f, -2.96f)
                curveTo(19.08f, 12.63f, 21.0f, 10.55f, 21.0f, 8.0f)
                verticalLineTo(7.0f)
                curveTo(21.0f, 5.9f, 20.1f, 5.0f, 19.0f, 5.0f)
                close()
                moveTo(5.0f, 8.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(3.82f)
                curveTo(5.84f, 10.4f, 5.0f, 9.3f, 5.0f, 8.0f)
                close()
                moveTo(19.0f, 8.0f)
                curveToRelative(0.0f, 1.3f, -0.84f, 2.4f, -2.0f, 2.82f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(8.0f)
                close()
            }
        }
        return _emojiEvents!!
    }

private var _emojiEvents: ImageVector? = null

public val Icons.Filled.EventRepeat: ImageVector
    get() {
        if (_eventRepeat != null) {
            return _eventRepeat!!
        }
        _eventRepeat = materialIcon(name = "Filled.EventRepeat") {
            materialPath {
                moveTo(21.0f, 12.0f)
                verticalLineTo(6.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineTo(2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(2.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 4.0f, 3.0f, 4.9f, 3.0f, 6.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(10.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(21.0f)
                close()
                moveTo(15.64f, 20.0f)
                curveToRelative(0.43f, 1.45f, 1.77f, 2.5f, 3.36f, 2.5f)
                curveToRelative(1.93f, 0.0f, 3.5f, -1.57f, 3.5f, -3.5f)
                reflectiveCurveToRelative(-1.57f, -3.5f, -3.5f, -3.5f)
                curveToRelative(-0.95f, 0.0f, -1.82f, 0.38f, -2.45f, 1.0f)
                lineToRelative(1.45f, 0.0f)
                verticalLineTo(18.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(1.5f)
                lineToRelative(0.0f, 1.43f)
                curveTo(16.4f, 14.55f, 17.64f, 14.0f, 19.0f, 14.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, 2.24f, 5.0f, 5.0f)
                reflectiveCurveToRelative(-2.24f, 5.0f, -5.0f, 5.0f)
                curveToRelative(-2.42f, 0.0f, -4.44f, -1.72f, -4.9f, -4.0f)
                lineTo(15.64f, 20.0f)
                close()
            }
        }
        return _eventRepeat!!
    }

private var _eventRepeat: ImageVector? = null

public val Icons.Filled.EvStation: ImageVector
    get() {
        if (_evStation != null) {
            return _evStation!!
        }
        _evStation = materialIcon(name = "Filled.EvStation") {
            materialPath {
                moveTo(19.77f, 7.23f)
                lineToRelative(0.01f, -0.01f)
                lineToRelative(-3.72f, -3.72f)
                lineTo(15.0f, 4.56f)
                lineToRelative(2.11f, 2.11f)
                curveToRelative(-0.94f, 0.36f, -1.61f, 1.26f, -1.61f, 2.33f)
                curveToRelative(0.0f, 1.38f, 1.12f, 2.5f, 2.5f, 2.5f)
                curveToRelative(0.36f, 0.0f, 0.69f, -0.08f, 1.0f, -0.21f)
                verticalLineToRelative(7.21f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineTo(14.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(6.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(16.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(-7.5f)
                horizontalLineToRelative(1.5f)
                verticalLineToRelative(5.0f)
                curveToRelative(0.0f, 1.38f, 1.12f, 2.5f, 2.5f, 2.5f)
                reflectiveCurveToRelative(2.5f, -1.12f, 2.5f, -2.5f)
                verticalLineTo(9.0f)
                curveToRelative(0.0f, -0.69f, -0.28f, -1.32f, -0.73f, -1.77f)
                close()
                moveTo(18.0f, 10.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(8.0f, 18.0f)
                verticalLineToRelative(-4.5f)
                horizontalLineTo(6.0f)
                lineTo(10.0f, 6.0f)
                verticalLineToRelative(5.0f)
                horizontalLineToRelative(2.0f)
                lineToRelative(-4.0f, 7.0f)
                close()
            }
        }
        return _evStation!!
    }

private var _evStation: ImageVector? = null

public val Icons.Filled.ExpandLess: ImageVector
    get() {
        if (_expandLess != null) {
            return _expandLess!!
        }
        _expandLess = materialIcon(name = "Filled.ExpandLess") {
            materialPath {
                moveTo(12.0f, 8.0f)
                lineToRelative(-6.0f, 6.0f)
                lineToRelative(1.41f, 1.41f)
                lineTo(12.0f, 10.83f)
                lineToRelative(4.59f, 4.58f)
                lineTo(18.0f, 14.0f)
                close()
            }
        }
        return _expandLess!!
    }

private var _expandLess: ImageVector? = null

public val Icons.Filled.ExpandMore: ImageVector
    get() {
        if (_expandMore != null) {
            return _expandMore!!
        }
        _expandMore = materialIcon(name = "Filled.ExpandMore") {
            materialPath {
                moveTo(16.59f, 8.59f)
                lineTo(12.0f, 13.17f)
                lineTo(7.41f, 8.59f)
                lineTo(6.0f, 10.0f)
                lineToRelative(6.0f, 6.0f)
                lineToRelative(6.0f, -6.0f)
                close()
            }
        }
        return _expandMore!!
    }

private var _expandMore: ImageVector? = null

public val Icons.Filled.Fastfood: ImageVector
    get() {
        if (_fastfood != null) {
            return _fastfood!!
        }
        _fastfood = materialIcon(name = "Filled.Fastfood") {
            materialPath {
                moveTo(18.06f, 22.99f)
                horizontalLineToRelative(1.66f)
                curveToRelative(0.84f, 0.0f, 1.53f, -0.64f, 1.63f, -1.46f)
                lineTo(23.0f, 5.05f)
                horizontalLineToRelative(-5.0f)
                lineTo(18.0f, 1.0f)
                horizontalLineToRelative(-1.97f)
                verticalLineToRelative(4.05f)
                horizontalLineToRelative(-4.97f)
                lineToRelative(0.3f, 2.34f)
                curveToRelative(1.71f, 0.47f, 3.31f, 1.32f, 4.27f, 2.26f)
                curveToRelative(1.44f, 1.42f, 2.43f, 2.89f, 2.43f, 5.29f)
                verticalLineToRelative(8.05f)
                close()
                moveTo(1.0f, 21.99f)
                lineTo(1.0f, 21.0f)
                horizontalLineToRelative(15.03f)
                verticalLineToRelative(0.99f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.01f, 1.0f)
                lineTo(2.01f, 22.99f)
                curveToRelative(-0.56f, 0.0f, -1.01f, -0.45f, -1.01f, -1.0f)
                close()
                moveTo(16.03f, 14.99f)
                curveToRelative(0.0f, -8.0f, -15.03f, -8.0f, -15.03f, 0.0f)
                horizontalLineToRelative(15.03f)
                close()
                moveTo(1.02f, 17.0f)
                horizontalLineToRelative(15.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-15.0f)
                close()
            }
        }
        return _fastfood!!
    }

private var _fastfood: ImageVector? = null

public val Icons.Filled.Feedback: ImageVector
    get() {
        if (_feedback != null) {
            return _feedback!!
        }
        _feedback = materialIcon(name = "Filled.Feedback") {
            materialPath {
                moveTo(20.0f, 2.0f)
                lineTo(4.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(2.0f, 22.0f)
                lineToRelative(4.0f, -4.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(13.0f, 14.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(13.0f, 10.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(11.0f, 6.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(4.0f)
                close()
            }
        }
        return _feedback!!
    }

private var _feedback: ImageVector? = null

public val Icons.Filled.FileUpload: ImageVector
    get() {
        if (_fileUpload != null) {
            return _fileUpload!!
        }
        _fileUpload = materialIcon(name = "Filled.FileUpload") {
            materialPath {
                moveTo(9.0f, 16.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(-6.0f)
                horizontalLineToRelative(4.0f)
                lineToRelative(-7.0f, -7.0f)
                lineToRelative(-7.0f, 7.0f)
                horizontalLineToRelative(4.0f)
                close()
                moveTo(5.0f, 18.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(2.0f)
                lineTo(5.0f, 20.0f)
                close()
            }
        }
        return _fileUpload!!
    }

private var _fileUpload: ImageVector? = null

public val Icons.Filled.FilterList: ImageVector
    get() {
        if (_filterList != null) {
            return _filterList!!
        }
        _filterList = materialIcon(name = "Filled.FilterList") {
            materialPath {
                moveTo(10.0f, 18.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(3.0f, 6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(18.0f)
                lineTo(21.0f, 6.0f)
                lineTo(3.0f, 6.0f)
                close()
                moveTo(6.0f, 13.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(-2.0f)
                lineTo(6.0f, 11.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
        return _filterList!!
    }

private var _filterList: ImageVector? = null

public val Icons.Filled.Fingerprint: ImageVector
    get() {
        if (_fingerprint != null) {
            return _fingerprint!!
        }
        _fingerprint = materialIcon(name = "Filled.Fingerprint") {
            materialPath {
                moveTo(17.81f, 4.47f)
                curveToRelative(-0.08f, 0.0f, -0.16f, -0.02f, -0.23f, -0.06f)
                curveTo(15.66f, 3.42f, 14.0f, 3.0f, 12.01f, 3.0f)
                curveToRelative(-1.98f, 0.0f, -3.86f, 0.47f, -5.57f, 1.41f)
                curveToRelative(-0.24f, 0.13f, -0.54f, 0.04f, -0.68f, -0.2f)
                curveToRelative(-0.13f, -0.24f, -0.04f, -0.55f, 0.2f, -0.68f)
                curveTo(7.82f, 2.52f, 9.86f, 2.0f, 12.01f, 2.0f)
                curveToRelative(2.13f, 0.0f, 3.99f, 0.47f, 6.03f, 1.52f)
                curveToRelative(0.25f, 0.13f, 0.34f, 0.43f, 0.21f, 0.67f)
                curveToRelative(-0.09f, 0.18f, -0.26f, 0.28f, -0.44f, 0.28f)
                close()
                moveTo(3.5f, 9.72f)
                curveToRelative(-0.1f, 0.0f, -0.2f, -0.03f, -0.29f, -0.09f)
                curveToRelative(-0.23f, -0.16f, -0.28f, -0.47f, -0.12f, -0.7f)
                curveToRelative(0.99f, -1.4f, 2.25f, -2.5f, 3.75f, -3.27f)
                curveTo(9.98f, 4.04f, 14.0f, 4.03f, 17.15f, 5.65f)
                curveToRelative(1.5f, 0.77f, 2.76f, 1.86f, 3.75f, 3.25f)
                curveToRelative(0.16f, 0.22f, 0.11f, 0.54f, -0.12f, 0.7f)
                curveToRelative(-0.23f, 0.16f, -0.54f, 0.11f, -0.7f, -0.12f)
                curveToRelative(-0.9f, -1.26f, -2.04f, -2.25f, -3.39f, -2.94f)
                curveToRelative(-2.87f, -1.47f, -6.54f, -1.47f, -9.4f, 0.01f)
                curveToRelative(-1.36f, 0.7f, -2.5f, 1.7f, -3.4f, 2.96f)
                curveToRelative(-0.08f, 0.14f, -0.23f, 0.21f, -0.39f, 0.21f)
                close()
                moveTo(9.75f, 21.79f)
                curveToRelative(-0.13f, 0.0f, -0.26f, -0.05f, -0.35f, -0.15f)
                curveToRelative(-0.87f, -0.87f, -1.34f, -1.43f, -2.01f, -2.64f)
                curveToRelative(-0.69f, -1.23f, -1.05f, -2.73f, -1.05f, -4.34f)
                curveToRelative(0.0f, -2.97f, 2.54f, -5.39f, 5.66f, -5.39f)
                reflectiveCurveToRelative(5.66f, 2.42f, 5.66f, 5.39f)
                curveToRelative(0.0f, 0.28f, -0.22f, 0.5f, -0.5f, 0.5f)
                reflectiveCurveToRelative(-0.5f, -0.22f, -0.5f, -0.5f)
                curveToRelative(0.0f, -2.42f, -2.09f, -4.39f, -4.66f, -4.39f)
                curveToRelative(-2.57f, 0.0f, -4.66f, 1.97f, -4.66f, 4.39f)
                curveToRelative(0.0f, 1.44f, 0.32f, 2.77f, 0.93f, 3.85f)
                curveToRelative(0.64f, 1.15f, 1.08f, 1.64f, 1.85f, 2.42f)
                curveToRelative(0.19f, 0.2f, 0.19f, 0.51f, 0.0f, 0.71f)
                curveToRelative(-0.11f, 0.1f, -0.24f, 0.15f, -0.37f, 0.15f)
                close()
                moveTo(16.92f, 19.94f)
                curveToRelative(-1.19f, 0.0f, -2.24f, -0.3f, -3.1f, -0.89f)
                curveToRelative(-1.49f, -1.01f, -2.38f, -2.65f, -2.38f, -4.39f)
                curveToRelative(0.0f, -0.28f, 0.22f, -0.5f, 0.5f, -0.5f)
                reflectiveCurveToRelative(0.5f, 0.22f, 0.5f, 0.5f)
                curveToRelative(0.0f, 1.41f, 0.72f, 2.74f, 1.94f, 3.56f)
                curveToRelative(0.71f, 0.48f, 1.54f, 0.71f, 2.54f, 0.71f)
                curveToRelative(0.24f, 0.0f, 0.64f, -0.03f, 1.04f, -0.1f)
                curveToRelative(0.27f, -0.05f, 0.53f, 0.13f, 0.58f, 0.41f)
                curveToRelative(0.05f, 0.27f, -0.13f, 0.53f, -0.41f, 0.58f)
                curveToRelative(-0.57f, 0.11f, -1.07f, 0.12f, -1.21f, 0.12f)
                close()
                moveTo(14.91f, 22.0f)
                curveToRelative(-0.04f, 0.0f, -0.09f, -0.01f, -0.13f, -0.02f)
                curveToRelative(-1.59f, -0.44f, -2.63f, -1.03f, -3.72f, -2.1f)
                curveToRelative(-1.4f, -1.39f, -2.17f, -3.24f, -2.17f, -5.22f)
                curveToRelative(0.0f, -1.62f, 1.38f, -2.94f, 3.08f, -2.94f)
                curveToRelative(1.7f, 0.0f, 3.08f, 1.32f, 3.08f, 2.94f)
                curveToRelative(0.0f, 1.07f, 0.93f, 1.94f, 2.08f, 1.94f)
                reflectiveCurveToRelative(2.08f, -0.87f, 2.08f, -1.94f)
                curveToRelative(0.0f, -3.77f, -3.25f, -6.83f, -7.25f, -6.83f)
                curveToRelative(-2.84f, 0.0f, -5.44f, 1.58f, -6.61f, 4.03f)
                curveToRelative(-0.39f, 0.81f, -0.59f, 1.76f, -0.59f, 2.8f)
                curveToRelative(0.0f, 0.78f, 0.07f, 2.01f, 0.67f, 3.61f)
                curveToRelative(0.1f, 0.26f, -0.03f, 0.55f, -0.29f, 0.64f)
                curveToRelative(-0.26f, 0.1f, -0.55f, -0.04f, -0.64f, -0.29f)
                curveToRelative(-0.49f, -1.31f, -0.73f, -2.61f, -0.73f, -3.96f)
                curveToRelative(0.0f, -1.2f, 0.23f, -2.29f, 0.68f, -3.24f)
                curveToRelative(1.33f, -2.79f, 4.28f, -4.6f, 7.51f, -4.6f)
                curveToRelative(4.55f, 0.0f, 8.25f, 3.51f, 8.25f, 7.83f)
                curveToRelative(0.0f, 1.62f, -1.38f, 2.94f, -3.08f, 2.94f)
                reflectiveCurveToRelative(-3.08f, -1.32f, -3.08f, -2.94f)
                curveToRelative(0.0f, -1.07f, -0.93f, -1.94f, -2.08f, -1.94f)
                reflectiveCurveToRelative(-2.08f, 0.87f, -2.08f, 1.94f)
                curveToRelative(0.0f, 1.71f, 0.66f, 3.31f, 1.87f, 4.51f)
                curveToRelative(0.95f, 0.94f, 1.86f, 1.46f, 3.27f, 1.85f)
                curveToRelative(0.27f, 0.07f, 0.42f, 0.35f, 0.35f, 0.61f)
                curveToRelative(-0.05f, 0.23f, -0.26f, 0.38f, -0.47f, 0.38f)
                close()
            }
        }
        return _fingerprint!!
    }

private var _fingerprint: ImageVector? = null

public val Icons.Filled.FitnessCenter: ImageVector
    get() {
        if (_fitnessCenter != null) {
            return _fitnessCenter!!
        }
        _fitnessCenter = materialIcon(name = "Filled.FitnessCenter") {
            materialPath {
                moveTo(20.57f, 14.86f)
                lineTo(22.0f, 13.43f)
                lineTo(20.57f, 12.0f)
                lineTo(17.0f, 15.57f)
                lineTo(8.43f, 7.0f)
                lineTo(12.0f, 3.43f)
                lineTo(10.57f, 2.0f)
                lineTo(9.14f, 3.43f)
                lineTo(7.71f, 2.0f)
                lineTo(5.57f, 4.14f)
                lineTo(4.14f, 2.71f)
                lineTo(2.71f, 4.14f)
                lineToRelative(1.43f, 1.43f)
                lineTo(2.0f, 7.71f)
                lineToRelative(1.43f, 1.43f)
                lineTo(2.0f, 10.57f)
                lineTo(3.43f, 12.0f)
                lineTo(7.0f, 8.43f)
                lineTo(15.57f, 17.0f)
                lineTo(12.0f, 20.57f)
                lineTo(13.43f, 22.0f)
                lineToRelative(1.43f, -1.43f)
                lineTo(16.29f, 22.0f)
                lineToRelative(2.14f, -2.14f)
                lineToRelative(1.43f, 1.43f)
                lineToRelative(1.43f, -1.43f)
                lineToRelative(-1.43f, -1.43f)
                lineTo(22.0f, 16.29f)
                close()
            }
        }
        return _fitnessCenter!!
    }

private var _fitnessCenter: ImageVector? = null

public val Icons.Filled.Flight: ImageVector
    get() {
        if (_flight != null) {
            return _flight!!
        }
        _flight = materialIcon(name = "Filled.Flight") {
            materialPath {
                moveTo(21.0f, 16.0f)
                verticalLineToRelative(-2.0f)
                lineToRelative(-8.0f, -5.0f)
                verticalLineTo(3.5f)
                curveToRelative(0.0f, -0.83f, -0.67f, -1.5f, -1.5f, -1.5f)
                reflectiveCurveTo(10.0f, 2.67f, 10.0f, 3.5f)
                verticalLineTo(9.0f)
                lineToRelative(-8.0f, 5.0f)
                verticalLineToRelative(2.0f)
                lineToRelative(8.0f, -2.5f)
                verticalLineTo(19.0f)
                lineToRelative(-2.0f, 1.5f)
                verticalLineTo(22.0f)
                lineToRelative(3.5f, -1.0f)
                lineToRelative(3.5f, 1.0f)
                verticalLineToRelative(-1.5f)
                lineTo(13.0f, 19.0f)
                verticalLineToRelative(-5.5f)
                lineToRelative(8.0f, 2.5f)
                close()
            }
        }
        return _flight!!
    }

private var _flight: ImageVector? = null

public val Icons.Filled.FolderOpen: ImageVector
    get() {
        if (_folderOpen != null) {
            return _folderOpen!!
        }
        _folderOpen = materialIcon(name = "Filled.FolderOpen") {
            materialPath {
                moveTo(20.0f, 6.0f)
                horizontalLineToRelative(-8.0f)
                lineToRelative(-2.0f, -2.0f)
                lineTo(4.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(2.0f, 18.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 8.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(20.0f, 18.0f)
                lineTo(4.0f, 18.0f)
                lineTo(4.0f, 8.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(10.0f)
                close()
            }
        }
        return _folderOpen!!
    }

private var _folderOpen: ImageVector? = null

public val Icons.Filled.Gavel: ImageVector
    get() {
        if (_gavel != null) {
            return _gavel!!
        }
        _gavel = materialIcon(name = "Filled.Gavel") {
            materialPath {
                moveTo(5.25f, 8.069f)
                lineToRelative(2.83f, -2.827f)
                lineToRelative(14.134f, 14.15f)
                lineToRelative(-2.83f, 2.827f)
                close()
            }
            materialPath {
                moveTo(9.486f, 3.827f)
                lineToRelative(2.828f, -2.829f)
                lineToRelative(5.658f, 5.656f)
                lineToRelative(-2.828f, 2.829f)
                close()
            }
            materialPath {
                moveTo(0.999f, 12.315f)
                lineToRelative(2.828f, -2.828f)
                lineToRelative(5.657f, 5.657f)
                lineToRelative(-2.828f, 2.828f)
                close()
            }
            materialPath {
                moveTo(1.0f, 21.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-12.0f)
                close()
            }
        }
        return _gavel!!
    }

private var _gavel: ImageVector? = null

public val Icons.Filled.GridView: ImageVector
    get() {
        if (_gridView != null) {
            return _gridView!!
        }
        _gridView = materialIcon(name = "Filled.GridView") {
            materialPath(pathFillType = EvenOdd) {
                moveTo(3.0f, 3.0f)
                verticalLineToRelative(8.0f)
                horizontalLineToRelative(8.0f)
                lineTo(11.0f, 3.0f)
                lineTo(3.0f, 3.0f)
                close()
                moveTo(9.0f, 9.0f)
                lineTo(5.0f, 9.0f)
                lineTo(5.0f, 5.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(3.0f, 13.0f)
                verticalLineToRelative(8.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-8.0f)
                lineTo(3.0f, 13.0f)
                close()
                moveTo(9.0f, 19.0f)
                lineTo(5.0f, 19.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(13.0f, 3.0f)
                verticalLineToRelative(8.0f)
                horizontalLineToRelative(8.0f)
                lineTo(21.0f, 3.0f)
                horizontalLineToRelative(-8.0f)
                close()
                moveTo(19.0f, 9.0f)
                horizontalLineToRelative(-4.0f)
                lineTo(15.0f, 5.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(13.0f, 13.0f)
                verticalLineToRelative(8.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-8.0f)
                horizontalLineToRelative(-8.0f)
                close()
                moveTo(19.0f, 19.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                close()
            }
        }
        return _gridView!!
    }

private var _gridView: ImageVector? = null

public val Icons.Filled.Handyman: ImageVector
    get() {
        if (_handyman != null) {
            return _handyman!!
        }
        _handyman = materialIcon(name = "Filled.Handyman") {
            materialPath {
                moveTo(21.67f, 18.17f)
                lineToRelative(-5.3f, -5.3f)
                horizontalLineToRelative(-0.99f)
                lineToRelative(-2.54f, 2.54f)
                verticalLineToRelative(0.99f)
                lineToRelative(5.3f, 5.3f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(2.12f, -2.12f)
                curveTo(22.06f, 19.2f, 22.06f, 18.56f, 21.67f, 18.17f)
                close()
            }
            materialPath {
                moveTo(17.34f, 10.19f)
                lineToRelative(1.41f, -1.41f)
                lineToRelative(2.12f, 2.12f)
                curveToRelative(1.17f, -1.17f, 1.17f, -3.07f, 0.0f, -4.24f)
                lineToRelative(-3.54f, -3.54f)
                lineToRelative(-1.41f, 1.41f)
                verticalLineTo(1.71f)
                lineTo(15.22f, 1.0f)
                lineToRelative(-3.54f, 3.54f)
                lineToRelative(0.71f, 0.71f)
                horizontalLineToRelative(2.83f)
                lineToRelative(-1.41f, 1.41f)
                lineToRelative(1.06f, 1.06f)
                lineToRelative(-2.89f, 2.89f)
                lineTo(7.85f, 6.48f)
                verticalLineTo(5.06f)
                lineTo(4.83f, 2.04f)
                lineTo(2.0f, 4.87f)
                lineToRelative(3.03f, 3.03f)
                horizontalLineToRelative(1.41f)
                lineToRelative(4.13f, 4.13f)
                lineToRelative(-0.85f, 0.85f)
                horizontalLineTo(7.6f)
                lineToRelative(-5.3f, 5.3f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(2.12f, 2.12f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(5.3f, -5.3f)
                verticalLineToRelative(-2.12f)
                lineToRelative(5.15f, -5.15f)
                lineTo(17.34f, 10.19f)
                close()
            }
        }
        return _handyman!!
    }

private var _handyman: ImageVector? = null

public val Icons.Filled.Headphones: ImageVector
    get() {
        if (_headphones != null) {
            return _headphones!!
        }
        _headphones = materialIcon(name = "Filled.Headphones") {
            materialPath {
                moveTo(12.0f, 3.0f)
                curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
                verticalLineToRelative(7.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-8.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -3.87f, 3.13f, -7.0f, 7.0f, -7.0f)
                reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(8.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-7.0f)
                curveTo(21.0f, 7.03f, 16.97f, 3.0f, 12.0f, 3.0f)
                close()
            }
        }
        return _headphones!!
    }

private var _headphones: ImageVector? = null

public val Icons.Filled.History: ImageVector
    get() {
        if (_history != null) {
            return _history!!
        }
        _history = materialIcon(name = "Filled.History") {
            materialPath {
                moveTo(13.0f, 3.0f)
                curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
                lineTo(1.0f, 12.0f)
                lineToRelative(3.89f, 3.89f)
                lineToRelative(0.07f, 0.14f)
                lineTo(9.0f, 12.0f)
                lineTo(6.0f, 12.0f)
                curveToRelative(0.0f, -3.87f, 3.13f, -7.0f, 7.0f, -7.0f)
                reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
                reflectiveCurveToRelative(-3.13f, 7.0f, -7.0f, 7.0f)
                curveToRelative(-1.93f, 0.0f, -3.68f, -0.79f, -4.94f, -2.06f)
                lineToRelative(-1.42f, 1.42f)
                curveTo(8.27f, 19.99f, 10.51f, 21.0f, 13.0f, 21.0f)
                curveToRelative(4.97f, 0.0f, 9.0f, -4.03f, 9.0f, -9.0f)
                reflectiveCurveToRelative(-4.03f, -9.0f, -9.0f, -9.0f)
                close()
                moveTo(12.0f, 8.0f)
                verticalLineToRelative(5.0f)
                lineToRelative(4.28f, 2.54f)
                lineToRelative(0.72f, -1.21f)
                lineToRelative(-3.5f, -2.08f)
                lineTo(13.5f, 8.0f)
                lineTo(12.0f, 8.0f)
                close()
            }
        }
        return _history!!
    }

private var _history: ImageVector? = null

public val Icons.Filled.Hotel: ImageVector
    get() {
        if (_hotel != null) {
            return _hotel!!
        }
        _hotel = materialIcon(name = "Filled.Hotel") {
            materialPath {
                moveTo(7.0f, 13.0f)
                curveToRelative(1.66f, 0.0f, 3.0f, -1.34f, 3.0f, -3.0f)
                reflectiveCurveTo(8.66f, 7.0f, 7.0f, 7.0f)
                reflectiveCurveToRelative(-3.0f, 1.34f, -3.0f, 3.0f)
                reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
                close()
                moveTo(19.0f, 7.0f)
                horizontalLineToRelative(-8.0f)
                verticalLineToRelative(7.0f)
                lineTo(3.0f, 14.0f)
                lineTo(3.0f, 5.0f)
                lineTo(1.0f, 5.0f)
                verticalLineToRelative(15.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(18.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-9.0f)
                curveToRelative(0.0f, -2.21f, -1.79f, -4.0f, -4.0f, -4.0f)
                close()
            }
        }
        return _hotel!!
    }

private var _hotel: ImageVector? = null

public val Icons.Filled.Icecream: ImageVector
    get() {
        if (_icecream != null) {
            return _icecream!!
        }
        _icecream = materialIcon(name = "Filled.Icecream") {
            materialPath(pathFillType = EvenOdd) {
                moveTo(8.79f, 12.4f)
                lineToRelative(3.26f, 6.22f)
                lineToRelative(3.17f, -6.21f)
                curveToRelative(-0.11f, -0.08f, -0.21f, -0.16f, -0.3f, -0.25f)
                curveTo(14.08f, 12.69f, 13.07f, 13.0f, 12.0f, 13.0f)
                reflectiveCurveToRelative(-2.08f, -0.31f, -2.92f, -0.84f)
                curveTo(8.99f, 12.25f, 8.89f, 12.33f, 8.79f, 12.4f)
                close()
                moveTo(6.83f, 12.99f)
                curveTo(5.25f, 12.9f, 4.0f, 11.6f, 4.0f, 10.0f)
                curveToRelative(0.0f, -1.49f, 1.09f, -2.73f, 2.52f, -2.96f)
                curveTo(6.75f, 4.22f, 9.12f, 2.0f, 12.0f, 2.0f)
                reflectiveCurveToRelative(5.25f, 2.22f, 5.48f, 5.04f)
                curveTo(18.91f, 7.27f, 20.0f, 8.51f, 20.0f, 10.0f)
                curveToRelative(0.0f, 1.59f, -1.24f, 2.9f, -2.81f, 2.99f)
                lineTo(12.07f, 23.0f)
                lineTo(6.83f, 12.99f)
                close()
            }
        }
        return _icecream!!
    }

private var _icecream: ImageVector? = null

public val Icons.Filled.KeyboardHide: ImageVector
    get() {
        if (_keyboardHide != null) {
            return _keyboardHide!!
        }
        _keyboardHide = materialIcon(name = "Filled.KeyboardHide") {
            materialPath {
                moveTo(20.0f, 3.0f)
                lineTo(4.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(2.0f, 15.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(11.0f, 6.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(11.0f, 6.0f)
                close()
                moveTo(11.0f, 9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(11.0f, 9.0f)
                close()
                moveTo(8.0f, 6.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                lineTo(8.0f, 8.0f)
                lineTo(8.0f, 6.0f)
                close()
                moveTo(8.0f, 9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                lineTo(8.0f, 11.0f)
                lineTo(8.0f, 9.0f)
                close()
                moveTo(7.0f, 11.0f)
                lineTo(5.0f, 11.0f)
                lineTo(5.0f, 9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(7.0f, 8.0f)
                lineTo(5.0f, 8.0f)
                lineTo(5.0f, 6.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(16.0f, 15.0f)
                lineTo(8.0f, 15.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(16.0f, 11.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(14.0f, 9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(16.0f, 8.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(14.0f, 6.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(19.0f, 11.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(17.0f, 9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(19.0f, 8.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(17.0f, 6.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(12.0f, 23.0f)
                lineToRelative(4.0f, -4.0f)
                lineTo(8.0f, 19.0f)
                lineToRelative(4.0f, 4.0f)
                close()
            }
        }
        return _keyboardHide!!
    }

private var _keyboardHide: ImageVector? = null

public val Icons.Filled.Kitchen: ImageVector
    get() {
        if (_kitchen != null) {
            return _kitchen!!
        }
        _kitchen = materialIcon(name = "Filled.Kitchen") {
            materialPath {
                moveTo(20.0f, 9.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(6.0f)
                curveTo(4.9f, 2.0f, 4.0f, 2.9f, 4.0f, 4.0f)
                verticalLineToRelative(5.0f)
                horizontalLineTo(20.0f)
                close()
                moveTo(8.0f, 5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(5.0f)
                close()
            }
            materialPath {
                moveTo(4.0f, 11.0f)
                verticalLineToRelative(9.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-9.0f)
                horizontalLineTo(4.0f)
                close()
                moveTo(10.0f, 17.0f)
                horizontalLineTo(8.0f)
                verticalLineToRelative(-5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(17.0f)
                close()
            }
        }
        return _kitchen!!
    }

private var _kitchen: ImageVector? = null

public val Icons.Filled.Language: ImageVector
    get() {
        if (_language != null) {
            return _language!!
        }
        _language = materialIcon(name = "Filled.Language") {
            materialPath {
                moveTo(11.99f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 9.99f, 10.0f)
                curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
                close()
                moveTo(18.92f, 8.0f)
                horizontalLineToRelative(-2.95f)
                curveToRelative(-0.32f, -1.25f, -0.78f, -2.45f, -1.38f, -3.56f)
                curveToRelative(1.84f, 0.63f, 3.37f, 1.91f, 4.33f, 3.56f)
                close()
                moveTo(12.0f, 4.04f)
                curveToRelative(0.83f, 1.2f, 1.48f, 2.53f, 1.91f, 3.96f)
                horizontalLineToRelative(-3.82f)
                curveToRelative(0.43f, -1.43f, 1.08f, -2.76f, 1.91f, -3.96f)
                close()
                moveTo(4.26f, 14.0f)
                curveTo(4.1f, 13.36f, 4.0f, 12.69f, 4.0f, 12.0f)
                reflectiveCurveToRelative(0.1f, -1.36f, 0.26f, -2.0f)
                horizontalLineToRelative(3.38f)
                curveToRelative(-0.08f, 0.66f, -0.14f, 1.32f, -0.14f, 2.0f)
                curveToRelative(0.0f, 0.68f, 0.06f, 1.34f, 0.14f, 2.0f)
                lineTo(4.26f, 14.0f)
                close()
                moveTo(5.08f, 16.0f)
                horizontalLineToRelative(2.95f)
                curveToRelative(0.32f, 1.25f, 0.78f, 2.45f, 1.38f, 3.56f)
                curveToRelative(-1.84f, -0.63f, -3.37f, -1.9f, -4.33f, -3.56f)
                close()
                moveTo(8.03f, 8.0f)
                lineTo(5.08f, 8.0f)
                curveToRelative(0.96f, -1.66f, 2.49f, -2.93f, 4.33f, -3.56f)
                curveTo(8.81f, 5.55f, 8.35f, 6.75f, 8.03f, 8.0f)
                close()
                moveTo(12.0f, 19.96f)
                curveToRelative(-0.83f, -1.2f, -1.48f, -2.53f, -1.91f, -3.96f)
                horizontalLineToRelative(3.82f)
                curveToRelative(-0.43f, 1.43f, -1.08f, 2.76f, -1.91f, 3.96f)
                close()
                moveTo(14.34f, 14.0f)
                lineTo(9.66f, 14.0f)
                curveToRelative(-0.09f, -0.66f, -0.16f, -1.32f, -0.16f, -2.0f)
                curveToRelative(0.0f, -0.68f, 0.07f, -1.35f, 0.16f, -2.0f)
                horizontalLineToRelative(4.68f)
                curveToRelative(0.09f, 0.65f, 0.16f, 1.32f, 0.16f, 2.0f)
                curveToRelative(0.0f, 0.68f, -0.07f, 1.34f, -0.16f, 2.0f)
                close()
                moveTo(14.59f, 19.56f)
                curveToRelative(0.6f, -1.11f, 1.06f, -2.31f, 1.38f, -3.56f)
                horizontalLineToRelative(2.95f)
                curveToRelative(-0.96f, 1.65f, -2.49f, 2.93f, -4.33f, 3.56f)
                close()
                moveTo(16.36f, 14.0f)
                curveToRelative(0.08f, -0.66f, 0.14f, -1.32f, 0.14f, -2.0f)
                curveToRelative(0.0f, -0.68f, -0.06f, -1.34f, -0.14f, -2.0f)
                horizontalLineToRelative(3.38f)
                curveToRelative(0.16f, 0.64f, 0.26f, 1.31f, 0.26f, 2.0f)
                reflectiveCurveToRelative(-0.1f, 1.36f, -0.26f, 2.0f)
                horizontalLineToRelative(-3.38f)
                close()
            }
        }
        return _language!!
    }

private var _language: ImageVector? = null

public val Icons.Filled.Layers: ImageVector
    get() {
        if (_layers != null) {
            return _layers!!
        }
        _layers = materialIcon(name = "Filled.Layers") {
            materialPath {
                moveTo(11.99f, 18.54f)
                lineToRelative(-7.37f, -5.73f)
                lineTo(3.0f, 14.07f)
                lineToRelative(9.0f, 7.0f)
                lineToRelative(9.0f, -7.0f)
                lineToRelative(-1.63f, -1.27f)
                lineToRelative(-7.38f, 5.74f)
                close()
                moveTo(12.0f, 16.0f)
                lineToRelative(7.36f, -5.73f)
                lineTo(21.0f, 9.0f)
                lineToRelative(-9.0f, -7.0f)
                lineToRelative(-9.0f, 7.0f)
                lineToRelative(1.63f, 1.27f)
                lineTo(12.0f, 16.0f)
                close()
            }
        }
        return _layers!!
    }

private var _layers: ImageVector? = null

public val Icons.Filled.LocalBar: ImageVector
    get() {
        if (_localBar != null) {
            return _localBar!!
        }
        _localBar = materialIcon(name = "Filled.LocalBar") {
            materialPath {
                moveTo(21.0f, 5.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(3.0f)
                verticalLineToRelative(2.0f)
                lineToRelative(8.0f, 9.0f)
                verticalLineToRelative(5.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-5.0f)
                verticalLineToRelative(-5.0f)
                lineToRelative(8.0f, -9.0f)
                close()
                moveTo(7.43f, 7.0f)
                lineTo(5.66f, 5.0f)
                horizontalLineToRelative(12.69f)
                lineToRelative(-1.78f, 2.0f)
                horizontalLineTo(7.43f)
                close()
            }
        }
        return _localBar!!
    }

private var _localBar: ImageVector? = null

public val Icons.Filled.LocalCafe: ImageVector
    get() {
        if (_localCafe != null) {
            return _localCafe!!
        }
        _localCafe = materialIcon(name = "Filled.LocalCafe") {
            materialPath {
                moveTo(20.0f, 3.0f)
                lineTo(4.0f, 3.0f)
                verticalLineToRelative(10.0f)
                curveToRelative(0.0f, 2.21f, 1.79f, 4.0f, 4.0f, 4.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(1.11f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 5.0f)
                curveToRelative(0.0f, -1.11f, -0.89f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(20.0f, 8.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(18.0f, 5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(3.0f)
                close()
                moveTo(4.0f, 19.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(2.0f)
                lineTo(4.0f, 21.0f)
                close()
            }
        }
        return _localCafe!!
    }

private var _localCafe: ImageVector? = null

public val Icons.Filled.LocalFireDepartment: ImageVector
    get() {
        if (_localFireDepartment != null) {
            return _localFireDepartment!!
        }
        _localFireDepartment = materialIcon(name = "Filled.LocalFireDepartment") {
            materialPath {
                moveTo(12.0f, 12.9f)
                lineToRelative(-2.13f, 2.09f)
                curveTo(9.31f, 15.55f, 9.0f, 16.28f, 9.0f, 17.06f)
                curveTo(9.0f, 18.68f, 10.35f, 20.0f, 12.0f, 20.0f)
                reflectiveCurveToRelative(3.0f, -1.32f, 3.0f, -2.94f)
                curveToRelative(0.0f, -0.78f, -0.31f, -1.52f, -0.87f, -2.07f)
                lineTo(12.0f, 12.9f)
                close()
            }
            materialPath {
                moveTo(16.0f, 6.0f)
                lineToRelative(-0.44f, 0.55f)
                curveTo(14.38f, 8.02f, 12.0f, 7.19f, 12.0f, 5.3f)
                verticalLineTo(2.0f)
                curveToRelative(0.0f, 0.0f, -8.0f, 4.0f, -8.0f, 11.0f)
                curveToRelative(0.0f, 2.92f, 1.56f, 5.47f, 3.89f, 6.86f)
                curveTo(7.33f, 19.07f, 7.0f, 18.1f, 7.0f, 17.06f)
                curveToRelative(0.0f, -1.32f, 0.52f, -2.56f, 1.47f, -3.5f)
                lineTo(12.0f, 10.1f)
                lineToRelative(3.53f, 3.47f)
                curveToRelative(0.95f, 0.93f, 1.47f, 2.17f, 1.47f, 3.5f)
                curveToRelative(0.0f, 1.02f, -0.31f, 1.96f, -0.85f, 2.75f)
                curveToRelative(1.89f, -1.15f, 3.29f, -3.06f, 3.71f, -5.3f)
                curveTo(20.52f, 10.97f, 18.79f, 7.62f, 16.0f, 6.0f)
                close()
            }
        }
        return _localFireDepartment!!
    }

private var _localFireDepartment: ImageVector? = null

public val Icons.Filled.LocalGasStation: ImageVector
    get() {
        if (_localGasStation != null) {
            return _localGasStation!!
        }
        _localGasStation = materialIcon(name = "Filled.LocalGasStation") {
            materialPath {
                moveTo(19.77f, 7.23f)
                lineToRelative(0.01f, -0.01f)
                lineToRelative(-3.72f, -3.72f)
                lineTo(15.0f, 4.56f)
                lineToRelative(2.11f, 2.11f)
                curveToRelative(-0.94f, 0.36f, -1.61f, 1.26f, -1.61f, 2.33f)
                curveToRelative(0.0f, 1.38f, 1.12f, 2.5f, 2.5f, 2.5f)
                curveToRelative(0.36f, 0.0f, 0.69f, -0.08f, 1.0f, -0.21f)
                verticalLineToRelative(7.21f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(17.0f, 14.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-1.0f)
                lineTo(14.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                lineTo(6.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(16.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(-7.5f)
                horizontalLineToRelative(1.5f)
                verticalLineToRelative(5.0f)
                curveToRelative(0.0f, 1.38f, 1.12f, 2.5f, 2.5f, 2.5f)
                reflectiveCurveToRelative(2.5f, -1.12f, 2.5f, -2.5f)
                lineTo(20.5f, 9.0f)
                curveToRelative(0.0f, -0.69f, -0.28f, -1.32f, -0.73f, -1.77f)
                close()
                moveTo(12.0f, 10.0f)
                lineTo(6.0f, 10.0f)
                lineTo(6.0f, 5.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(5.0f)
                close()
                moveTo(18.0f, 10.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _localGasStation!!
    }

private var _localGasStation: ImageVector? = null

public val Icons.Filled.LocalHospital: ImageVector
    get() {
        if (_localHospital != null) {
            return _localHospital!!
        }
        _localHospital = materialIcon(name = "Filled.LocalHospital") {
            materialPath {
                moveTo(19.0f, 3.0f)
                lineTo(5.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(3.0f, 19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(21.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(18.0f, 14.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-4.0f)
                lineTo(6.0f, 14.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(4.0f)
                lineTo(10.0f, 6.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                close()
            }
        }
        return _localHospital!!
    }

private var _localHospital: ImageVector? = null

public val Icons.Filled.LocalMall: ImageVector
    get() {
        if (_localMall != null) {
            return _localMall!!
        }
        _localMall = materialIcon(name = "Filled.LocalMall") {
            materialPath {
                moveTo(19.0f, 6.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(0.0f, -2.76f, -2.24f, -5.0f, -5.0f, -5.0f)
                reflectiveCurveTo(7.0f, 3.24f, 7.0f, 6.0f)
                lineTo(5.0f, 6.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(3.0f, 20.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(21.0f, 8.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(12.0f, 3.0f)
                curveToRelative(1.66f, 0.0f, 3.0f, 1.34f, 3.0f, 3.0f)
                lineTo(9.0f, 6.0f)
                curveToRelative(0.0f, -1.66f, 1.34f, -3.0f, 3.0f, -3.0f)
                close()
                moveTo(12.0f, 13.0f)
                curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                reflectiveCurveToRelative(3.0f, -1.34f, 3.0f, -3.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.0f, 2.76f, -2.24f, 5.0f, -5.0f, 5.0f)
                close()
            }
        }
        return _localMall!!
    }

private var _localMall: ImageVector? = null

public val Icons.Filled.LocalTaxi: ImageVector
    get() {
        if (_localTaxi != null) {
            return _localTaxi!!
        }
        _localTaxi = materialIcon(name = "Filled.LocalTaxi") {
            materialPath {
                moveTo(18.92f, 6.01f)
                curveTo(18.72f, 5.42f, 18.16f, 5.0f, 17.5f, 5.0f)
                lineTo(15.0f, 5.0f)
                lineTo(15.0f, 3.0f)
                lineTo(9.0f, 3.0f)
                verticalLineToRelative(2.0f)
                lineTo(6.5f, 5.0f)
                curveToRelative(-0.66f, 0.0f, -1.21f, 0.42f, -1.42f, 1.01f)
                lineTo(3.0f, 12.0f)
                verticalLineToRelative(8.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-8.0f)
                lineToRelative(-2.08f, -5.99f)
                close()
                moveTo(6.5f, 16.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(5.67f, 13.0f, 6.5f, 13.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(7.33f, 16.0f, 6.5f, 16.0f)
                close()
                moveTo(17.5f, 16.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
                moveTo(5.0f, 11.0f)
                lineToRelative(1.5f, -4.5f)
                horizontalLineToRelative(11.0f)
                lineTo(19.0f, 11.0f)
                lineTo(5.0f, 11.0f)
                close()
            }
        }
        return _localTaxi!!
    }

private var _localTaxi: ImageVector? = null

public val Icons.Filled.Luggage: ImageVector
    get() {
        if (_luggage != null) {
            return _luggage!!
        }
        _luggage = materialIcon(name = "Filled.Luggage") {
            materialPath {
                moveTo(17.0f, 6.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-4.0f)
                curveTo(9.45f, 2.0f, 9.0f, 2.45f, 9.0f, 3.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(7.0f)
                curveTo(5.9f, 6.0f, 5.0f, 6.9f, 5.0f, 8.0f)
                verticalLineToRelative(11.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(8.0f)
                curveTo(19.0f, 6.9f, 18.1f, 6.0f, 17.0f, 6.0f)
                close()
                moveTo(9.5f, 18.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(1.5f)
                verticalLineTo(18.0f)
                close()
                moveTo(12.75f, 18.0f)
                horizontalLineToRelative(-1.5f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(1.5f)
                verticalLineTo(18.0f)
                close()
                moveTo(13.5f, 6.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineTo(3.5f)
                horizontalLineToRelative(3.0f)
                verticalLineTo(6.0f)
                close()
                moveTo(16.0f, 18.0f)
                horizontalLineToRelative(-1.5f)
                verticalLineTo(9.0f)
                horizontalLineTo(16.0f)
                verticalLineTo(18.0f)
                close()
            }
        }
        return _luggage!!
    }

private var _luggage: ImageVector? = null

public val Icons.Filled.LunchDining: ImageVector
    get() {
        if (_lunchDining != null) {
            return _lunchDining!!
        }
        _lunchDining = materialIcon(name = "Filled.LunchDining") {
            materialPath(pathFillType = EvenOdd) {
                moveTo(22.0f, 10.0f)
                curveToRelative(0.32f, -3.28f, -4.28f, -6.0f, -9.99f, -6.0f)
                curveTo(6.3f, 4.0f, 1.7f, 6.72f, 2.02f, 10.0f)
                horizontalLineTo(22.0f)
                close()
            }
            materialPath(pathFillType = EvenOdd) {
                moveTo(5.35f, 13.5f)
                curveToRelative(0.55f, 0.0f, 0.78f, 0.14f, 1.15f, 0.36f)
                curveToRelative(0.45f, 0.27f, 1.07f, 0.64f, 2.18f, 0.64f)
                reflectiveCurveToRelative(1.73f, -0.37f, 2.18f, -0.64f)
                curveToRelative(0.37f, -0.23f, 0.59f, -0.36f, 1.15f, -0.36f)
                curveToRelative(0.55f, 0.0f, 0.78f, 0.14f, 1.15f, 0.36f)
                curveToRelative(0.45f, 0.27f, 1.07f, 0.64f, 2.18f, 0.64f)
                curveToRelative(1.11f, 0.0f, 1.73f, -0.37f, 2.18f, -0.64f)
                curveToRelative(0.37f, -0.23f, 0.59f, -0.36f, 1.15f, -0.36f)
                curveToRelative(0.55f, 0.0f, 0.78f, 0.14f, 1.15f, 0.36f)
                curveToRelative(0.45f, 0.27f, 1.07f, 0.63f, 2.17f, 0.64f)
                verticalLineToRelative(-1.98f)
                curveToRelative(0.0f, 0.0f, -0.79f, -0.16f, -1.16f, -0.38f)
                curveToRelative(-0.45f, -0.27f, -1.07f, -0.64f, -2.18f, -0.64f)
                curveToRelative(-1.11f, 0.0f, -1.73f, 0.37f, -2.18f, 0.64f)
                curveToRelative(-0.37f, 0.23f, -0.6f, 0.36f, -1.15f, 0.36f)
                reflectiveCurveToRelative(-0.78f, -0.14f, -1.15f, -0.36f)
                curveToRelative(-0.45f, -0.27f, -1.07f, -0.64f, -2.18f, -0.64f)
                reflectiveCurveToRelative(-1.73f, 0.37f, -2.18f, 0.64f)
                curveToRelative(-0.37f, 0.23f, -0.59f, 0.36f, -1.15f, 0.36f)
                curveToRelative(-0.55f, 0.0f, -0.78f, -0.14f, -1.15f, -0.36f)
                curveToRelative(-0.45f, -0.27f, -1.07f, -0.64f, -2.18f, -0.64f)
                curveToRelative(-1.11f, 0.0f, -1.73f, 0.37f, -2.18f, 0.64f)
                curveTo(2.78f, 12.37f, 2.56f, 12.5f, 2.0f, 12.5f)
                verticalLineToRelative(2.0f)
                curveToRelative(1.11f, 0.0f, 1.73f, -0.37f, 2.21f, -0.64f)
                curveTo(4.58f, 13.63f, 4.8f, 13.5f, 5.35f, 13.5f)
                close()
            }
            materialPath(pathFillType = EvenOdd) {
                moveTo(2.0f, 16.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(2.0f)
                close()
            }
        }
        return _lunchDining!!
    }

private var _lunchDining: ImageVector? = null

public val Icons.Filled.MedicalServices: ImageVector
    get() {
        if (_medicalServices != null) {
            return _medicalServices!!
        }
        _medicalServices = materialIcon(name = "Filled.MedicalServices") {
            materialPath {
                moveTo(20.0f, 6.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-4.0f)
                curveTo(8.9f, 2.0f, 8.0f, 2.9f, 8.0f, 4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(4.0f)
                curveTo(2.9f, 6.0f, 2.0f, 6.9f, 2.0f, 8.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(8.0f)
                curveTo(22.0f, 6.9f, 21.1f, 6.0f, 20.0f, 6.0f)
                close()
                moveTo(10.0f, 4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(4.0f)
                close()
                moveTo(16.0f, 15.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineTo(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(3.0f)
                verticalLineTo(15.0f)
                close()
            }
        }
        return _medicalServices!!
    }

private var _medicalServices: ImageVector? = null

public val Icons.Filled.Medication: ImageVector
    get() {
        if (_medication != null) {
            return _medication!!
        }
        _medication = materialIcon(name = "Filled.Medication") {
            materialPath {
                moveTo(6.0f, 3.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-12.0f)
                close()
            }
            materialPath {
                moveTo(17.0f, 6.0f)
                horizontalLineTo(7.0f)
                curveTo(5.9f, 6.0f, 5.0f, 6.9f, 5.0f, 8.0f)
                verticalLineToRelative(11.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(8.0f)
                curveTo(19.0f, 6.9f, 18.1f, 6.0f, 17.0f, 6.0f)
                close()
                moveTo(16.0f, 15.0f)
                horizontalLineToRelative(-2.5f)
                verticalLineToRelative(2.5f)
                horizontalLineToRelative(-3.0f)
                verticalLineTo(15.0f)
                horizontalLineTo(8.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(2.5f)
                verticalLineTo(9.5f)
                horizontalLineToRelative(3.0f)
                verticalLineTo(12.0f)
                horizontalLineTo(16.0f)
                verticalLineTo(15.0f)
                close()
            }
        }
        return _medication!!
    }

private var _medication: ImageVector? = null

public val Icons.Filled.MoreHoriz: ImageVector
    get() {
        if (_moreHoriz != null) {
            return _moreHoriz!!
        }
        _moreHoriz = materialIcon(name = "Filled.MoreHoriz") {
            materialPath {
                moveTo(6.0f, 10.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(18.0f, 10.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(12.0f, 10.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
            }
        }
        return _moreHoriz!!
    }

private var _moreHoriz: ImageVector? = null

public val Icons.Filled.Movie: ImageVector
    get() {
        if (_movie != null) {
            return _movie!!
        }
        _movie = materialIcon(name = "Filled.Movie") {
            materialPath {
                moveTo(18.0f, 4.0f)
                lineToRelative(2.0f, 4.0f)
                horizontalLineToRelative(-3.0f)
                lineToRelative(-2.0f, -4.0f)
                horizontalLineToRelative(-2.0f)
                lineToRelative(2.0f, 4.0f)
                horizontalLineToRelative(-3.0f)
                lineToRelative(-2.0f, -4.0f)
                horizontalLineTo(8.0f)
                lineToRelative(2.0f, 4.0f)
                horizontalLineTo(7.0f)
                lineTo(5.0f, 4.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(2.0f, 18.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(-4.0f)
                close()
            }
        }
        return _movie!!
    }

private var _movie: ImageVector? = null

public val Icons.Filled.MusicNote: ImageVector
    get() {
        if (_musicNote != null) {
            return _musicNote!!
        }
        _musicNote = materialIcon(name = "Filled.MusicNote") {
            materialPath {
                moveTo(12.0f, 3.0f)
                verticalLineToRelative(10.55f)
                curveToRelative(-0.59f, -0.34f, -1.27f, -0.55f, -2.0f, -0.55f)
                curveToRelative(-2.21f, 0.0f, -4.0f, 1.79f, -4.0f, 4.0f)
                reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
                reflectiveCurveToRelative(4.0f, -1.79f, 4.0f, -4.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(3.0f)
                horizontalLineToRelative(-6.0f)
                close()
            }
        }
        return _musicNote!!
    }

private var _musicNote: ImageVector? = null

public val Icons.Filled.Palette: ImageVector
    get() {
        if (_palette != null) {
            return _palette!!
        }
        _palette = materialIcon(name = "Filled.Palette") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.49f, 2.0f, 2.0f, 6.49f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.49f, 10.0f, 10.0f, 10.0f)
                curveToRelative(1.38f, 0.0f, 2.5f, -1.12f, 2.5f, -2.5f)
                curveToRelative(0.0f, -0.61f, -0.23f, -1.2f, -0.64f, -1.67f)
                curveToRelative(-0.08f, -0.1f, -0.13f, -0.21f, -0.13f, -0.33f)
                curveToRelative(0.0f, -0.28f, 0.22f, -0.5f, 0.5f, -0.5f)
                horizontalLineTo(16.0f)
                curveToRelative(3.31f, 0.0f, 6.0f, -2.69f, 6.0f, -6.0f)
                curveTo(22.0f, 6.04f, 17.51f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(17.5f, 13.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                curveToRelative(0.0f, -0.83f, 0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                curveTo(19.0f, 12.33f, 18.33f, 13.0f, 17.5f, 13.0f)
                close()
                moveTo(14.5f, 9.0f)
                curveTo(13.67f, 9.0f, 13.0f, 8.33f, 13.0f, 7.5f)
                curveTo(13.0f, 6.67f, 13.67f, 6.0f, 14.5f, 6.0f)
                reflectiveCurveTo(16.0f, 6.67f, 16.0f, 7.5f)
                curveTo(16.0f, 8.33f, 15.33f, 9.0f, 14.5f, 9.0f)
                close()
                moveTo(5.0f, 11.5f)
                curveTo(5.0f, 10.67f, 5.67f, 10.0f, 6.5f, 10.0f)
                reflectiveCurveTo(8.0f, 10.67f, 8.0f, 11.5f)
                curveTo(8.0f, 12.33f, 7.33f, 13.0f, 6.5f, 13.0f)
                reflectiveCurveTo(5.0f, 12.33f, 5.0f, 11.5f)
                close()
                moveTo(11.0f, 7.5f)
                curveTo(11.0f, 8.33f, 10.33f, 9.0f, 9.5f, 9.0f)
                reflectiveCurveTo(8.0f, 8.33f, 8.0f, 7.5f)
                curveTo(8.0f, 6.67f, 8.67f, 6.0f, 9.5f, 6.0f)
                reflectiveCurveTo(11.0f, 6.67f, 11.0f, 7.5f)
                close()
            }
        }
        return _palette!!
    }

private var _palette: ImageVector? = null

public val Icons.Filled.Payments: ImageVector
    get() {
        if (_payments != null) {
            return _payments!!
        }
        _payments = materialIcon(name = "Filled.Payments") {
            materialPath {
                moveTo(19.0f, 14.0f)
                lineTo(19.0f, 6.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                lineTo(3.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(8.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                close()
                moveTo(10.0f, 13.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, -1.34f, -3.0f, -3.0f)
                reflectiveCurveToRelative(1.34f, -3.0f, 3.0f, -3.0f)
                reflectiveCurveToRelative(3.0f, 1.34f, 3.0f, 3.0f)
                reflectiveCurveToRelative(-1.34f, 3.0f, -3.0f, 3.0f)
                close()
                moveTo(23.0f, 7.0f)
                verticalLineToRelative(11.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                lineTo(4.0f, 20.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(17.0f)
                lineTo(21.0f, 7.0f)
                horizontalLineToRelative(2.0f)
                close()
            }
        }
        return _payments!!
    }

private var _payments: ImageVector? = null

public val Icons.Filled.Percent: ImageVector
    get() {
        if (_percent != null) {
            return _percent!!
        }
        _percent = materialIcon(name = "Filled.Percent") {
            materialPath {
                moveTo(7.5f, 11.0f)
                curveTo(9.43f, 11.0f, 11.0f, 9.43f, 11.0f, 7.5f)
                reflectiveCurveTo(9.43f, 4.0f, 7.5f, 4.0f)
                reflectiveCurveTo(4.0f, 5.57f, 4.0f, 7.5f)
                reflectiveCurveTo(5.57f, 11.0f, 7.5f, 11.0f)
                close()
                moveTo(7.5f, 6.0f)
                curveTo(8.33f, 6.0f, 9.0f, 6.67f, 9.0f, 7.5f)
                reflectiveCurveTo(8.33f, 9.0f, 7.5f, 9.0f)
                reflectiveCurveTo(6.0f, 8.33f, 6.0f, 7.5f)
                reflectiveCurveTo(6.67f, 6.0f, 7.5f, 6.0f)
                close()
            }
            materialPath {
                moveTo(4.002f, 18.583f)
                lineToRelative(14.587f, -14.587f)
                lineToRelative(1.414f, 1.414f)
                lineToRelative(-14.587f, 14.587f)
                close()
            }
            materialPath {
                moveTo(16.5f, 13.0f)
                curveToRelative(-1.93f, 0.0f, -3.5f, 1.57f, -3.5f, 3.5f)
                reflectiveCurveToRelative(1.57f, 3.5f, 3.5f, 3.5f)
                reflectiveCurveToRelative(3.5f, -1.57f, 3.5f, -3.5f)
                reflectiveCurveTo(18.43f, 13.0f, 16.5f, 13.0f)
                close()
                moveTo(16.5f, 18.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(17.33f, 18.0f, 16.5f, 18.0f)
                close()
            }
        }
        return _percent!!
    }

private var _percent: ImageVector? = null

public val Icons.Filled.Pets: ImageVector
    get() {
        if (_pets != null) {
            return _pets!!
        }
        _pets = materialIcon(name = "Filled.Pets") {
            materialPath {
                moveTo(4.5f, 9.5f)
                moveToRelative(-2.5f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, 5.0f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, -5.0f, 0.0f)
            }
            materialPath {
                moveTo(9.0f, 5.5f)
                moveToRelative(-2.5f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, 5.0f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, -5.0f, 0.0f)
            }
            materialPath {
                moveTo(15.0f, 5.5f)
                moveToRelative(-2.5f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, 5.0f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, -5.0f, 0.0f)
            }
            materialPath {
                moveTo(19.5f, 9.5f)
                moveToRelative(-2.5f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, 5.0f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, -5.0f, 0.0f)
            }
            materialPath {
                moveTo(17.34f, 14.86f)
                curveToRelative(-0.87f, -1.02f, -1.6f, -1.89f, -2.48f, -2.91f)
                curveToRelative(-0.46f, -0.54f, -1.05f, -1.08f, -1.75f, -1.32f)
                curveToRelative(-0.11f, -0.04f, -0.22f, -0.07f, -0.33f, -0.09f)
                curveToRelative(-0.25f, -0.04f, -0.52f, -0.04f, -0.78f, -0.04f)
                reflectiveCurveToRelative(-0.53f, 0.0f, -0.79f, 0.05f)
                curveToRelative(-0.11f, 0.02f, -0.22f, 0.05f, -0.33f, 0.09f)
                curveToRelative(-0.7f, 0.24f, -1.28f, 0.78f, -1.75f, 1.32f)
                curveToRelative(-0.87f, 1.02f, -1.6f, 1.89f, -2.48f, 2.91f)
                curveToRelative(-1.31f, 1.31f, -2.92f, 2.76f, -2.62f, 4.79f)
                curveToRelative(0.29f, 1.02f, 1.02f, 2.03f, 2.33f, 2.32f)
                curveToRelative(0.73f, 0.15f, 3.06f, -0.44f, 5.54f, -0.44f)
                horizontalLineToRelative(0.18f)
                curveToRelative(2.48f, 0.0f, 4.81f, 0.58f, 5.54f, 0.44f)
                curveToRelative(1.31f, -0.29f, 2.04f, -1.31f, 2.33f, -2.32f)
                curveToRelative(0.31f, -2.04f, -1.3f, -3.49f, -2.61f, -4.8f)
                close()
            }
        }
        return _pets!!
    }

private var _pets: ImageVector? = null

public val Icons.Filled.PhoneAndroid: ImageVector
    get() {
        if (_phoneAndroid != null) {
            return _phoneAndroid!!
        }
        _phoneAndroid = materialIcon(name = "Filled.PhoneAndroid") {
            materialPath {
                moveTo(16.0f, 1.0f)
                lineTo(8.0f, 1.0f)
                curveTo(6.34f, 1.0f, 5.0f, 2.34f, 5.0f, 4.0f)
                verticalLineToRelative(16.0f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                horizontalLineToRelative(8.0f)
                curveToRelative(1.66f, 0.0f, 3.0f, -1.34f, 3.0f, -3.0f)
                lineTo(19.0f, 4.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                close()
                moveTo(14.0f, 21.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(1.0f)
                close()
                moveTo(17.25f, 18.0f)
                lineTo(6.75f, 18.0f)
                lineTo(6.75f, 4.0f)
                horizontalLineToRelative(10.5f)
                verticalLineToRelative(14.0f)
                close()
            }
        }
        return _phoneAndroid!!
    }

private var _phoneAndroid: ImageVector? = null

public val Icons.Filled.PictureAsPdf: ImageVector
    get() {
        if (_pictureAsPdf != null) {
            return _pictureAsPdf!!
        }
        _pictureAsPdf = materialIcon(name = "Filled.PictureAsPdf") {
            materialPath {
                moveTo(20.0f, 2.0f)
                lineTo(8.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(11.5f, 9.5f)
                curveToRelative(0.0f, 0.83f, -0.67f, 1.5f, -1.5f, 1.5f)
                lineTo(9.0f, 11.0f)
                verticalLineToRelative(2.0f)
                lineTo(7.5f, 13.0f)
                lineTo(7.5f, 7.0f)
                lineTo(10.0f, 7.0f)
                curveToRelative(0.83f, 0.0f, 1.5f, 0.67f, 1.5f, 1.5f)
                verticalLineToRelative(1.0f)
                close()
                moveTo(16.5f, 11.5f)
                curveToRelative(0.0f, 0.83f, -0.67f, 1.5f, -1.5f, 1.5f)
                horizontalLineToRelative(-2.5f)
                lineTo(12.5f, 7.0f)
                lineTo(15.0f, 7.0f)
                curveToRelative(0.83f, 0.0f, 1.5f, 0.67f, 1.5f, 1.5f)
                verticalLineToRelative(3.0f)
                close()
                moveTo(20.5f, 8.5f)
                lineTo(19.0f, 8.5f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(1.5f)
                lineTo(20.5f, 11.0f)
                lineTo(19.0f, 11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-1.5f)
                lineTo(17.5f, 7.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(1.5f)
                close()
                moveTo(9.0f, 9.5f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(-1.0f)
                lineTo(9.0f, 8.5f)
                verticalLineToRelative(1.0f)
                close()
                moveTo(4.0f, 6.0f)
                lineTo(2.0f, 6.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(-2.0f)
                lineTo(4.0f, 20.0f)
                lineTo(4.0f, 6.0f)
                close()
                moveTo(14.0f, 11.5f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineToRelative(3.0f)
                close()
            }
        }
        return _pictureAsPdf!!
    }

private var _pictureAsPdf: ImageVector? = null

public val Icons.Filled.PieChart: ImageVector
    get() {
        if (_pieChart != null) {
            return _pieChart!!
        }
        _pieChart = materialIcon(name = "Filled.PieChart") {
            materialPath {
                moveTo(11.0f, 2.0f)
                verticalLineToRelative(20.0f)
                curveToRelative(-5.07f, -0.5f, -9.0f, -4.79f, -9.0f, -10.0f)
                reflectiveCurveToRelative(3.93f, -9.5f, 9.0f, -10.0f)
                close()
                moveTo(13.03f, 2.0f)
                verticalLineToRelative(8.99f)
                lineTo(22.0f, 10.99f)
                curveToRelative(-0.47f, -4.74f, -4.24f, -8.52f, -8.97f, -8.99f)
                close()
                moveTo(13.03f, 13.01f)
                lineTo(13.03f, 22.0f)
                curveToRelative(4.74f, -0.47f, 8.5f, -4.25f, 8.97f, -8.99f)
                horizontalLineToRelative(-8.97f)
                close()
            }
        }
        return _pieChart!!
    }

private var _pieChart: ImageVector? = null

public val Icons.Filled.Plumbing: ImageVector
    get() {
        if (_plumbing != null) {
            return _plumbing!!
        }
        _plumbing = materialIcon(name = "Filled.Plumbing") {
            materialPath {
                moveTo(19.28f, 4.93f)
                lineToRelative(-2.12f, -2.12f)
                curveToRelative(-0.78f, -0.78f, -2.05f, -0.78f, -2.83f, 0.0f)
                lineTo(11.5f, 5.64f)
                lineToRelative(2.12f, 2.12f)
                lineToRelative(2.12f, -2.12f)
                lineToRelative(3.54f, 3.54f)
                curveTo(20.45f, 8.0f, 20.45f, 6.1f, 19.28f, 4.93f)
                close()
            }
            materialPath {
                moveTo(5.49f, 13.77f)
                curveToRelative(0.59f, 0.59f, 1.54f, 0.59f, 2.12f, 0.0f)
                lineToRelative(2.47f, -2.47f)
                lineTo(7.96f, 9.17f)
                lineToRelative(-2.47f, 2.47f)
                curveTo(4.9f, 12.23f, 4.9f, 13.18f, 5.49f, 13.77f)
                lineTo(5.49f, 13.77f)
                close()
            }
            materialPath {
                moveTo(15.04f, 7.76f)
                lineToRelative(-0.71f, 0.71f)
                lineToRelative(-0.71f, 0.71f)
                lineToRelative(-3.18f, -3.18f)
                curveTo(9.85f, 5.4f, 8.9f, 5.4f, 8.32f, 5.99f)
                curveToRelative(-0.59f, 0.59f, -0.59f, 1.54f, 0.0f, 2.12f)
                lineToRelative(3.18f, 3.18f)
                lineTo(10.79f, 12.0f)
                lineToRelative(-6.36f, 6.36f)
                curveToRelative(-0.78f, 0.78f, -0.78f, 2.05f, 0.0f, 2.83f)
                curveToRelative(0.78f, 0.78f, 2.05f, 0.78f, 2.83f, 0.0f)
                lineTo(16.45f, 12.0f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineTo(15.04f, 7.76f)
                close()
            }
        }
        return _plumbing!!
    }

private var _plumbing: ImageVector? = null

public val Icons.Filled.Public: ImageVector
    get() {
        if (_public != null) {
            return _public!!
        }
        _public = materialIcon(name = "Filled.Public") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(11.0f, 19.93f)
                curveToRelative(-3.95f, -0.49f, -7.0f, -3.85f, -7.0f, -7.93f)
                curveToRelative(0.0f, -0.62f, 0.08f, -1.21f, 0.21f, -1.79f)
                lineTo(9.0f, 15.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                verticalLineToRelative(1.93f)
                close()
                moveTo(17.9f, 17.39f)
                curveToRelative(-0.26f, -0.81f, -1.0f, -1.39f, -1.9f, -1.39f)
                horizontalLineToRelative(-1.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(8.0f, 12.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(11.0f, 7.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-0.41f)
                curveToRelative(2.93f, 1.19f, 5.0f, 4.06f, 5.0f, 7.41f)
                curveToRelative(0.0f, 2.08f, -0.8f, 3.97f, -2.1f, 5.39f)
                close()
            }
        }
        return _public!!
    }

private var _public: ImageVector? = null

public val Icons.Filled.Redeem: ImageVector
    get() {
        if (_redeem != null) {
            return _redeem!!
        }
        _redeem = materialIcon(name = "Filled.Redeem") {
            materialPath {
                moveTo(20.0f, 6.0f)
                horizontalLineToRelative(-2.18f)
                curveToRelative(0.11f, -0.31f, 0.18f, -0.65f, 0.18f, -1.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                curveToRelative(-1.05f, 0.0f, -1.96f, 0.54f, -2.5f, 1.35f)
                lineToRelative(-0.5f, 0.67f)
                lineToRelative(-0.5f, -0.68f)
                curveTo(10.96f, 2.54f, 10.05f, 2.0f, 9.0f, 2.0f)
                curveTo(7.34f, 2.0f, 6.0f, 3.34f, 6.0f, 5.0f)
                curveToRelative(0.0f, 0.35f, 0.07f, 0.69f, 0.18f, 1.0f)
                lineTo(4.0f, 6.0f)
                curveToRelative(-1.11f, 0.0f, -1.99f, 0.89f, -1.99f, 2.0f)
                lineTo(2.0f, 19.0f)
                curveToRelative(0.0f, 1.11f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.11f, 0.0f, 2.0f, -0.89f, 2.0f, -2.0f)
                lineTo(22.0f, 8.0f)
                curveToRelative(0.0f, -1.11f, -0.89f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(15.0f, 4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                close()
                moveTo(9.0f, 4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                close()
                moveTo(20.0f, 19.0f)
                lineTo(4.0f, 19.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(20.0f, 14.0f)
                lineTo(4.0f, 14.0f)
                lineTo(4.0f, 8.0f)
                horizontalLineToRelative(5.08f)
                lineTo(7.0f, 10.83f)
                lineTo(8.62f, 12.0f)
                lineTo(11.0f, 8.76f)
                lineToRelative(1.0f, -1.36f)
                lineToRelative(1.0f, 1.36f)
                lineTo(15.38f, 12.0f)
                lineTo(17.0f, 10.83f)
                lineTo(14.92f, 8.0f)
                lineTo(20.0f, 8.0f)
                verticalLineToRelative(6.0f)
                close()
            }
        }
        return _redeem!!
    }

private var _redeem: ImageVector? = null

public val Icons.Filled.Repeat: ImageVector
    get() {
        if (_repeat != null) {
            return _repeat!!
        }
        _repeat = materialIcon(name = "Filled.Repeat") {
            materialPath {
                moveTo(7.0f, 7.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(3.0f)
                lineToRelative(4.0f, -4.0f)
                lineToRelative(-4.0f, -4.0f)
                verticalLineToRelative(3.0f)
                lineTo(5.0f, 5.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(2.0f)
                lineTo(7.0f, 7.0f)
                close()
                moveTo(17.0f, 17.0f)
                lineTo(7.0f, 17.0f)
                verticalLineToRelative(-3.0f)
                lineToRelative(-4.0f, 4.0f)
                lineToRelative(4.0f, 4.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(-6.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(4.0f)
                close()
            }
        }
        return _repeat!!
    }

private var _repeat: ImageVector? = null

public val Icons.Filled.RequestQuote: ImageVector
    get() {
        if (_requestQuote != null) {
            return _requestQuote!!
        }
        _requestQuote = materialIcon(name = "Filled.RequestQuote") {
            materialPath {
                moveTo(14.0f, 2.0f)
                horizontalLineTo(6.0f)
                curveTo(4.9f, 2.0f, 4.01f, 2.9f, 4.01f, 4.0f)
                lineTo(4.0f, 20.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 1.99f, 2.0f)
                horizontalLineTo(18.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(8.0f)
                lineTo(14.0f, 2.0f)
                close()
                moveTo(15.0f, 12.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(-3.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(1.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(12.0f)
                close()
                moveTo(13.0f, 8.0f)
                verticalLineTo(3.5f)
                lineTo(17.5f, 8.0f)
                horizontalLineTo(13.0f)
                close()
            }
        }
        return _requestQuote!!
    }

private var _requestQuote: ImageVector? = null

public val Icons.Filled.Restaurant: ImageVector
    get() {
        if (_restaurant != null) {
            return _restaurant!!
        }
        _restaurant = materialIcon(name = "Filled.Restaurant") {
            materialPath {
                moveTo(11.0f, 9.0f)
                lineTo(9.0f, 9.0f)
                lineTo(9.0f, 2.0f)
                lineTo(7.0f, 2.0f)
                verticalLineToRelative(7.0f)
                lineTo(5.0f, 9.0f)
                lineTo(5.0f, 2.0f)
                lineTo(3.0f, 2.0f)
                verticalLineToRelative(7.0f)
                curveToRelative(0.0f, 2.12f, 1.66f, 3.84f, 3.75f, 3.97f)
                lineTo(6.75f, 22.0f)
                horizontalLineToRelative(2.5f)
                verticalLineToRelative(-9.03f)
                curveTo(11.34f, 12.84f, 13.0f, 11.12f, 13.0f, 9.0f)
                lineTo(13.0f, 2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(7.0f)
                close()
                moveTo(16.0f, 6.0f)
                verticalLineToRelative(8.0f)
                horizontalLineToRelative(2.5f)
                verticalLineToRelative(8.0f)
                lineTo(21.0f, 22.0f)
                lineTo(21.0f, 2.0f)
                curveToRelative(-2.76f, 0.0f, -5.0f, 2.24f, -5.0f, 4.0f)
                close()
            }
        }
        return _restaurant!!
    }

private var _restaurant: ImageVector? = null

public val Icons.Filled.Restore: ImageVector
    get() {
        if (_restore != null) {
            return _restore!!
        }
        _restore = materialIcon(name = "Filled.Restore") {
            materialPath {
                moveTo(13.0f, 3.0f)
                curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
                lineTo(1.0f, 12.0f)
                lineToRelative(3.89f, 3.89f)
                lineToRelative(0.07f, 0.14f)
                lineTo(9.0f, 12.0f)
                lineTo(6.0f, 12.0f)
                curveToRelative(0.0f, -3.87f, 3.13f, -7.0f, 7.0f, -7.0f)
                reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
                reflectiveCurveToRelative(-3.13f, 7.0f, -7.0f, 7.0f)
                curveToRelative(-1.93f, 0.0f, -3.68f, -0.79f, -4.94f, -2.06f)
                lineToRelative(-1.42f, 1.42f)
                curveTo(8.27f, 19.99f, 10.51f, 21.0f, 13.0f, 21.0f)
                curveToRelative(4.97f, 0.0f, 9.0f, -4.03f, 9.0f, -9.0f)
                reflectiveCurveToRelative(-4.03f, -9.0f, -9.0f, -9.0f)
                close()
                moveTo(12.0f, 8.0f)
                verticalLineToRelative(5.0f)
                lineToRelative(4.28f, 2.54f)
                lineToRelative(0.72f, -1.21f)
                lineToRelative(-3.5f, -2.08f)
                lineTo(13.5f, 8.0f)
                lineTo(12.0f, 8.0f)
                close()
            }
        }
        return _restore!!
    }

private var _restore: ImageVector? = null

public val Icons.Filled.Router: ImageVector
    get() {
        if (_router != null) {
            return _router!!
        }
        _router = materialIcon(name = "Filled.Router") {
            materialPath {
                moveTo(20.2f, 5.9f)
                lineToRelative(0.8f, -0.8f)
                curveTo(19.6f, 3.7f, 17.8f, 3.0f, 16.0f, 3.0f)
                reflectiveCurveToRelative(-3.6f, 0.7f, -5.0f, 2.1f)
                lineToRelative(0.8f, 0.8f)
                curveTo(13.0f, 4.8f, 14.5f, 4.2f, 16.0f, 4.2f)
                reflectiveCurveToRelative(3.0f, 0.6f, 4.2f, 1.7f)
                close()
                moveTo(19.3f, 6.7f)
                curveToRelative(-0.9f, -0.9f, -2.1f, -1.4f, -3.3f, -1.4f)
                reflectiveCurveToRelative(-2.4f, 0.5f, -3.3f, 1.4f)
                lineToRelative(0.8f, 0.8f)
                curveToRelative(0.7f, -0.7f, 1.6f, -1.0f, 2.5f, -1.0f)
                curveToRelative(0.9f, 0.0f, 1.8f, 0.3f, 2.5f, 1.0f)
                lineToRelative(0.8f, -0.8f)
                close()
                moveTo(19.0f, 13.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(17.0f, 9.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(4.0f)
                lineTo(5.0f, 13.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(8.0f, 18.0f)
                lineTo(6.0f, 18.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(11.5f, 18.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(15.0f, 18.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
        return _router!!
    }

private var _router: ImageVector? = null

public val Icons.Filled.Savings: ImageVector
    get() {
        if (_savings != null) {
            return _savings!!
        }
        _savings = materialIcon(name = "Filled.Savings") {
            materialPath {
                moveTo(19.83f, 7.5f)
                lineToRelative(-2.27f, -2.27f)
                curveToRelative(0.07f, -0.42f, 0.18f, -0.81f, 0.32f, -1.15f)
                curveTo(17.96f, 3.9f, 18.0f, 3.71f, 18.0f, 3.5f)
                curveTo(18.0f, 2.67f, 17.33f, 2.0f, 16.5f, 2.0f)
                curveToRelative(-1.64f, 0.0f, -3.09f, 0.79f, -4.0f, 2.0f)
                lineToRelative(-5.0f, 0.0f)
                curveTo(4.46f, 4.0f, 2.0f, 6.46f, 2.0f, 9.5f)
                reflectiveCurveTo(4.5f, 21.0f, 4.5f, 21.0f)
                lineToRelative(5.5f, 0.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                lineToRelative(5.5f, 0.0f)
                lineToRelative(1.68f, -5.59f)
                lineTo(22.0f, 14.47f)
                verticalLineTo(7.5f)
                horizontalLineTo(19.83f)
                close()
                moveTo(13.0f, 9.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(5.0f)
                verticalLineTo(9.0f)
                close()
                moveTo(16.0f, 11.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                curveTo(17.0f, 10.55f, 16.55f, 11.0f, 16.0f, 11.0f)
                close()
            }
        }
        return _savings!!
    }

private var _savings: ImageVector? = null

public val Icons.Filled.School: ImageVector
    get() {
        if (_school != null) {
            return _school!!
        }
        _school = materialIcon(name = "Filled.School") {
            materialPath {
                moveTo(5.0f, 13.18f)
                verticalLineToRelative(4.0f)
                lineTo(12.0f, 21.0f)
                lineToRelative(7.0f, -3.82f)
                verticalLineToRelative(-4.0f)
                lineTo(12.0f, 17.0f)
                lineToRelative(-7.0f, -3.82f)
                close()
                moveTo(12.0f, 3.0f)
                lineTo(1.0f, 9.0f)
                lineToRelative(11.0f, 6.0f)
                lineToRelative(9.0f, -4.91f)
                verticalLineTo(17.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(9.0f)
                lineTo(12.0f, 3.0f)
                close()
            }
        }
        return _school!!
    }

private var _school: ImageVector? = null

public val Icons.Filled.Security: ImageVector
    get() {
        if (_security != null) {
            return _security!!
        }
        _security = materialIcon(name = "Filled.Security") {
            materialPath {
                moveTo(12.0f, 1.0f)
                lineTo(3.0f, 5.0f)
                verticalLineToRelative(6.0f)
                curveToRelative(0.0f, 5.55f, 3.84f, 10.74f, 9.0f, 12.0f)
                curveToRelative(5.16f, -1.26f, 9.0f, -6.45f, 9.0f, -12.0f)
                lineTo(21.0f, 5.0f)
                lineToRelative(-9.0f, -4.0f)
                close()
                moveTo(12.0f, 11.99f)
                horizontalLineToRelative(7.0f)
                curveToRelative(-0.53f, 4.12f, -3.28f, 7.79f, -7.0f, 8.94f)
                lineTo(12.0f, 12.0f)
                lineTo(5.0f, 12.0f)
                lineTo(5.0f, 6.3f)
                lineToRelative(7.0f, -3.11f)
                verticalLineToRelative(8.8f)
                close()
            }
        }
        return _security!!
    }

private var _security: ImageVector? = null

public val Icons.Filled.SelfImprovement: ImageVector
    get() {
        if (_selfImprovement != null) {
            return _selfImprovement!!
        }
        _selfImprovement = materialIcon(name = "Filled.SelfImprovement") {
            materialPath {
                moveTo(12.0f, 6.0f)
                moveToRelative(-2.0f, 0.0f)
                arcToRelative(2.0f, 2.0f, 0.0f, true, true, 4.0f, 0.0f)
                arcToRelative(2.0f, 2.0f, 0.0f, true, true, -4.0f, 0.0f)
            }
            materialPath {
                moveTo(21.0f, 16.0f)
                verticalLineToRelative(-2.0f)
                curveToRelative(-2.24f, 0.0f, -4.16f, -0.96f, -5.6f, -2.68f)
                lineToRelative(-1.34f, -1.6f)
                curveTo(13.68f, 9.26f, 13.12f, 9.0f, 12.53f, 9.0f)
                horizontalLineToRelative(-1.05f)
                curveToRelative(-0.59f, 0.0f, -1.15f, 0.26f, -1.53f, 0.72f)
                lineToRelative(-1.34f, 1.6f)
                curveTo(7.16f, 13.04f, 5.24f, 14.0f, 3.0f, 14.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(2.77f, 0.0f, 5.19f, -1.17f, 7.0f, -3.25f)
                verticalLineTo(15.0f)
                lineToRelative(-3.88f, 1.55f)
                curveTo(5.45f, 16.82f, 5.0f, 17.48f, 5.0f, 18.21f)
                curveTo(5.0f, 19.2f, 5.8f, 20.0f, 6.79f, 20.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(-0.5f)
                curveToRelative(0.0f, -1.38f, 1.12f, -2.5f, 2.5f, -2.5f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.28f, 0.0f, 0.5f, 0.22f, 0.5f, 0.5f)
                reflectiveCurveTo(14.78f, 18.0f, 14.5f, 18.0f)
                horizontalLineToRelative(-3.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, 0.67f, -1.5f, 1.5f)
                verticalLineTo(20.0f)
                horizontalLineToRelative(7.21f)
                curveTo(18.2f, 20.0f, 19.0f, 19.2f, 19.0f, 18.21f)
                curveToRelative(0.0f, -0.73f, -0.45f, -1.39f, -1.12f, -1.66f)
                lineTo(14.0f, 15.0f)
                verticalLineToRelative(-2.25f)
                curveTo(15.81f, 14.83f, 18.23f, 16.0f, 21.0f, 16.0f)
                close()
            }
        }
        return _selfImprovement!!
    }

private var _selfImprovement: ImageVector? = null

public val Icons.Filled.Shield: ImageVector
    get() {
        if (_shield != null) {
            return _shield!!
        }
        _shield = materialIcon(name = "Filled.Shield") {
            materialPath {
                moveTo(12.0f, 1.0f)
                lineTo(3.0f, 5.0f)
                verticalLineToRelative(6.0f)
                curveToRelative(0.0f, 5.55f, 3.84f, 10.74f, 9.0f, 12.0f)
                curveToRelative(5.16f, -1.26f, 9.0f, -6.45f, 9.0f, -12.0f)
                verticalLineTo(5.0f)
                lineToRelative(-9.0f, -4.0f)
                close()
            }
        }
        return _shield!!
    }

private var _shield: ImageVector? = null

public val Icons.Filled.ShoppingBag: ImageVector
    get() {
        if (_shoppingBag != null) {
            return _shoppingBag!!
        }
        _shoppingBag = materialIcon(name = "Filled.ShoppingBag") {
            materialPath {
                moveTo(18.0f, 6.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(0.0f, -2.21f, -1.79f, -4.0f, -4.0f, -4.0f)
                reflectiveCurveTo(8.0f, 3.79f, 8.0f, 6.0f)
                horizontalLineTo(6.0f)
                curveTo(4.9f, 6.0f, 4.0f, 6.9f, 4.0f, 8.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(8.0f)
                curveTo(20.0f, 6.9f, 19.1f, 6.0f, 18.0f, 6.0f)
                close()
                moveTo(10.0f, 10.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(10.0f)
                close()
                moveTo(12.0f, 4.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                horizontalLineToRelative(-4.0f)
                curveTo(10.0f, 4.9f, 10.9f, 4.0f, 12.0f, 4.0f)
                close()
                moveTo(16.0f, 10.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(10.0f)
                close()
            }
        }
        return _shoppingBag!!
    }

private var _shoppingBag: ImageVector? = null

public val Icons.Filled.Spa: ImageVector
    get() {
        if (_spa != null) {
            return _spa!!
        }
        _spa = materialIcon(name = "Filled.Spa") {
            materialPath {
                moveTo(8.55f, 12.0f)
                curveToRelative(-1.07f, -0.71f, -2.25f, -1.27f, -3.53f, -1.61f)
                curveToRelative(1.28f, 0.34f, 2.46f, 0.9f, 3.53f, 1.61f)
                close()
                moveTo(18.98f, 10.39f)
                curveToRelative(-1.29f, 0.34f, -2.49f, 0.91f, -3.57f, 1.64f)
                curveToRelative(1.08f, -0.73f, 2.28f, -1.3f, 3.57f, -1.64f)
                close()
            }
            materialPath {
                moveTo(15.49f, 9.63f)
                curveToRelative(-0.18f, -2.79f, -1.31f, -5.51f, -3.43f, -7.63f)
                curveToRelative(-2.14f, 2.14f, -3.32f, 4.86f, -3.55f, 7.63f)
                curveToRelative(1.28f, 0.68f, 2.46f, 1.56f, 3.49f, 2.63f)
                curveToRelative(1.03f, -1.06f, 2.21f, -1.94f, 3.49f, -2.63f)
                close()
                moveTo(8.99f, 12.28f)
                curveToRelative(-0.14f, -0.1f, -0.3f, -0.19f, -0.45f, -0.29f)
                curveToRelative(0.15f, 0.11f, 0.31f, 0.19f, 0.45f, 0.29f)
                close()
                moveTo(15.41f, 12.03f)
                curveToRelative(-0.13f, 0.09f, -0.27f, 0.16f, -0.4f, 0.26f)
                curveToRelative(0.13f, -0.1f, 0.27f, -0.17f, 0.4f, -0.26f)
                close()
                moveTo(12.0f, 15.45f)
                curveTo(9.85f, 12.17f, 6.18f, 10.0f, 2.0f, 10.0f)
                curveToRelative(0.0f, 5.32f, 3.36f, 9.82f, 8.03f, 11.49f)
                curveToRelative(0.63f, 0.23f, 1.29f, 0.4f, 1.97f, 0.51f)
                curveToRelative(0.68f, -0.12f, 1.33f, -0.29f, 1.97f, -0.51f)
                curveTo(18.64f, 19.82f, 22.0f, 15.32f, 22.0f, 10.0f)
                curveToRelative(-4.18f, 0.0f, -7.85f, 2.17f, -10.0f, 5.45f)
                close()
            }
        }
        return _spa!!
    }

private var _spa: ImageVector? = null

public val Icons.Filled.SportsEsports: ImageVector
    get() {
        if (_sportsEsports != null) {
            return _sportsEsports!!
        }
        _sportsEsports = materialIcon(name = "Filled.SportsEsports") {
            materialPath {
                moveTo(21.58f, 16.09f)
                lineToRelative(-1.09f, -7.66f)
                curveTo(20.21f, 6.46f, 18.52f, 5.0f, 16.53f, 5.0f)
                horizontalLineTo(7.47f)
                curveTo(5.48f, 5.0f, 3.79f, 6.46f, 3.51f, 8.43f)
                lineToRelative(-1.09f, 7.66f)
                curveTo(2.2f, 17.63f, 3.39f, 19.0f, 4.94f, 19.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.68f, 0.0f, 1.32f, -0.27f, 1.8f, -0.75f)
                lineTo(9.0f, 16.0f)
                horizontalLineToRelative(6.0f)
                lineToRelative(2.25f, 2.25f)
                curveToRelative(0.48f, 0.48f, 1.13f, 0.75f, 1.8f, 0.75f)
                horizontalLineToRelative(0.0f)
                curveTo(20.61f, 19.0f, 21.8f, 17.63f, 21.58f, 16.09f)
                close()
                moveTo(11.0f, 11.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(11.0f)
                close()
                moveTo(15.0f, 10.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                curveTo(16.0f, 9.55f, 15.55f, 10.0f, 15.0f, 10.0f)
                close()
                moveTo(17.0f, 13.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                curveTo(18.0f, 12.55f, 17.55f, 13.0f, 17.0f, 13.0f)
                close()
            }
        }
        return _sportsEsports!!
    }

private var _sportsEsports: ImageVector? = null

public val Icons.Filled.SportsSoccer: ImageVector
    get() {
        if (_sportsSoccer != null) {
            return _sportsSoccer!!
        }
        _sportsSoccer = materialIcon(name = "Filled.SportsSoccer") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                curveToRelative(0.0f, 5.52f, 4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                curveTo(22.0f, 6.48f, 17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(13.0f, 5.3f)
                lineToRelative(1.35f, -0.95f)
                curveToRelative(1.82f, 0.56f, 3.37f, 1.76f, 4.38f, 3.34f)
                lineToRelative(-0.39f, 1.34f)
                lineToRelative(-1.35f, 0.46f)
                lineTo(13.0f, 6.7f)
                verticalLineTo(5.3f)
                close()
                moveTo(9.65f, 4.35f)
                lineTo(11.0f, 5.3f)
                verticalLineToRelative(1.4f)
                lineTo(7.01f, 9.49f)
                lineTo(5.66f, 9.03f)
                lineTo(5.27f, 7.69f)
                curveTo(6.28f, 6.12f, 7.83f, 4.92f, 9.65f, 4.35f)
                close()
                moveTo(7.08f, 17.11f)
                lineToRelative(-1.14f, 0.1f)
                curveTo(4.73f, 15.81f, 4.0f, 13.99f, 4.0f, 12.0f)
                curveToRelative(0.0f, -0.12f, 0.01f, -0.23f, 0.02f, -0.35f)
                lineToRelative(1.0f, -0.73f)
                lineTo(6.4f, 11.4f)
                lineToRelative(1.46f, 4.34f)
                lineTo(7.08f, 17.11f)
                close()
                moveTo(14.5f, 19.59f)
                curveTo(13.71f, 19.85f, 12.87f, 20.0f, 12.0f, 20.0f)
                reflectiveCurveToRelative(-1.71f, -0.15f, -2.5f, -0.41f)
                lineToRelative(-0.69f, -1.49f)
                lineTo(9.45f, 17.0f)
                horizontalLineToRelative(5.11f)
                lineToRelative(0.64f, 1.11f)
                lineTo(14.5f, 19.59f)
                close()
                moveTo(14.27f, 15.0f)
                horizontalLineTo(9.73f)
                lineToRelative(-1.35f, -4.02f)
                lineTo(12.0f, 8.44f)
                lineToRelative(3.63f, 2.54f)
                lineTo(14.27f, 15.0f)
                close()
                moveTo(18.06f, 17.21f)
                lineToRelative(-1.14f, -0.1f)
                lineToRelative(-0.79f, -1.37f)
                lineToRelative(1.46f, -4.34f)
                lineToRelative(1.39f, -0.47f)
                lineToRelative(1.0f, 0.73f)
                curveTo(19.99f, 11.77f, 20.0f, 11.88f, 20.0f, 12.0f)
                curveTo(20.0f, 13.99f, 19.27f, 15.81f, 18.06f, 17.21f)
                close()
            }
        }
        return _sportsSoccer!!
    }

private var _sportsSoccer: ImageVector? = null

public val Icons.Filled.Storefront: ImageVector
    get() {
        if (_storefront != null) {
            return _storefront!!
        }
        _storefront = materialIcon(name = "Filled.Storefront") {
            materialPath {
                moveTo(21.9f, 8.89f)
                lineToRelative(-1.05f, -4.37f)
                curveToRelative(-0.22f, -0.9f, -1.0f, -1.52f, -1.91f, -1.52f)
                horizontalLineTo(5.05f)
                curveTo(4.15f, 3.0f, 3.36f, 3.63f, 3.15f, 4.52f)
                lineTo(2.1f, 8.89f)
                curveToRelative(-0.24f, 1.02f, -0.02f, 2.06f, 0.62f, 2.88f)
                curveTo(2.8f, 11.88f, 2.91f, 11.96f, 3.0f, 12.06f)
                verticalLineTo(19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-6.94f)
                curveToRelative(0.09f, -0.09f, 0.2f, -0.18f, 0.28f, -0.28f)
                curveTo(21.92f, 10.96f, 22.15f, 9.91f, 21.9f, 8.89f)
                close()
                moveTo(18.91f, 4.99f)
                lineToRelative(1.05f, 4.37f)
                curveToRelative(0.1f, 0.42f, 0.01f, 0.84f, -0.25f, 1.17f)
                curveTo(19.57f, 10.71f, 19.27f, 11.0f, 18.77f, 11.0f)
                curveToRelative(-0.61f, 0.0f, -1.14f, -0.49f, -1.21f, -1.14f)
                lineTo(16.98f, 5.0f)
                lineTo(18.91f, 4.99f)
                close()
                moveTo(13.0f, 5.0f)
                horizontalLineToRelative(1.96f)
                lineToRelative(0.54f, 4.52f)
                curveToRelative(0.05f, 0.39f, -0.07f, 0.78f, -0.33f, 1.07f)
                curveTo(14.95f, 10.85f, 14.63f, 11.0f, 14.22f, 11.0f)
                curveTo(13.55f, 11.0f, 13.0f, 10.41f, 13.0f, 9.69f)
                verticalLineTo(5.0f)
                close()
                moveTo(8.49f, 9.52f)
                lineTo(9.04f, 5.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(4.69f)
                curveTo(11.0f, 10.41f, 10.45f, 11.0f, 9.71f, 11.0f)
                curveToRelative(-0.34f, 0.0f, -0.65f, -0.15f, -0.89f, -0.41f)
                curveTo(8.57f, 10.3f, 8.45f, 9.91f, 8.49f, 9.52f)
                close()
                moveTo(4.04f, 9.36f)
                lineTo(5.05f, 5.0f)
                horizontalLineToRelative(1.97f)
                lineTo(6.44f, 9.86f)
                curveTo(6.36f, 10.51f, 5.84f, 11.0f, 5.23f, 11.0f)
                curveToRelative(-0.49f, 0.0f, -0.8f, -0.29f, -0.93f, -0.47f)
                curveTo(4.03f, 10.21f, 3.94f, 9.78f, 4.04f, 9.36f)
                close()
                moveTo(5.0f, 19.0f)
                verticalLineToRelative(-6.03f)
                curveTo(5.08f, 12.98f, 5.15f, 13.0f, 5.23f, 13.0f)
                curveToRelative(0.87f, 0.0f, 1.66f, -0.36f, 2.24f, -0.95f)
                curveToRelative(0.6f, 0.6f, 1.4f, 0.95f, 2.31f, 0.95f)
                curveToRelative(0.87f, 0.0f, 1.65f, -0.36f, 2.23f, -0.93f)
                curveToRelative(0.59f, 0.57f, 1.39f, 0.93f, 2.29f, 0.93f)
                curveToRelative(0.84f, 0.0f, 1.64f, -0.35f, 2.24f, -0.95f)
                curveToRelative(0.58f, 0.59f, 1.37f, 0.95f, 2.24f, 0.95f)
                curveToRelative(0.08f, 0.0f, 0.15f, -0.02f, 0.23f, -0.03f)
                verticalLineTo(19.0f)
                horizontalLineTo(5.0f)
                close()
            }
        }
        return _storefront!!
    }

private var _storefront: ImageVector? = null

public val Icons.Filled.Subway: ImageVector
    get() {
        if (_subway != null) {
            return _subway!!
        }
        _subway = materialIcon(name = "Filled.Subway") {
            materialPath {
                moveTo(15.5f, 16.0f)
                moveToRelative(-1.0f, 0.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, true, 2.0f, 0.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, true, -2.0f, 0.0f)
            }
            materialPath {
                moveTo(8.5f, 16.0f)
                moveToRelative(-1.0f, 0.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, true, 2.0f, 0.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, true, -2.0f, 0.0f)
            }
            materialPath {
                moveTo(7.01f, 9.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(5.0f)
                horizontalLineToRelative(-10.0f)
                close()
                moveTo(17.8f, 2.8f)
                curveTo(16.0f, 2.09f, 13.86f, 2.0f, 12.0f, 2.0f)
                curveToRelative(-1.86f, 0.0f, -4.0f, 0.09f, -5.8f, 0.8f)
                curveTo(3.53f, 3.84f, 2.0f, 6.05f, 2.0f, 8.86f)
                lineTo(2.0f, 22.0f)
                horizontalLineToRelative(20.0f)
                lineTo(22.0f, 8.86f)
                curveToRelative(0.0f, -2.81f, -1.53f, -5.02f, -4.2f, -6.06f)
                close()
                moveTo(18.0f, 15.88f)
                curveToRelative(0.0f, 1.45f, -1.18f, 2.62f, -2.63f, 2.62f)
                lineToRelative(1.13f, 1.12f)
                lineTo(16.5f, 20.0f)
                lineTo(15.0f, 20.0f)
                lineToRelative(-1.5f, -1.5f)
                horizontalLineToRelative(-2.83f)
                lineTo(9.17f, 20.0f)
                lineTo(7.5f, 20.0f)
                verticalLineToRelative(-0.38f)
                lineToRelative(1.12f, -1.12f)
                curveTo(7.18f, 18.5f, 6.0f, 17.32f, 6.0f, 15.88f)
                lineTo(6.0f, 9.0f)
                curveToRelative(0.0f, -2.63f, 3.0f, -3.0f, 6.0f, -3.0f)
                curveToRelative(3.32f, 0.0f, 6.0f, 0.38f, 6.0f, 3.0f)
                verticalLineToRelative(6.88f)
                close()
            }
        }
        return _subway!!
    }

private var _subway: ImageVector? = null

public val Icons.Filled.SwapHoriz: ImageVector
    get() {
        if (_swapHoriz != null) {
            return _swapHoriz!!
        }
        _swapHoriz = materialIcon(name = "Filled.SwapHoriz") {
            materialPath {
                moveTo(6.99f, 11.0f)
                lineTo(3.0f, 15.0f)
                lineToRelative(3.99f, 4.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineTo(14.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(6.99f)
                verticalLineToRelative(-3.0f)
                close()
                moveTo(21.0f, 9.0f)
                lineToRelative(-3.99f, -4.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(10.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(7.01f)
                verticalLineToRelative(3.0f)
                lineTo(21.0f, 9.0f)
                close()
            }
        }
        return _swapHoriz!!
    }

private var _swapHoriz: ImageVector? = null

public val Icons.Filled.Sync: ImageVector
    get() {
        if (_sync != null) {
            return _sync!!
        }
        _sync = materialIcon(name = "Filled.Sync") {
            materialPath {
                moveTo(12.0f, 4.0f)
                lineTo(12.0f, 1.0f)
                lineTo(8.0f, 5.0f)
                lineToRelative(4.0f, 4.0f)
                lineTo(12.0f, 6.0f)
                curveToRelative(3.31f, 0.0f, 6.0f, 2.69f, 6.0f, 6.0f)
                curveToRelative(0.0f, 1.01f, -0.25f, 1.97f, -0.7f, 2.8f)
                lineToRelative(1.46f, 1.46f)
                curveTo(19.54f, 15.03f, 20.0f, 13.57f, 20.0f, 12.0f)
                curveToRelative(0.0f, -4.42f, -3.58f, -8.0f, -8.0f, -8.0f)
                close()
                moveTo(12.0f, 18.0f)
                curveToRelative(-3.31f, 0.0f, -6.0f, -2.69f, -6.0f, -6.0f)
                curveToRelative(0.0f, -1.01f, 0.25f, -1.97f, 0.7f, -2.8f)
                lineTo(5.24f, 7.74f)
                curveTo(4.46f, 8.97f, 4.0f, 10.43f, 4.0f, 12.0f)
                curveToRelative(0.0f, 4.42f, 3.58f, 8.0f, 8.0f, 8.0f)
                verticalLineToRelative(3.0f)
                lineToRelative(4.0f, -4.0f)
                lineToRelative(-4.0f, -4.0f)
                verticalLineToRelative(3.0f)
                close()
            }
        }
        return _sync!!
    }

private var _sync: ImageVector? = null

public val Icons.Filled.TableChart: ImageVector
    get() {
        if (_tableChart != null) {
            return _tableChart!!
        }
        _tableChart = materialIcon(name = "Filled.TableChart") {
            materialPath {
                moveTo(10.0f, 10.02f)
                horizontalLineToRelative(5.0f)
                lineTo(15.0f, 21.0f)
                horizontalLineToRelative(-5.0f)
                close()
                moveTo(17.0f, 21.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-9.0f)
                horizontalLineToRelative(-5.0f)
                verticalLineToRelative(11.0f)
                close()
                moveTo(20.0f, 3.0f)
                lineTo(5.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(19.0f)
                lineTo(22.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(3.0f, 19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(3.0f)
                lineTo(8.0f, 10.0f)
                lineTo(3.0f, 10.0f)
                verticalLineToRelative(9.0f)
                close()
            }
        }
        return _tableChart!!
    }

private var _tableChart: ImageVector? = null

public val Icons.Filled.Theaters: ImageVector
    get() {
        if (_theaters != null) {
            return _theaters!!
        }
        _theaters = materialIcon(name = "Filled.Theaters") {
            materialPath {
                moveTo(18.0f, 3.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(16.0f, 3.0f)
                lineTo(8.0f, 3.0f)
                verticalLineToRelative(2.0f)
                lineTo(6.0f, 5.0f)
                lineTo(6.0f, 3.0f)
                lineTo(4.0f, 3.0f)
                verticalLineToRelative(18.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                lineTo(20.0f, 3.0f)
                horizontalLineToRelative(-2.0f)
                close()
                moveTo(8.0f, 17.0f)
                lineTo(6.0f, 17.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(8.0f, 13.0f)
                lineTo(6.0f, 13.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(8.0f, 9.0f)
                lineTo(6.0f, 9.0f)
                lineTo(6.0f, 7.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(18.0f, 17.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(18.0f, 13.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(18.0f, 9.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(16.0f, 7.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
        return _theaters!!
    }

private var _theaters: ImageVector? = null

public val Icons.Filled.Today: ImageVector
    get() {
        if (_today != null) {
            return _today!!
        }
        _today = materialIcon(name = "Filled.Today") {
            materialPath {
                moveTo(19.0f, 3.0f)
                horizontalLineToRelative(-1.0f)
                lineTo(18.0f, 1.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                lineTo(8.0f, 3.0f)
                lineTo(8.0f, 1.0f)
                lineTo(6.0f, 1.0f)
                verticalLineToRelative(2.0f)
                lineTo(5.0f, 3.0f)
                curveToRelative(-1.11f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(3.0f, 19.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(21.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(19.0f, 19.0f)
                lineTo(5.0f, 19.0f)
                lineTo(5.0f, 8.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(11.0f)
                close()
                moveTo(7.0f, 10.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(5.0f)
                lineTo(7.0f, 15.0f)
                close()
            }
        }
        return _today!!
    }

private var _today: ImageVector? = null

public val Icons.Filled.TouchApp: ImageVector
    get() {
        if (_touchApp != null) {
            return _touchApp!!
        }
        _touchApp = materialIcon(name = "Filled.TouchApp") {
            materialPath {
                moveTo(9.0f, 11.24f)
                verticalLineTo(7.5f)
                curveTo(9.0f, 6.12f, 10.12f, 5.0f, 11.5f, 5.0f)
                reflectiveCurveTo(14.0f, 6.12f, 14.0f, 7.5f)
                verticalLineToRelative(3.74f)
                curveToRelative(1.21f, -0.81f, 2.0f, -2.18f, 2.0f, -3.74f)
                curveTo(16.0f, 5.01f, 13.99f, 3.0f, 11.5f, 3.0f)
                reflectiveCurveTo(7.0f, 5.01f, 7.0f, 7.5f)
                curveTo(7.0f, 9.06f, 7.79f, 10.43f, 9.0f, 11.24f)
                close()
                moveTo(18.84f, 15.87f)
                lineToRelative(-4.54f, -2.26f)
                curveToRelative(-0.17f, -0.07f, -0.35f, -0.11f, -0.54f, -0.11f)
                horizontalLineTo(13.0f)
                verticalLineToRelative(-6.0f)
                curveTo(13.0f, 6.67f, 12.33f, 6.0f, 11.5f, 6.0f)
                reflectiveCurveTo(10.0f, 6.67f, 10.0f, 7.5f)
                verticalLineToRelative(10.74f)
                curveToRelative(-3.6f, -0.76f, -3.54f, -0.75f, -3.67f, -0.75f)
                curveToRelative(-0.31f, 0.0f, -0.59f, 0.13f, -0.79f, 0.33f)
                lineToRelative(-0.79f, 0.8f)
                lineToRelative(4.94f, 4.94f)
                curveTo(9.96f, 23.83f, 10.34f, 24.0f, 10.75f, 24.0f)
                horizontalLineToRelative(6.79f)
                curveToRelative(0.75f, 0.0f, 1.33f, -0.55f, 1.44f, -1.28f)
                lineToRelative(0.75f, -5.27f)
                curveToRelative(0.01f, -0.07f, 0.02f, -0.14f, 0.02f, -0.2f)
                curveTo(19.75f, 16.63f, 19.37f, 16.09f, 18.84f, 15.87f)
                close()
            }
        }
        return _touchApp!!
    }

private var _touchApp: ImageVector? = null

public val Icons.Filled.Toys: ImageVector
    get() {
        if (_toys != null) {
            return _toys!!
        }
        _toys = materialIcon(name = "Filled.Toys") {
            materialPath {
                moveTo(22.0f, 14.0f)
                curveToRelative(0.0f, -1.95f, -1.4f, -3.57f, -3.25f, -3.92f)
                lineTo(17.4f, 6.05f)
                curveTo(17.0f, 4.82f, 15.85f, 4.0f, 14.56f, 4.0f)
                horizontalLineTo(9.44f)
                curveTo(8.15f, 4.0f, 7.0f, 4.82f, 6.6f, 6.05f)
                lineTo(5.81f, 8.4f)
                lineTo(4.41f, 7.0f)
                lineToRelative(0.29f, -0.29f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineToRelative(-2.0f, 2.0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineTo(3.0f, 8.41f)
                lineToRelative(1.79f, 1.79f)
                curveTo(3.18f, 10.72f, 2.0f, 12.22f, 2.0f, 14.0f)
                curveToRelative(0.0f, 1.5f, 0.83f, 2.79f, 2.05f, 3.48f)
                curveTo(4.28f, 18.9f, 5.51f, 20.0f, 7.0f, 20.0f)
                curveToRelative(1.3f, 0.0f, 2.4f, -0.84f, 2.82f, -2.0f)
                horizontalLineToRelative(4.37f)
                curveToRelative(0.41f, 1.16f, 1.51f, 2.0f, 2.82f, 2.0f)
                curveToRelative(1.49f, 0.0f, 2.72f, -1.1f, 2.95f, -2.52f)
                curveTo(21.17f, 16.79f, 22.0f, 15.5f, 22.0f, 14.0f)
                close()
                moveTo(7.0f, 18.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveTo(7.55f, 18.0f, 7.0f, 18.0f)
                close()
                moveTo(11.0f, 10.0f)
                horizontalLineTo(7.41f)
                lineTo(7.39f, 9.98f)
                lineToRelative(1.1f, -3.3f)
                curveTo(8.63f, 6.27f, 9.01f, 6.0f, 9.44f, 6.0f)
                horizontalLineTo(11.0f)
                verticalLineTo(10.0f)
                close()
                moveTo(13.0f, 6.0f)
                horizontalLineToRelative(1.56f)
                curveToRelative(0.43f, 0.0f, 0.81f, 0.27f, 0.95f, 0.68f)
                lineTo(16.61f, 10.0f)
                horizontalLineTo(13.0f)
                verticalLineTo(6.0f)
                close()
                moveTo(17.0f, 18.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveTo(17.55f, 18.0f, 17.0f, 18.0f)
                close()
            }
        }
        return _toys!!
    }

private var _toys: ImageVector? = null

public val Icons.Filled.Train: ImageVector
    get() {
        if (_train != null) {
            return _train!!
        }
        _train = materialIcon(name = "Filled.Train") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveToRelative(-4.0f, 0.0f, -8.0f, 0.5f, -8.0f, 4.0f)
                verticalLineToRelative(9.5f)
                curveTo(4.0f, 17.43f, 5.57f, 19.0f, 7.5f, 19.0f)
                lineTo(6.0f, 20.5f)
                verticalLineToRelative(0.5f)
                horizontalLineToRelative(2.23f)
                lineToRelative(2.0f, -2.0f)
                lineTo(14.0f, 19.0f)
                lineToRelative(2.0f, 2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-0.5f)
                lineTo(16.5f, 19.0f)
                curveToRelative(1.93f, 0.0f, 3.5f, -1.57f, 3.5f, -3.5f)
                lineTo(20.0f, 6.0f)
                curveToRelative(0.0f, -3.5f, -3.58f, -4.0f, -8.0f, -4.0f)
                close()
                moveTo(7.5f, 17.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(6.67f, 14.0f, 7.5f, 14.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(8.33f, 17.0f, 7.5f, 17.0f)
                close()
                moveTo(11.0f, 10.0f)
                lineTo(6.0f, 10.0f)
                lineTo(6.0f, 6.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(13.0f, 10.0f)
                lineTo(13.0f, 6.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(-5.0f)
                close()
                moveTo(16.5f, 17.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
            }
        }
        return _train!!
    }

private var _train: ImageVector? = null

public val Icons.Filled.Tune: ImageVector
    get() {
        if (_tune != null) {
            return _tune!!
        }
        _tune = materialIcon(name = "Filled.Tune") {
            materialPath {
                moveTo(3.0f, 17.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(-2.0f)
                lineTo(3.0f, 17.0f)
                close()
                moveTo(3.0f, 5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(10.0f)
                lineTo(13.0f, 5.0f)
                lineTo(3.0f, 5.0f)
                close()
                moveTo(13.0f, 21.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(2.0f)
                close()
                moveTo(7.0f, 9.0f)
                verticalLineToRelative(2.0f)
                lineTo(3.0f, 11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                lineTo(9.0f, 9.0f)
                lineTo(7.0f, 9.0f)
                close()
                moveTo(21.0f, 13.0f)
                verticalLineToRelative(-2.0f)
                lineTo(11.0f, 11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(10.0f)
                close()
                moveTo(15.0f, 9.0f)
                horizontalLineToRelative(2.0f)
                lineTo(17.0f, 7.0f)
                horizontalLineToRelative(4.0f)
                lineTo(21.0f, 5.0f)
                horizontalLineToRelative(-4.0f)
                lineTo(17.0f, 3.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(6.0f)
                close()
            }
        }
        return _tune!!
    }

private var _tune: ImageVector? = null

public val Icons.Filled.Tv: ImageVector
    get() {
        if (_tv != null) {
            return _tv!!
        }
        _tv = materialIcon(name = "Filled.Tv") {
            materialPath {
                moveTo(21.0f, 3.0f)
                lineTo(3.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(5.0f)
                curveToRelative(1.1f, 0.0f, 1.99f, -0.9f, 1.99f, -2.0f)
                lineTo(23.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(21.0f, 17.0f)
                lineTo(3.0f, 17.0f)
                lineTo(3.0f, 5.0f)
                horizontalLineToRelative(18.0f)
                verticalLineToRelative(12.0f)
                close()
            }
        }
        return _tv!!
    }

private var _tv: ImageVector? = null

public val Icons.Filled.TwoWheeler: ImageVector
    get() {
        if (_twoWheeler != null) {
            return _twoWheeler!!
        }
        _twoWheeler = materialIcon(name = "Filled.TwoWheeler") {
            materialPath {
                moveTo(20.0f, 11.0f)
                curveToRelative(-0.18f, 0.0f, -0.36f, 0.03f, -0.53f, 0.05f)
                lineTo(17.41f, 9.0f)
                horizontalLineTo(20.0f)
                verticalLineTo(6.0f)
                lineToRelative(-3.72f, 1.86f)
                lineTo(13.41f, 5.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(3.59f)
                lineToRelative(2.0f, 2.0f)
                horizontalLineTo(11.0f)
                lineToRelative(-4.0f, 2.0f)
                lineTo(5.0f, 9.0f)
                horizontalLineTo(0.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(-2.21f, 0.0f, -4.0f, 1.79f, -4.0f, 4.0f)
                curveToRelative(0.0f, 2.21f, 1.79f, 4.0f, 4.0f, 4.0f)
                curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
                lineToRelative(2.0f, 2.0f)
                horizontalLineToRelative(3.0f)
                lineToRelative(3.49f, -6.1f)
                lineToRelative(1.01f, 1.01f)
                curveTo(16.59f, 12.64f, 16.0f, 13.75f, 16.0f, 15.0f)
                curveToRelative(0.0f, 2.21f, 1.79f, 4.0f, 4.0f, 4.0f)
                curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
                curveTo(24.0f, 12.79f, 22.21f, 11.0f, 20.0f, 11.0f)
                close()
                moveTo(4.0f, 17.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                curveTo(6.0f, 16.1f, 5.1f, 17.0f, 4.0f, 17.0f)
                close()
                moveTo(20.0f, 17.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                curveTo(22.0f, 16.1f, 21.1f, 17.0f, 20.0f, 17.0f)
                close()
            }
        }
        return _twoWheeler!!
    }

private var _twoWheeler: ImageVector? = null

public val Icons.Filled.Unarchive: ImageVector
    get() {
        if (_unarchive != null) {
            return _unarchive!!
        }
        _unarchive = materialIcon(name = "Filled.Unarchive") {
            materialPath {
                moveTo(20.55f, 5.22f)
                lineToRelative(-1.39f, -1.68f)
                curveTo(18.88f, 3.21f, 18.47f, 3.0f, 18.0f, 3.0f)
                horizontalLineTo(6.0f)
                curveTo(5.53f, 3.0f, 5.12f, 3.21f, 4.85f, 3.55f)
                lineTo(3.46f, 5.22f)
                curveTo(3.17f, 5.57f, 3.0f, 6.01f, 3.0f, 6.5f)
                verticalLineTo(19.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(6.5f)
                curveTo(21.0f, 6.01f, 20.83f, 5.57f, 20.55f, 5.22f)
                close()
                moveTo(12.0f, 9.5f)
                lineToRelative(5.5f, 5.5f)
                horizontalLineTo(14.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(6.5f)
                lineTo(12.0f, 9.5f)
                close()
                moveTo(5.12f, 5.0f)
                lineToRelative(0.82f, -1.0f)
                horizontalLineToRelative(12.0f)
                lineToRelative(0.93f, 1.0f)
                horizontalLineTo(5.12f)
                close()
            }
        }
        return _unarchive!!
    }

private var _unarchive: ImageVector? = null

public val Icons.Filled.Upload: ImageVector
    get() {
        if (_upload != null) {
            return _upload!!
        }
        _upload = materialIcon(name = "Filled.Upload") {
            materialPath {
                moveTo(5.0f, 20.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(20.0f)
                close()
                moveTo(5.0f, 10.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(-6.0f)
                horizontalLineToRelative(4.0f)
                lineToRelative(-7.0f, -7.0f)
                lineTo(5.0f, 10.0f)
                close()
            }
        }
        return _upload!!
    }

private var _upload: ImageVector? = null

public val Icons.Filled.Verified: ImageVector
    get() {
        if (_verified != null) {
            return _verified!!
        }
        _verified = materialIcon(name = "Filled.Verified") {
            materialPath {
                moveTo(23.0f, 12.0f)
                lineToRelative(-2.44f, -2.79f)
                lineToRelative(0.34f, -3.69f)
                lineToRelative(-3.61f, -0.82f)
                lineTo(15.4f, 1.5f)
                lineTo(12.0f, 2.96f)
                lineTo(8.6f, 1.5f)
                lineTo(6.71f, 4.69f)
                lineTo(3.1f, 5.5f)
                lineTo(3.44f, 9.2f)
                lineTo(1.0f, 12.0f)
                lineToRelative(2.44f, 2.79f)
                lineToRelative(-0.34f, 3.7f)
                lineToRelative(3.61f, 0.82f)
                lineTo(8.6f, 22.5f)
                lineToRelative(3.4f, -1.47f)
                lineToRelative(3.4f, 1.46f)
                lineToRelative(1.89f, -3.19f)
                lineToRelative(3.61f, -0.82f)
                lineToRelative(-0.34f, -3.69f)
                lineTo(23.0f, 12.0f)
                close()
                moveTo(10.09f, 16.72f)
                lineToRelative(-3.8f, -3.81f)
                lineToRelative(1.48f, -1.48f)
                lineToRelative(2.32f, 2.33f)
                lineToRelative(5.85f, -5.87f)
                lineToRelative(1.48f, 1.48f)
                lineTo(10.09f, 16.72f)
                close()
            }
        }
        return _verified!!
    }

private var _verified: ImageVector? = null

public val Icons.Filled.ViewStream: ImageVector
    get() {
        if (_viewStream != null) {
            return _viewStream!!
        }
        _viewStream = materialIcon(name = "Filled.ViewStream") {
            materialPath {
                moveTo(3.0f, 17.0f)
                verticalLineToRelative(-2.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 19.0f, 3.0f, 18.1f, 3.0f, 17.0f)
                close()
                moveTo(3.0f, 7.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(7.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 5.0f, 3.0f, 5.9f, 3.0f, 7.0f)
                close()
            }
        }
        return _viewStream!!
    }

private var _viewStream: ImageVector? = null

public val Icons.Filled.Visibility: ImageVector
    get() {
        if (_visibility != null) {
            return _visibility!!
        }
        _visibility = materialIcon(name = "Filled.Visibility") {
            materialPath {
                moveTo(12.0f, 4.5f)
                curveTo(7.0f, 4.5f, 2.73f, 7.61f, 1.0f, 12.0f)
                curveToRelative(1.73f, 4.39f, 6.0f, 7.5f, 11.0f, 7.5f)
                reflectiveCurveToRelative(9.27f, -3.11f, 11.0f, -7.5f)
                curveToRelative(-1.73f, -4.39f, -6.0f, -7.5f, -11.0f, -7.5f)
                close()
                moveTo(12.0f, 17.0f)
                curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
                reflectiveCurveToRelative(2.24f, -5.0f, 5.0f, -5.0f)
                reflectiveCurveToRelative(5.0f, 2.24f, 5.0f, 5.0f)
                reflectiveCurveToRelative(-2.24f, 5.0f, -5.0f, 5.0f)
                close()
                moveTo(12.0f, 9.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
                reflectiveCurveToRelative(3.0f, -1.34f, 3.0f, -3.0f)
                reflectiveCurveToRelative(-1.34f, -3.0f, -3.0f, -3.0f)
                close()
            }
        }
        return _visibility!!
    }

private var _visibility: ImageVector? = null

public val Icons.Filled.VolunteerActivism: ImageVector
    get() {
        if (_volunteerActivism != null) {
            return _volunteerActivism!!
        }
        _volunteerActivism = materialIcon(name = "Filled.VolunteerActivism") {
            materialPath {
                moveTo(1.0f, 11.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(11.0f)
                horizontalLineToRelative(-4.0f)
                close()
            }
            materialPath {
                moveTo(16.0f, 3.25f)
                curveTo(16.65f, 2.49f, 17.66f, 2.0f, 18.7f, 2.0f)
                curveTo(20.55f, 2.0f, 22.0f, 3.45f, 22.0f, 5.3f)
                curveToRelative(0.0f, 2.27f, -2.91f, 4.9f, -6.0f, 7.7f)
                curveToRelative(-3.09f, -2.81f, -6.0f, -5.44f, -6.0f, -7.7f)
                curveTo(10.0f, 3.45f, 11.45f, 2.0f, 13.3f, 2.0f)
                curveTo(14.34f, 2.0f, 15.35f, 2.49f, 16.0f, 3.25f)
                close()
            }
            materialPath {
                moveTo(20.0f, 17.0f)
                horizontalLineToRelative(-7.0f)
                lineToRelative(-2.09f, -0.73f)
                lineToRelative(0.33f, -0.94f)
                lineTo(13.0f, 16.0f)
                horizontalLineToRelative(2.82f)
                curveToRelative(0.65f, 0.0f, 1.18f, -0.53f, 1.18f, -1.18f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.49f, -0.31f, -0.93f, -0.77f, -1.11f)
                lineTo(8.97f, 11.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(9.02f)
                lineTo(14.0f, 22.0f)
                lineToRelative(8.01f, -3.0f)
                verticalLineToRelative(0.0f)
                curveTo(22.0f, 17.9f, 21.11f, 17.0f, 20.0f, 17.0f)
                close()
            }
        }
        return _volunteerActivism!!
    }

private var _volunteerActivism: ImageVector? = null

public val Icons.Filled.Watch: ImageVector
    get() {
        if (_watch != null) {
            return _watch!!
        }
        _watch = materialIcon(name = "Filled.Watch") {
            materialPath {
                moveTo(20.0f, 12.0f)
                curveToRelative(0.0f, -2.54f, -1.19f, -4.81f, -3.04f, -6.27f)
                lineTo(16.0f, 0.0f)
                horizontalLineTo(8.0f)
                lineToRelative(-0.95f, 5.73f)
                curveTo(5.19f, 7.19f, 4.0f, 9.45f, 4.0f, 12.0f)
                reflectiveCurveToRelative(1.19f, 4.81f, 3.05f, 6.27f)
                lineTo(8.0f, 24.0f)
                horizontalLineToRelative(8.0f)
                lineToRelative(0.96f, -5.73f)
                curveTo(18.81f, 16.81f, 20.0f, 14.54f, 20.0f, 12.0f)
                close()
                moveTo(6.0f, 12.0f)
                curveToRelative(0.0f, -3.31f, 2.69f, -6.0f, 6.0f, -6.0f)
                reflectiveCurveToRelative(6.0f, 2.69f, 6.0f, 6.0f)
                reflectiveCurveToRelative(-2.69f, 6.0f, -6.0f, 6.0f)
                reflectiveCurveToRelative(-6.0f, -2.69f, -6.0f, -6.0f)
                close()
            }
        }
        return _watch!!
    }

private var _watch: ImageVector? = null

public val Icons.Filled.WaterDrop: ImageVector
    get() {
        if (_waterDrop != null) {
            return _waterDrop!!
        }
        _waterDrop = materialIcon(name = "Filled.WaterDrop") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveToRelative(-5.33f, 4.55f, -8.0f, 8.48f, -8.0f, 11.8f)
                curveToRelative(0.0f, 4.98f, 3.8f, 8.2f, 8.0f, 8.2f)
                reflectiveCurveToRelative(8.0f, -3.22f, 8.0f, -8.2f)
                curveTo(20.0f, 10.48f, 17.33f, 6.55f, 12.0f, 2.0f)
                close()
                moveTo(7.83f, 14.0f)
                curveToRelative(0.37f, 0.0f, 0.67f, 0.26f, 0.74f, 0.62f)
                curveToRelative(0.41f, 2.22f, 2.28f, 2.98f, 3.64f, 2.87f)
                curveToRelative(0.43f, -0.02f, 0.79f, 0.32f, 0.79f, 0.75f)
                curveToRelative(0.0f, 0.4f, -0.32f, 0.73f, -0.72f, 0.75f)
                curveToRelative(-2.13f, 0.13f, -4.62f, -1.09f, -5.19f, -4.12f)
                curveTo(7.01f, 14.42f, 7.37f, 14.0f, 7.83f, 14.0f)
                close()
            }
        }
        return _waterDrop!!
    }

private var _waterDrop: ImageVector? = null

public val Icons.Filled.Weekend: ImageVector
    get() {
        if (_weekend != null) {
            return _weekend!!
        }
        _weekend = materialIcon(name = "Filled.Weekend") {
            materialPath {
                moveTo(21.0f, 10.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -1.1f, -0.89f, -2.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(5.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(18.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-5.0f)
                curveTo(23.0f, 10.9f, 22.1f, 10.0f, 21.0f, 10.0f)
                close()
                moveTo(18.0f, 5.0f)
                horizontalLineTo(6.0f)
                curveTo(4.9f, 5.0f, 4.0f, 5.9f, 4.0f, 7.0f)
                verticalLineToRelative(2.15f)
                curveToRelative(1.16f, 0.41f, 2.0f, 1.52f, 2.0f, 2.81f)
                verticalLineTo(14.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(-2.03f)
                curveToRelative(0.0f, -1.3f, 0.84f, -2.4f, 2.0f, -2.81f)
                verticalLineTo(7.0f)
                curveTo(20.0f, 5.9f, 19.1f, 5.0f, 18.0f, 5.0f)
                close()
            }
        }
        return _weekend!!
    }

private var _weekend: ImageVector? = null

public val Icons.Filled.Wifi: ImageVector
    get() {
        if (_wifi != null) {
            return _wifi!!
        }
        _wifi = materialIcon(name = "Filled.Wifi") {
            materialPath {
                moveTo(1.0f, 9.0f)
                lineToRelative(2.0f, 2.0f)
                curveToRelative(4.97f, -4.97f, 13.03f, -4.97f, 18.0f, 0.0f)
                lineToRelative(2.0f, -2.0f)
                curveTo(16.93f, 2.93f, 7.08f, 2.93f, 1.0f, 9.0f)
                close()
                moveTo(9.0f, 17.0f)
                lineToRelative(3.0f, 3.0f)
                lineToRelative(3.0f, -3.0f)
                curveToRelative(-1.65f, -1.66f, -4.34f, -1.66f, -6.0f, 0.0f)
                close()
                moveTo(5.0f, 13.0f)
                lineToRelative(2.0f, 2.0f)
                curveToRelative(2.76f, -2.76f, 7.24f, -2.76f, 10.0f, 0.0f)
                lineToRelative(2.0f, -2.0f)
                curveTo(15.14f, 9.14f, 8.87f, 9.14f, 5.0f, 13.0f)
                close()
            }
        }
        return _wifi!!
    }

private var _wifi: ImageVector? = null

public val Icons.Filled.Work: ImageVector
    get() {
        if (_work != null) {
            return _work!!
        }
        _work = materialIcon(name = "Filled.Work") {
            materialPath {
                moveTo(20.0f, 6.0f)
                horizontalLineToRelative(-4.0f)
                lineTo(16.0f, 4.0f)
                curveToRelative(0.0f, -1.11f, -0.89f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-4.0f)
                curveToRelative(-1.11f, 0.0f, -2.0f, 0.89f, -2.0f, 2.0f)
                verticalLineToRelative(2.0f)
                lineTo(4.0f, 6.0f)
                curveToRelative(-1.11f, 0.0f, -1.99f, 0.89f, -1.99f, 2.0f)
                lineTo(2.0f, 19.0f)
                curveToRelative(0.0f, 1.11f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.11f, 0.0f, 2.0f, -0.89f, 2.0f, -2.0f)
                lineTo(22.0f, 8.0f)
                curveToRelative(0.0f, -1.11f, -0.89f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(14.0f, 6.0f)
                horizontalLineToRelative(-4.0f)
                lineTo(10.0f, 4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
        return _work!!
    }

private var _work: ImageVector? = null

public val Icons.Filled.WorkspacePremium: ImageVector
    get() {
        if (_workspacePremium != null) {
            return _workspacePremium!!
        }
        _workspacePremium = materialIcon(name = "Filled.WorkspacePremium") {
            materialPath {
                moveTo(9.68f, 13.69f)
                lineTo(12.0f, 11.93f)
                lineToRelative(2.31f, 1.76f)
                lineToRelative(-0.88f, -2.85f)
                lineTo(15.75f, 9.0f)
                horizontalLineToRelative(-2.84f)
                lineTo(12.0f, 6.19f)
                lineTo(11.09f, 9.0f)
                horizontalLineTo(8.25f)
                lineToRelative(2.31f, 1.84f)
                lineTo(9.68f, 13.69f)
                close()
                moveTo(20.0f, 10.0f)
                curveToRelative(0.0f, -4.42f, -3.58f, -8.0f, -8.0f, -8.0f)
                reflectiveCurveToRelative(-8.0f, 3.58f, -8.0f, 8.0f)
                curveToRelative(0.0f, 2.03f, 0.76f, 3.87f, 2.0f, 5.28f)
                verticalLineTo(23.0f)
                lineToRelative(6.0f, -2.0f)
                lineToRelative(6.0f, 2.0f)
                verticalLineToRelative(-7.72f)
                curveTo(19.24f, 13.87f, 20.0f, 12.03f, 20.0f, 10.0f)
                close()
                moveTo(12.0f, 4.0f)
                curveToRelative(3.31f, 0.0f, 6.0f, 2.69f, 6.0f, 6.0f)
                reflectiveCurveToRelative(-2.69f, 6.0f, -6.0f, 6.0f)
                reflectiveCurveToRelative(-6.0f, -2.69f, -6.0f, -6.0f)
                reflectiveCurveTo(8.69f, 4.0f, 12.0f, 4.0f)
                close()
            }
        }
        return _workspacePremium!!
    }

private var _workspacePremium: ImageVector? = null

public val Icons.Filled.Yard: ImageVector
    get() {
        if (_yard != null) {
            return _yard!!
        }
        _yard = materialIcon(name = "Filled.Yard") {
            materialPath {
                moveTo(20.0f, 2.0f)
                horizontalLineTo(4.0f)
                curveTo(2.9f, 2.0f, 2.0f, 2.9f, 2.0f, 4.0f)
                verticalLineToRelative(16.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(4.0f)
                curveTo(22.0f, 2.9f, 21.1f, 2.0f, 20.0f, 2.0f)
                close()
                moveTo(8.0f, 8.22f)
                curveToRelative(0.0f, -0.86f, 0.7f, -1.56f, 1.56f, -1.56f)
                curveToRelative(0.33f, 0.0f, 0.64f, 0.1f, 0.89f, 0.28f)
                lineToRelative(-0.01f, -0.12f)
                curveToRelative(0.0f, -0.86f, 0.7f, -1.56f, 1.56f, -1.56f)
                reflectiveCurveToRelative(1.56f, 0.7f, 1.56f, 1.56f)
                lineToRelative(-0.01f, 0.12f)
                curveToRelative(0.26f, -0.18f, 0.56f, -0.28f, 0.89f, -0.28f)
                curveToRelative(0.86f, 0.0f, 1.56f, 0.7f, 1.56f, 1.56f)
                curveToRelative(0.0f, 0.62f, -0.37f, 1.16f, -0.89f, 1.4f)
                curveTo(15.63f, 9.87f, 16.0f, 10.41f, 16.0f, 11.03f)
                curveToRelative(0.0f, 0.86f, -0.7f, 1.56f, -1.56f, 1.56f)
                curveToRelative(-0.33f, 0.0f, -0.64f, -0.11f, -0.89f, -0.28f)
                lineToRelative(0.01f, 0.12f)
                curveToRelative(0.0f, 0.86f, -0.7f, 1.56f, -1.56f, 1.56f)
                reflectiveCurveToRelative(-1.56f, -0.7f, -1.56f, -1.56f)
                lineToRelative(0.01f, -0.12f)
                curveToRelative(-0.26f, 0.18f, -0.56f, 0.28f, -0.89f, 0.28f)
                curveTo(8.7f, 12.59f, 8.0f, 11.89f, 8.0f, 11.03f)
                curveToRelative(0.0f, -0.62f, 0.37f, -1.16f, 0.89f, -1.4f)
                curveTo(8.37f, 9.38f, 8.0f, 8.84f, 8.0f, 8.22f)
                close()
                moveTo(12.0f, 19.0f)
                curveToRelative(-3.31f, 0.0f, -6.0f, -2.69f, -6.0f, -6.0f)
                curveTo(9.31f, 13.0f, 12.0f, 15.69f, 12.0f, 19.0f)
                curveToRelative(0.0f, -3.31f, 2.69f, -6.0f, 6.0f, -6.0f)
                curveTo(18.0f, 16.31f, 15.31f, 19.0f, 12.0f, 19.0f)
                close()
            }
            materialPath {
                moveTo(12.0f, 9.62f)
                moveToRelative(-1.56f, 0.0f)
                arcToRelative(1.56f, 1.56f, 0.0f, true, true, 3.12f, 0.0f)
                arcToRelative(1.56f, 1.56f, 0.0f, true, true, -3.12f, 0.0f)
            }
        }
        return _yard!!
    }

private var _yard: ImageVector? = null


public val Icons.Filled.PushPin: ImageVector
    get() {
        if (_pushPin != null) {
            return _pushPin!!
        }
        _pushPin = materialIcon(name = "Filled.PushPin") {
            materialPath {
                moveTo(16.0f, 9.0f)
                verticalLineTo(4.0f)
                lineToRelative(1.0f, 0.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(7.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                lineToRelative(1.0f, 0.0f)
                verticalLineToRelative(5.0f)
                curveToRelative(0.0f, 1.66f, -1.34f, 3.0f, -3.0f, 3.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(5.97f)
                verticalLineToRelative(7.0f)
                lineToRelative(1.0f, 1.0f)
                lineToRelative(1.0f, -1.0f)
                verticalLineToRelative(-7.0f)
                horizontalLineTo(19.0f)
                verticalLineToRelative(-2.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, -1.34f, -3.0f, -3.0f)
                close()
            }
        }
        return _pushPin!!
    }

private var _pushPin: ImageVector? = null
