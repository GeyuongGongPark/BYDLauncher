package com.bydlauncher.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.bydlauncher.ui.theme.AccentCyanDim

@Composable
fun SetDefaultHomeButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = AccentCyanDim),
    ) {
        Text("기본 홈 앱으로 설정", fontSize = 13.sp)
    }
}
