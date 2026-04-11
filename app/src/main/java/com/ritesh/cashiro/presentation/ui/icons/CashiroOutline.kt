package com.ritesh.cashiro.presentation.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Iconax.CashiroOutline: ImageVector
    get() {
        if (_CashiroOutline != null) {
            return _CashiroOutline!!
        }
        _CashiroOutline = ImageVector.Builder(
            name = "CashiroOutline",
            defaultWidth = 192.dp,
            defaultHeight = 192.dp,
            viewportWidth = 192f,
            viewportHeight = 192f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                fillAlpha = 0f,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 8.993004f,
                strokeLineCap = StrokeCap.Square,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(129.8f, 96f)
                lineTo(166.99f, 126.99f)
                arcTo(74.37f, 74.37f, -0f, isMoreThanHalf = true, isPositiveArc = true, 166.99f, 65.01f)
                lineTo(129.8f, 96f)
                close()
                moveTo(98.82f, 126.99f)
                lineTo(129.8f, 96f)
                lineTo(98.82f, 65.01f)
                lineTo(67.83f, 96f)
                lineTo(98.82f, 126.99f)
                close()
            }
        }.build()

        return _CashiroOutline!!
    }

@Suppress("ObjectPropertyName")
private var _CashiroOutline: ImageVector? = null
