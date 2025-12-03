package com.lebaillyapp.narratix.assistant.decoration


import android.graphics.RuntimeShader
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import com.lebaillyapp.narratix.R
import kotlinx.coroutines.isActive
import kotlin.math.PI

@Composable
fun FlowRibbonShader(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFCCCCCC),
    distortionStrength: Float = 1f,
    highlightIntensity: Float = 0.5f,
    gamma: Float = 0.85f,
    horizontalMix: Float = 0.5f,
    microStrength: Float = 0.3f,
    autoAnimate: Boolean = true,
    scaleOverride: Float = 1f,
    rotationOverride: Float = 1f,
    timeSpeedOverride: Float = 1f,
    animSpeedMultiplicator: Float = 1f
) {
    val context = LocalContext.current
    val shaderSource = remember {
        context.resources.openRawResource(R.raw.flow_ribbon_shader).bufferedReader().use { it.readText() }
    }
    val shader = remember(shaderSource) { RuntimeShader(shaderSource) }

    var time by remember { mutableStateOf(0f) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    // Animation du temps optimisée
    LaunchedEffect(autoAnimate) {
        val start = System.nanoTime()
        while (isActive) {
            withFrameMillis {
                if (autoAnimate) {
                    time = ((System.nanoTime() - start) / 1_000_000_000f) % 1000f
                }
            }
        }
    }

    Canvas(
        modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
    ) {
        val w = canvasSize.width
        val h = canvasSize.height
        if (w == 0f || h == 0f) return@Canvas

        // Correction du zoom : animation modifie offset/rotation seulement
        val animatedRotation = if (autoAnimate) (time * 0.03) % (2 * PI.toFloat()) else rotationOverride
        val animatedScale = scaleOverride  // ne plus multiplier par sin(time) pour éviter zoom

        shader.apply {
            setColorUniform("u_Color", color.toArgb())
            setFloatUniform("u_Time", time * animSpeedMultiplicator)
            setFloatUniform("u_DistortionStrength", distortionStrength)
            setFloatUniform("u_TimeSpeed", timeSpeedOverride)
            setFloatUniform("u_HighlightIntensity", highlightIntensity)
            setFloatUniform("u_Gamma", gamma)
            setFloatUniform("u_HorizontalMix", horizontalMix)
            setFloatUniform("u_MicroStrength", microStrength)
            setFloatUniform("u_Scale", animatedScale)
            setFloatUniform("u_Rotation", animatedRotation.toFloat())
            setFloatUniform("u_Resolution", floatArrayOf(w, h))
        }

        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply { this.shader = shader }
            drawRect(0f, 0f, w, h, paint)
        }
    }
}