// File: app/src/main/java/com/folioledger/app/ui/iconpack/PhosphorIconResolver.kt
package com.folioledger.app.ui.iconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal object PhosphorIconResolver {

    private fun phosphor(name: String, block: PathBuilder.() -> Unit): ImageVector {
        return ImageVector.Builder(
            name = "Phosphor.$name",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Black),
            pathBuilder = block
        ).build()
    }

    private val forkKnife by lazy {
        phosphor("ForkKnife") {
            moveTo(11.0f, 9.0f)
            curveToRelative(0.0f, 1.66f, -1.34f, 3.0f, -3.0f, 3.0f)
            lineTo(7.0f, 12.0f)
            verticalLineToRelative(9.0f)
            curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
            reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
            verticalLineToRelative(-9.0f)
            horizontalLineToRelative(-1.0f)
            curveToRelative(-1.66f, 0.0f, -3.0f, -1.34f, -3.0f, -3.0f)
            lineTo(1.0f, 3.0f)
            curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
            reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
            verticalLineToRelative(5.0f)
            horizontalLineToRelative(1.0f)
            lineTo(4.0f, 3.0f)
            curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
            reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
            verticalLineToRelative(5.0f)
            horizontalLineToRelative(1.0f)
            lineTo(7.0f, 3.0f)
            curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
            reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
            verticalLineToRelative(5.0f)
            horizontalLineToRelative(1.0f)
            lineTo(10.0f, 3.0f)
            curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
            reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
            verticalLineToRelative(6.0f)
            close()
            moveTo(22.0f, 8.5f)
            curveToRelative(0.0f, 3.04f, -2.46f, 5.5f, -5.5f, 5.5f)
            horizontalLineToRelative(-0.5f)
            verticalLineToRelative(7.0f)
            curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
            reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
            lineTo(14.0f, 3.0f)
            curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
            curveToRelative(3.87f, 0.0f, 7.0f, 2.91f, 7.0f, 6.5f)
            close()
        }
    }

    private val coffee by lazy {
        phosphor("Coffee") {
            moveTo(23.0f, 10.0f)
            curveToRelative(0.0f, -2.21f, -1.79f, -4.0f, -4.0f, -4.0f)
            horizontalLineToRelative(-1.0f)
            lineTo(18.0f, 5.0f)
            curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
            lineTo(4.0f, 3.0f)
            curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
            verticalLineToRelative(10.0f)
            curveToRelative(0.0f, 3.87f, 3.13f, 7.0f, 7.0f, 7.0f)
            horizontalLineToRelative(4.0f)
            curveToRelative(3.87f, 0.0f, 7.0f, -3.13f, 7.0f, -7.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(1.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            verticalLineToRelative(-1.0f)
            close()
            moveTo(21.0f, 11.0f)
            curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
            horizontalLineToRelative(-2.0f)
            lineTo(18.0f, 8.0f)
            horizontalLineToRelative(1.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
            verticalLineToRelative(1.0f)
            close()
            moveTo(2.0f, 23.0f)
            curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
            horizontalLineToRelative(18.0f)
            curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
            reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
            lineTo(3.0f, 24.0f)
            curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
            close()
        }
    }

    private val shoppingBag by lazy {
        phosphor("ShoppingBag") {
            moveTo(19.0f, 6.0f)
            horizontalLineToRelative(-2.0f)
            curveToRelative(0.0f, -2.76f, -2.24f, -5.0f, -5.0f, -5.0f)
            reflectiveCurveTo(7.0f, 3.24f, 7.0f, 6.0f)
            lineTo(5.0f, 6.0f)
            curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
            verticalLineToRelative(12.0f)
            curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
            horizontalLineToRelative(12.0f)
            curveToRelative(1.66f, 0.0f, 3.0f, -1.34f, 3.0f, -3.0f)
            lineTo(21.0f, 8.0f)
            curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
            close()
            moveTo(12.0f, 3.0f)
            curveToRelative(1.66f, 0.0f, 3.0f, 1.34f, 3.0f, 3.0f)
            lineTo(9.0f, 6.0f)
            curveToRelative(0.0f, -1.66f, 1.34f, -3.0f, 3.0f, -3.0f)
            close()
        }
    }

    private val shoppingCart by lazy {
        phosphor("ShoppingCart") {
            moveTo(22.73f, 7.24f)
            curveToRelative(-0.25f, -0.44f, -0.72f, -0.71f, -1.23f, -0.71f)
            lineTo(6.43f, 6.53f)
            lineToRelative(-0.35f, -1.75f)
            curveTo(5.82f, 3.44f, 4.67f, 2.5f, 3.31f, 2.5f)
            lineTo(2.0f, 2.5f)
            curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
            reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
            horizontalLineToRelative(1.31f)
            curveToRelative(0.45f, 0.0f, 0.84f, 0.31f, 0.92f, 0.76f)
            lineToRelative(1.88f, 9.38f)
            curveTo(7.41f, 16.14f, 8.76f, 17.25f, 10.3f, 17.25f)
            horizontalLineToRelative(8.95f)
            curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
            reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
            horizontalLineToRelative(-8.95f)
            curveToRelative(-0.51f, 0.0f, -0.96f, -0.37f, -1.04f, -0.88f)
            lineToRelative(-0.25f, -1.25f)
            horizontalLineToRelative(10.22f)
            curveToRelative(1.23f, 0.0f, 2.3f, -0.84f, 2.59f, -2.04f)
            lineToRelative(1.15f, -4.6f)
            curveToRelative(0.12f, -0.49f, 0.01f, -0.99f, -0.24f, -1.24f)
            close()
            moveTo(9.0f, 21.5f)
            curveToRelative(0.83f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
            reflectiveCurveToRelative(-0.67f, -1.5f, -1.5f, -1.5f)
            reflectiveCurveToRelative(-1.5f, 0.67f, -1.5f, 1.5f)
            reflectiveCurveToRelative(0.67f, 1.5f, 1.5f, 1.5f)
            close()
            moveTo(18.0f, 21.5f)
            curveToRelative(0.83f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
            reflectiveCurveToRelative(-0.67f, -1.5f, -1.5f, -1.5f)
            reflectiveCurveToRelative(-1.5f, 0.67f, -1.5f, 1.5f)
            reflectiveCurveToRelative(0.67f, 1.5f, 1.5f, 1.5f)
            close()
        }
    }

    private val house by lazy {
        phosphor("House") {
            moveTo(21.42f, 10.16f)
            lineTo(12.71f, 2.32f)
            curveToRelative(-0.41f, -0.37f, -1.02f, -0.37f, -1.42f, 0.0f)
            lineTo(2.58f, 10.16f)
            curveToRelative(-0.36f, 0.33f, -0.4f, 0.88f, -0.07f, 1.25f)
            curveToRelative(0.33f, 0.36f, 0.88f, 0.4f, 1.25f, 0.07f)
            lineTo(4.0f, 11.26f)
            lineTo(4.0f, 20.0f)
            curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(12.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            verticalLineToRelative(-8.74f)
            lineToRelative(0.24f, 0.22f)
            curveToRelative(0.18f, 0.16f, 0.41f, 0.24f, 0.63f, 0.24f)
            curveToRelative(0.23f, 0.0f, 0.47f, -0.09f, 0.64f, -0.27f)
            curveToRelative(0.35f, -0.37f, 0.31f, -0.92f, -0.09f, -1.29f)
            close()
            moveTo(14.0f, 20.0f)
            horizontalLineToRelative(-4.0f)
            verticalLineToRelative(-5.0f)
            curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
            horizontalLineToRelative(2.0f)
            curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
            verticalLineToRelative(5.0f)
            close()
        }
    }

    private val lightning by lazy {
        phosphor("Lightning") {
            moveTo(21.28f, 10.28f)
            curveToRelative(-0.2f, -0.22f, -0.49f, -0.34f, -0.79f, -0.34f)
            horizontalLineToRelative(-5.35f)
            lineToRelative(1.7f, -7.22f)
            curveToRelative(0.12f, -0.5f, -0.15f, -1.01f, -0.63f, -1.18f)
            curveToRelative(-0.47f, -0.18f, -1.01f, 0.04f, -1.25f, 0.49f)
            lineTo(4.16f, 13.0f)
            curveToRelative(-0.19f, 0.35f, -0.16f, 0.77f, 0.07f, 1.09f)
            curveToRelative(0.23f, 0.32f, 0.6f, 0.5f, 0.99f, 0.5f)
            horizontalLineToRelative(5.42f)
            lineToRelative(-1.87f, 7.51f)
            curveToRelative(-0.13f, 0.53f, 0.15f, 1.07f, 0.66f, 1.25f)
            curveToRelative(0.15f, 0.05f, 0.3f, 0.08f, 0.45f, 0.08f)
            curveToRelative(0.38f, 0.0f, 0.74f, -0.2f, 0.91f, -0.55f)
            lineToRelative(10.37f, -11.45f)
            curveToRelative(0.27f, -0.3f, 0.31f, -0.74f, 0.12f, -1.09f)
            close()
        }
    }

    private val drop by lazy {
        phosphor("Drop") {
            moveTo(12.0f, 2.15f)
            curveToRelative(-0.35f, 0.0f, -0.69f, 0.15f, -0.92f, 0.42f)
            lineTo(6.42f, 7.97f)
            curveTo(4.28f, 10.45f, 3.25f, 13.34f, 3.65f, 16.2f)
            curveToRelative(0.53f, 3.84f, 3.62f, 6.78f, 7.5f, 7.14f)
            curveToRelative(0.28f, 0.03f, 0.57f, 0.04f, 0.85f, 0.04f)
            curveToRelative(4.55f, 0.0f, 8.35f, -3.53f, 8.62f, -8.1f)
            curveToRelative(0.18f, -3.07f, -1.05f, -5.92f, -3.46f, -8.31f)
            lineToRelative(-4.24f, -4.4f)
            curveToRelative(-0.23f, -0.27f, -0.57f, -0.42f, -0.92f, -0.42f)
            close()
        }
    }

    private val deviceMobile by lazy {
        phosphor("DeviceMobile") {
            moveTo(17.0f, 2.0f)
            horizontalLineTo(7.0f)
            curveTo(5.34f, 2.0f, 4.0f, 3.34f, 4.0f, 5.0f)
            verticalLineToRelative(14.0f)
            curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
            horizontalLineToRelative(10.0f)
            curveToRelative(1.66f, 0.0f, 3.0f, -1.34f, 3.0f, -3.0f)
            verticalLineTo(5.0f)
            curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
            close()
            moveTo(12.0f, 19.5f)
            curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
            reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
            reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
            reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
            close()
            moveTo(18.0f, 15.0f)
            horizontalLineTo(6.0f)
            verticalLineTo(6.0f)
            horizontalLineToRelative(12.0f)
            verticalLineToRelative(9.0f)
            close()
        }
    }

    private val creditCard by lazy {
        phosphor("CreditCard") {
            moveTo(20.0f, 4.0f)
            lineTo(4.0f, 4.0f)
            curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
            verticalLineToRelative(10.0f)
            curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
            horizontalLineToRelative(16.0f)
            curveToRelative(1.66f, 0.0f, 3.0f, -1.34f, 3.0f, -3.0f)
            lineTo(23.0f, 7.0f)
            curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
            close()
            moveTo(3.0f, 7.0f)
            curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
            horizontalLineToRelative(16.0f)
            curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
            verticalLineToRelative(1.0f)
            lineTo(3.0f, 8.0f)
            lineTo(3.0f, 7.0f)
            close()
            moveTo(21.0f, 17.0f)
            curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
            lineTo(4.0f, 18.0f)
            curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
            verticalLineToRelative(-6.0f)
            horizontalLineToRelative(18.0f)
            verticalLineToRelative(6.0f)
            close()
        }
    }

    private val bank by lazy {
        phosphor("Bank") {
            moveTo(11.51f, 2.22f)
            curveToRelative(0.31f, -0.2f, 0.67f, -0.22f, 0.98f, 0.0f)
            lineToRelative(9.0f, 5.0f)
            curveToRelative(0.32f, 0.18f, 0.51f, 0.51f, 0.51f, 0.88f)
            curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
            horizontalLineToRelative(-1.0f)
            verticalLineToRelative(7.0f)
            horizontalLineToRelative(1.0f)
            curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
            verticalLineToRelative(2.0f)
            curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
            lineTo(3.0f, 20.1f)
            curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
            verticalLineToRelative(-2.0f)
            curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
            horizontalLineToRelative(1.0f)
            verticalLineToRelative(-7.0f)
            lineTo(3.0f, 9.1f)
            curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
            curveToRelative(0.0f, -0.37f, 0.19f, -0.7f, 0.51f, -0.88f)
            lineToRelative(9.0f, -5.0f)
            close()
            moveTo(6.0f, 9.1f)
            verticalLineToRelative(7.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(-7.0f)
            lineTo(6.0f, 9.1f)
            close()
            moveTo(11.0f, 9.1f)
            verticalLineToRelative(7.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(-7.0f)
            horizontalLineToRelative(-2.0f)
            close()
            moveTo(16.0f, 9.1f)
            verticalLineToRelative(7.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(-7.0f)
            horizontalLineToRelative(-2.0f)
            close()
        }
    }

    private val wallet by lazy {
        phosphor("Wallet") {
            moveTo(21.0f, 7.0f)
            lineTo(21.0f, 6.0f)
            curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
            lineTo(4.0f, 3.0f)
            curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
            verticalLineToRelative(12.0f)
            curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
            horizontalLineToRelative(16.0f)
            curveToRelative(1.66f, 0.0f, 3.0f, -1.34f, 3.0f, -3.0f)
            verticalLineToRelative(-7.0f)
            curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
            horizontalLineToRelative(-6.0f)
            curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
            reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
            horizontalLineToRelative(5.0f)
            close()
            moveTo(16.0f, 12.0f)
            horizontalLineToRelative(5.0f)
            verticalLineToRelative(3.0f)
            horizontalLineToRelative(-5.0f)
            curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
            reflectiveCurveToRelative(0.45f, -2.0f, 1.0f, -2.0f)
            close()
        }
    }

    private val car by lazy {
        phosphor("Car") {
            moveTo(22.92f, 10.12f)
            lineToRelative(-1.7f, -4.26f)
            curveTo(20.73f, 4.65f, 19.56f, 4.0f, 18.26f, 4.0f)
            lineTo(5.74f, 4.0f)
            curveTo(4.44f, 4.0f, 3.27f, 4.65f, 2.78f, 5.86f)
            lineTo(1.08f, 10.12f)
            curveTo(0.39f, 10.63f, 0.0f, 11.43f, 0.0f, 12.28f)
            lineTo(0.0f, 19.0f)
            curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(1.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            verticalLineToRelative(-1.0f)
            horizontalLineToRelative(14.0f)
            verticalLineToRelative(1.0f)
            curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(1.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            verticalLineToRelative(-6.72f)
            curveToRelative(0.0f, -0.85f, -0.39f, -1.65f, -1.08f, -2.16f)
            close()
            moveTo(4.63f, 6.6f)
            curveToRelative(0.16f, -0.4f, 0.55f, -0.6f, 1.11f, -0.6f)
            horizontalLineToRelative(12.52f)
            curveToRelative(0.56f, 0.0f, 0.95f, 0.2f, 1.11f, 0.6f)
            lineToRelative(1.36f, 3.4f)
            lineTo(3.27f, 10.0f)
            lineToRelative(1.36f, -3.4f)
            close()
            moveTo(21.0f, 16.0f)
            lineTo(3.0f, 16.0f)
            verticalLineToRelative(-4.0f)
            curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
            horizontalLineToRelative(16.0f)
            curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
            verticalLineToRelative(4.0f)
            close()
        }
    }

    private val airplane by lazy {
        phosphor("Airplane") {
            moveTo(21.41f, 13.09f)
            lineTo(14.0f, 9.6f)
            verticalLineTo(4.5f)
            curveTo(14.0f, 3.12f, 12.88f, 2.0f, 11.5f, 2.0f)
            reflectiveCurveTo(9.0f, 3.12f, 9.0f, 4.5f)
            verticalLineToRelative(5.1f)
            lineTo(1.59f, 13.09f)
            curveTo(1.23f, 13.25f, 1.0f, 13.61f, 1.0f, 14.0f)
            curveToRelative(0.0f, 0.69f, 0.67f, 1.2f, 1.33f, 0.99f)
            lineTo(9.0f, 12.86f)
            verticalLineTo(18.0f)
            lineToRelative(-2.29f, 1.72f)
            curveTo(6.44f, 19.92f, 6.27f, 20.28f, 6.32f, 20.65f)
            curveToRelative(0.08f, 0.66f, 0.68f, 1.13f, 1.34f, 1.03f)
            lineTo(11.5f, 21.0f)
            lineToRelative(3.84f, 0.68f)
            curveToRelative(0.66f, 0.1f, 1.26f, -0.37f, 1.34f, -1.03f)
            curveToRelative(0.05f, -0.37f, -0.12f, -0.73f, -0.39f, -0.93f)
            lineTo(14.0f, 18.0f)
            verticalLineToRelative(-5.14f)
            lineToRelative(6.67f, 2.13f)
            curveToRelative(0.66f, 0.21f, 1.33f, -0.3f, 1.33f, -0.99f)
            curveToRelative(0.0f, -0.39f, -0.23f, -0.75f, -0.59f, -0.91f)
            close()
        }
    }

    private val trendUp by lazy {
        phosphor("TrendUp") {
            moveTo(23.0f, 6.0f)
            verticalLineToRelative(6.0f)
            curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
            reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
            verticalLineTo(8.41f)
            lineToRelative(-7.29f, 7.3f)
            curveToRelative(-0.39f, 0.39f, -1.02f, 0.39f, -1.41f, 0.0f)
            lineTo(8.0f, 11.41f)
            lineToRelative(-5.29f, 5.3f)
            curveToRelative(-0.39f, 0.39f, -1.02f, 0.39f, -1.41f, 0.0f)
            reflectiveCurveToRelative(-0.39f, -1.02f, 0.0f, -1.41f)
            lineToRelative(6.0f, -6.0f)
            curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0.0f)
            lineTo(13.0f, 13.59f)
            lineToRelative(6.59f, -6.59f)
            horizontalLineTo(16.0f)
            curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
            reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
            horizontalLineToRelative(6.0f)
            curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
            close()
        }
    }

    private val trendDown by lazy {
        phosphor("TrendDown") {
            moveTo(23.0f, 18.0f)
            curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
            horizontalLineToRelative(-6.0f)
            curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
            reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
            horizontalLineToRelative(3.59f)
            lineToRelative(-6.59f, -6.59f)
            lineToRelative(-4.29f, 4.3f)
            curveToRelative(-0.39f, 0.39f, -1.02f, 0.39f, -1.41f, 0.0f)
            lineToRelative(-6.0f, -6.0f)
            curveToRelative(-0.39f, -0.39f, -0.39f, -1.02f, 0.0f, -1.41f)
            reflectiveCurveToRelative(1.02f, -0.39f, 1.41f, 0.0f)
            lineTo(8.0f, 12.59f)
            lineToRelative(4.29f, -4.3f)
            curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0.0f)
            lineTo(21.0f, 15.59f)
            verticalLineTo(12.0f)
            curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
            reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
            verticalLineToRelative(6.0f)
            close()
        }
    }

    private val receipt by lazy {
        phosphor("Receipt") {
            moveTo(20.0f, 2.0f)
            horizontalLineTo(4.0f)
            curveTo(2.9f, 2.0f, 2.0f, 2.9f, 2.0f, 4.0f)
            verticalLineToRelative(17.0f)
            curveToRelative(0.0f, 0.42f, 0.26f, 0.8f, 0.65f, 0.95f)
            curveToRelative(0.39f, 0.15f, 0.84f, 0.04f, 1.11f, -0.27f)
            lineTo(6.5f, 18.5f)
            lineToRelative(2.74f, 3.18f)
            curveToRelative(0.38f, 0.43f, 1.04f, 0.43f, 1.42f, 0.0f)
            lineTo(12.0f, 20.15f)
            lineToRelative(1.34f, 1.53f)
            curveToRelative(0.38f, 0.43f, 1.04f, 0.43f, 1.42f, 0.0f)
            lineToRelative(2.74f, -3.18f)
            lineToRelative(2.74f, 3.18f)
            curveTo(20.44f, 21.89f, 20.73f, 22.0f, 21.0f, 22.0f)
            curveToRelative(0.18f, 0.0f, 0.36f, -0.05f, 0.52f, -0.15f)
            curveToRelative(0.31f, -0.19f, 0.48f, -0.52f, 0.48f, -0.85f)
            verticalLineTo(4.0f)
            curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
            close()
            moveTo(17.0f, 8.0f)
            horizontalLineTo(7.0f)
            curveTo(6.45f, 8.0f, 6.0f, 7.55f, 6.0f, 7.0f)
            reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
            horizontalLineToRelative(10.0f)
            curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
            reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
            close()
            moveTo(17.0f, 12.0f)
            horizontalLineTo(7.0f)
            curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
            reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
            horizontalLineToRelative(10.0f)
            curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
            reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
            close()
        }
    }

    private val gift by lazy {
        phosphor("Gift") {
            moveTo(21.0f, 10.0f)
            horizontalLineTo(3.0f)
            curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
            verticalLineTo(6.0f)
            curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
            horizontalLineToRelative(3.14f)
            curveToRelative(-0.09f, -0.31f, -0.14f, -0.65f, -0.14f, -1.0f)
            curveToRelative(0.0f, -1.65f, 1.35f, -3.0f, 3.0f, -3.0f)
            curveToRelative(1.23f, 0.0f, 2.37f, 0.77f, 2.82f, 1.92f)
            curveTo(13.27f, 0.77f, 14.41f, 0.0f, 15.64f, 0.0f)
            curveToRelative(1.65f, 0.0f, 3.0f, 1.35f, 3.0f, 3.0f)
            curveToRelative(0.0f, 0.35f, -0.05f, 0.69f, -0.14f, 1.0f)
            horizontalLineTo(20.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
            verticalLineToRelative(3.0f)
            curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
            close()
            moveTo(11.0f, 12.0f)
            horizontalLineTo(4.0f)
            verticalLineToRelative(8.0f)
            curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(5.0f)
            verticalLineToRelative(-10.0f)
            close()
            moveTo(20.0f, 12.0f)
            horizontalLineToRelative(-7.0f)
            verticalLineToRelative(10.0f)
            horizontalLineToRelative(5.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            verticalLineToRelative(-8.0f)
            close()
        }
    }

    private val film by lazy {
        phosphor("Film") {
            moveTo(20.0f, 4.0f)
            horizontalLineTo(4.0f)
            curveTo(2.9f, 4.0f, 2.0f, 4.9f, 2.0f, 6.0f)
            verticalLineToRelative(12.0f)
            curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(16.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            verticalLineTo(6.0f)
            curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
            close()
            moveTo(6.0f, 18.0f)
            horizontalLineTo(4.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(6.0f, 14.0f)
            horizontalLineTo(4.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(6.0f, 10.0f)
            horizontalLineTo(4.0f)
            verticalLineTo(8.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(20.0f, 18.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(20.0f, 14.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(20.0f, 10.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineTo(8.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
        }
    }

    private val music by lazy {
        phosphor("MusicNote") {
            moveTo(21.0f, 3.0f)
            verticalLineToRelative(12.5f)
            curveToRelative(0.0f, -0.17f, -0.04f, -0.34f, -0.11f, -0.49f)
            curveToRelative(-0.52f, -1.17f, -1.78f, -2.01f, -3.29f, -2.01f)
            curveToRelative(-1.99f, 0.0f, -3.6f, 1.61f, -3.6f, 3.6f)
            reflectiveCurveToRelative(1.61f, 3.6f, 3.6f, 3.6f)
            curveToRelative(1.85f, 0.0f, 3.37f, -1.39f, 3.57f, -3.2f)
            lineTo(21.2f, 6.0f)
            lineToRelative(-10.0f, 2.5f)
            verticalLineToRelative(10.0f)
            curveToRelative(0.0f, -0.17f, -0.04f, -0.34f, -0.11f, -0.49f)
            curveToRelative(-0.52f, -1.17f, -1.78f, -2.01f, -3.29f, -2.01f)
            curveToRelative(-1.99f, 0.0f, -3.6f, 1.61f, -3.6f, 3.6f)
            reflectiveCurveToRelative(1.61f, 3.6f, 3.6f, 3.6f)
            curveToRelative(1.85f, 0.0f, 3.37f, -1.39f, 3.57f, -3.2f)
            lineTo(11.2f, 7.0f)
            curveToRelative(0.0f, -0.46f, 0.31f, -0.86f, 0.76f, -0.97f)
            lineToRelative(10.0f, -2.5f)
            curveToRelative(0.59f, -0.15f, 1.17f, 0.29f, 1.17f, 0.89f)
            close()
        }
    }

    private val gamepad by lazy {
        phosphor("Gamepad") {
            moveTo(21.43f, 6.1f)
            curveToRelative(-0.84f, -0.68f, -1.97f, -0.85f, -2.97f, -0.45f)
            lineToRelative(-2.68f, 1.07f)
            curveTo(14.52f, 6.26f, 13.29f, 6.0f, 12.0f, 6.0f)
            reflectiveCurveToRelative(-2.52f, 0.26f, -3.78f, 0.72f)
            lineTo(5.54f, 5.65f)
            curveTo(4.54f, 5.25f, 3.41f, 5.42f, 2.57f, 6.1f)
            curveTo(1.47f, 6.99f, 0.92f, 8.44f, 1.13f, 9.87f)
            lineToRelative(1.2f, 8.0f)
            curveToRelative(0.24f, 1.61f, 1.48f, 2.87f, 3.07f, 3.1f)
            curveToRelative(1.4f, 0.2f, 2.76f, -0.54f, 3.39f, -1.8f)
            lineTo(10.4f, 16.0f)
            horizontalLineToRelative(3.2f)
            lineToRelative(1.61f, 3.17f)
            curveToRelative(0.63f, 1.26f, 1.99f, 2.0f, 3.39f, 1.8f)
            curveToRelative(1.59f, -0.23f, 2.83f, -1.49f, 3.07f, -3.1f)
            lineToRelative(1.2f, -8.0f)
            curveToRelative(0.21f, -1.43f, -0.34f, -2.88f, -1.44f, -3.77f)
            close()
            moveTo(8.0f, 12.0f)
            horizontalLineTo(7.0f)
            verticalLineToRelative(1.0f)
            curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
            reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
            verticalLineToRelative(-1.0f)
            horizontalLineTo(4.0f)
            curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
            reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
            horizontalLineToRelative(1.0f)
            verticalLineTo(9.0f)
            curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
            reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
            verticalLineToRelative(1.0f)
            horizontalLineToRelative(1.0f)
            curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
            reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
            close()
            moveTo(16.5f, 13.0f)
            curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
            reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
            reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
            reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
            close()
            moveTo(19.0f, 10.5f)
            curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
            reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
            reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
            reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
            close()
        }
    }

    private val bookOpen by lazy {
        phosphor("BookOpen") {
            moveTo(21.41f, 3.09f)
            curveToRelative(-0.38f, -0.21f, -0.84f, -0.16f, -1.17f, 0.12f)
            lineTo(13.0f, 9.42f)
            lineTo(5.76f, 3.21f)
            curveTo(5.43f, 2.93f, 4.97f, 2.88f, 4.59f, 3.09f)
            curveTo(4.21f, 3.3f, 3.97f, 3.71f, 4.0f, 4.14f)
            lineToRelative(1.0f, 14.0f)
            curveToRelative(0.04f, 0.52f, 0.42f, 0.94f, 0.94f, 1.0f)
            lineTo(12.5f, 20.0f)
            lineToRelative(6.56f, -0.86f)
            curveToRelative(0.52f, -0.06f, 0.9f, -0.48f, 0.94f, -1.0f)
            lineToRelative(1.0f, -14.0f)
            curveToRelative(0.03f, -0.43f, -0.21f, -0.84f, -0.59f, -1.05f)
            close()
        }
    }

    private val tag by lazy {
        phosphor("Tag") {
            moveTo(22.41f, 11.59f)
            lineTo(13.41f, 2.59f)
            curveTo(13.04f, 2.21f, 12.53f, 2.0f, 12.0f, 2.0f)
            horizontalLineTo(4.0f)
            curveTo(2.9f, 2.0f, 2.0f, 2.9f, 2.0f, 4.0f)
            verticalLineToRelative(8.0f)
            curveToRelative(0.0f, 0.53f, 0.21f, 1.04f, 0.59f, 1.41f)
            lineToRelative(9.0f, 9.0f)
            curveToRelative(0.78f, 0.78f, 2.05f, 0.78f, 2.83f, 0.0f)
            lineToRelative(8.0f, -8.0f)
            curveToRelative(0.78f, -0.78f, 0.78f, -2.05f, -0.01f, -2.82f)
            close()
            moveTo(6.5f, 8.0f)
            curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
            reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
            reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
            reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
            close()
        }
    }

    fun resolve(key: String): ImageVector? {
        return when (key) {
            "restaurant", "food", "dining", "utensils" -> forkKnife
            "local_cafe", "cafe", "coffee", "tea" -> coffee
            "shopping_cart", "shopping", "cart", "supermarket" -> shoppingCart
            "shopping_bag", "bag", "retail", "order" -> shoppingBag
            "home", "rent", "housing", "mortgage", "house" -> house
            "bolt", "electricity", "power", "utilities" -> lightning
            "water_drop", "water" -> drop
            "phone_android", "mobile", "recharge", "telecom" -> deviceMobile
            "credit_card", "card", "debit_card", "emi" -> creditCard
            "payments", "salary", "paycheck", "cashflow" -> bank
            "account_balance", "bank", "bank_account" -> bank
            "account_balance_wallet", "wallet", "cash", "prepaid_wallet" -> wallet
            "directions_car", "car", "vehicle", "transport" -> car
            "flight", "travel", "airfare", "airplane" -> airplane
            "trending_up", "investment", "stocks", "mutual_fund", "sip" -> trendUp
            "trending_down", "loss", "loan" -> trendDown
            "receipt_long", "bills", "invoice", "subscription" -> receipt
            "card_giftcard", "gift", "bonus", "redeem" -> gift
            "movie", "theaters", "cinema" -> film
            "music_note", "music", "spotify" -> music
            "sports_esports", "gaming", "game", "console" -> gamepad
            "school", "education", "courses", "tuition", "menu_book", "books" -> bookOpen
            "category", "tag" -> tag
            else -> null // Graceful fallback to Material
        }
    }
}
