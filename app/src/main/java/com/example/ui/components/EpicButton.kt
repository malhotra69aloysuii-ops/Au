package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EpicBlue
import com.example.ui.theme.EpicBluePressed
import com.example.ui.theme.EpicBorderGray
import com.example.ui.theme.EpicButtonPrimaryShape
import com.example.ui.theme.EpicButtonSecondaryShape
import com.example.ui.theme.EpicElevated
import com.example.ui.theme.EpicError
import com.example.ui.theme.EpicTextPrimary
import com.example.ui.theme.EpicWhite
import com.example.ui.theme.InterFontFamily

enum class EpicButtonStyle {
    PRIMARY,
    SECONDARY,
    DANGER,
    OUTLINE
}

@Composable
fun EpicButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: EpicButtonStyle = EpicButtonStyle.PRIMARY,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    testTag: String = "epic_button"
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 900f),
        label = "btn_press_scale"
    )

    val (shape, containerColor, contentColor, border) = when (style) {
        EpicButtonStyle.PRIMARY -> Tuple4(
            EpicButtonPrimaryShape,
            EpicBlue,
            EpicWhite,
            null
        )
        EpicButtonStyle.SECONDARY -> Tuple4(
            EpicButtonSecondaryShape,
            EpicElevated,
            EpicTextPrimary,
            BorderStroke(1.dp, EpicBorderGray)
        )
        EpicButtonStyle.DANGER -> Tuple4(
            EpicButtonSecondaryShape,
            EpicError,
            EpicWhite,
            null
        )
        EpicButtonStyle.OUTLINE -> Tuple4(
            EpicButtonSecondaryShape,
            Color.Transparent,
            EpicBlue,
            BorderStroke(1.dp, EpicBlue)
        )
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(enabled) {
                if (enabled) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        waitForUpOrCancellation()
                        isPressed = false
                    }
                }
            }
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = EpicElevated.copy(alpha = 0.5f),
                disabledContentColor = Color.Gray
            ),
            border = border,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
            modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .fillMaxWidth()
                .testTag(testTag)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text.uppercase(),
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 0.08.sp
                )
            }
        }
    }
}

private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
