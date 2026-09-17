package com.bydlauncher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bydlauncher.domain.drive.DriveEventType
import kotlinx.coroutines.delay

/**
 * 급가속/급제동 시 화면 테두리를 순간 플래시.
 * flashType이 non-null이면 플래시 후 onFlashDone 호출.
 */
@Composable
fun DriveFlashOverlay(
    flashType: DriveEventType?,
    onFlashDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    val flashColor = when (flashType) {
        DriveEventType.HARSH_ACCEL -> Color(0xFFFF6F00) // 주황
        DriveEventType.HARSH_BRAKE -> Color(0xFFD32F2F) // 빨강
        else -> Color.Transparent
    }

    LaunchedEffect(flashType) {
        if (flashType != null && flashType != DriveEventType.REGEN) {
            visible = true
            delay(600)
            visible = false
            onFlashDone()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(6.dp, flashColor),
            )
        }
    }
}
