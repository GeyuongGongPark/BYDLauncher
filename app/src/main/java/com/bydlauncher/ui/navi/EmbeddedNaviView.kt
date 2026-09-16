package com.bydlauncher.ui.navi

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch

/**
 * VirtualDisplay + ADB loopback(am start --display)으로 네비 앱 임베딩.
 * TextureView 사용 → SidePanel과 z-order 충돌 없음.
 * 터치는 input -d <displayId> tap x y 로 주입.
 */
@Composable
fun EmbeddedNaviView(
    packageName: String,
    modifier: Modifier = Modifier,
    onEmbeddingFailed: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var virtualDisplay by remember { mutableStateOf<VirtualDisplay?>(null) }
    var currentDisplayId by remember { mutableIntStateOf(-1) }
    var vdWidth by remember { mutableIntStateOf(0) }
    var vdHeight by remember { mutableIntStateOf(0) }

    DisposableEffect(packageName) {
        onDispose {
            virtualDisplay?.release()
            virtualDisplay = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextureView(ctx).apply {
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) {
                        val surface = Surface(st)
                        val actualW = w.coerceAtLeast(1)
                        val actualH = h.coerceAtLeast(1)
                        val vd = createVirtualDisplay(
                            context = ctx,
                            surface = surface,
                            width = actualW,
                            height = actualH,
                            density = ctx.resources.displayMetrics.densityDpi,
                        )
                        if (vd == null) {
                            surface.release()
                            onEmbeddingFailed()
                            return
                        }
                        virtualDisplay = vd
                        currentDisplayId = vd.display.displayId
                        vdWidth = actualW
                        vdHeight = actualH

                        scope.launch {
                            val result = AdbNaviLauncher.launch(
                                context = context,
                                packageName = packageName,
                                displayId = vd.display.displayId,
                            )
                            if (result.isFailure) {
                                vd.release()
                                virtualDisplay = null
                                onEmbeddingFailed()
                            }
                        }
                    }

                    override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) {
                        val newW = w.coerceAtLeast(1)
                        val newH = h.coerceAtLeast(1)
                        virtualDisplay?.resize(newW, newH, ctx.resources.displayMetrics.densityDpi)
                        vdWidth = newW
                        vdHeight = newH
                    }

                    override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                        virtualDisplay?.release()
                        virtualDisplay = null
                        return true
                    }

                    override fun onSurfaceTextureUpdated(st: SurfaceTexture) = Unit
                }

                setOnTouchListener { v, event ->
                    val id = currentDisplayId
                    val w = vdWidth
                    val h = vdHeight
                    if (id >= 0 && w > 0 && h > 0 && v.width > 0 && v.height > 0) {
                        // TextureView 크기 → VirtualDisplay 크기로 좌표 변환
                        val x = (event.x * w / v.width).toInt()
                        val y = (event.y * h / v.height).toInt()
                        scope.launch {
                            AdbNaviLauncher.injectTouch(context, id, event.action, x, y)
                        }
                    }
                    true
                }
            }
        },
    )
}

private fun createVirtualDisplay(
    context: Context,
    surface: Surface,
    width: Int,
    height: Int,
    density: Int,
): VirtualDisplay? = runCatching {
    val dm = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    dm.createVirtualDisplay(
        "BYDNaviDisplay",
        width,
        height,
        density,
        surface,
        DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC or
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY,
    )
}.getOrNull()
