package com.lebaillyapp.narratix.assistant.decoration

import android.graphics.Paint
import android.graphics.RuntimeShader
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePaint
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.lebaillyapp.narratix.R
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun UltimateChromeLiquidDemo() {
    var showControls by remember { mutableStateOf(false) }
    var isAnimating by remember { mutableStateOf(true) }

    // Paramètres
    var colorHue by remember { mutableStateOf(335.46f) }
    var distortionStrength by remember { mutableStateOf(3.18f) }
    var timeSpeed by remember { mutableStateOf(1.24f) }
    var highlightIntensity by remember { mutableStateOf(0.51f) }
    var gamma by remember { mutableStateOf(1.82f) }
    var horizontalMix by remember { mutableStateOf(1.52f) }
    var microStrength by remember { mutableStateOf(0.0f) }
    var scale by remember { mutableStateOf(2.28f) }
    var rotation by remember { mutableStateOf(0f) }

    val color = Color.hsv(colorHue, 1f, 1f)

    Box(Modifier.fillMaxSize()) {

        UltimateChromeLiquidLive(
            modifier = Modifier.fillMaxSize(),
            color = color,
            distortionStrength = distortionStrength,
            highlightIntensity = highlightIntensity,
            gamma = gamma,
            horizontalMix = horizontalMix,
            microStrength = microStrength,
            autoAnimate = isAnimating,
            scaleOverride = scale,
            rotationOverride = rotation,
            timeSpeedOverride = timeSpeed
        )

        if (showControls) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(8.dp)) {
                    ParameterSlider("Hue", colorHue, 0f, 360f) { colorHue = it }
                    ParameterSlider("Distortion", distortionStrength, 0f, 10f) { distortionStrength = it }
                    ParameterSlider("Time Speed", timeSpeed, 0f, 5f) { timeSpeed = it }
                    ParameterSlider("Highlight", highlightIntensity, 0f, 1f) { highlightIntensity = it }
                    ParameterSlider("Gamma", gamma, 0.1f, 2f) { gamma = it }
                    ParameterSlider("Horizontal Mix", horizontalMix, 0f, 10f) { horizontalMix = it }
                    ParameterSlider("Micro Strength", microStrength, 0f, 1f) { microStrength = it }
                    ParameterSlider("Scale", scale, 0.1f, 10f) { scale = it }
                    ParameterSlider("Rotation", rotation, 0f, 10 * PI.toFloat()) { rotation = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { isAnimating = !isAnimating }) {
                        Text(if (isAnimating) "Stop Animation" else "Resume Animation")
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showControls = !showControls },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text(if (showControls) "▲" else "▼")
        }
    }
}

@Composable
fun ParameterSlider(label: String, value: Float, min: Float, max: Float, onValueChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("$label: ${"%.2f".format(value)}", color = Color.White)
        Slider(value = value, onValueChange = onValueChange, valueRange = min..max)
    }
}

@Composable
fun UltimateChromeLiquidLive(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFCCCCCC),
    distortionStrength: Float = 1f,
    highlightIntensity: Float = 0.5f,
    gamma: Float = 0.85f,
    horizontalMix: Float = 0.5f,
    microStrength: Float = 0.3f,
    autoAnimate: Boolean = true,
    scaleOverride: Float = 1f,
    rotationOverride: Float = 0f,
    timeSpeedOverride: Float = 1f
) {
    val context = LocalContext.current
    val shaderSource = remember {
        context.resources.openRawResource(R.raw.metalic).bufferedReader().use { it.readText() }
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
        val animatedRotation = if (autoAnimate) (time * 0.3f) % (2 * PI.toFloat()) else rotationOverride
        val animatedScale = scaleOverride  // ne plus multiplier par sin(time) pour éviter zoom

        shader.apply {
            setColorUniform("u_Color", color.toArgb())
            setFloatUniform("u_Time", time)
            setFloatUniform("u_DistortionStrength", distortionStrength)
            setFloatUniform("u_TimeSpeed", timeSpeedOverride)
            setFloatUniform("u_HighlightIntensity", highlightIntensity)
            setFloatUniform("u_Gamma", gamma)
            setFloatUniform("u_HorizontalMix", horizontalMix)
            setFloatUniform("u_MicroStrength", microStrength)
            setFloatUniform("u_Scale", animatedScale)
            setFloatUniform("u_Rotation", animatedRotation)
            setFloatUniform("u_Resolution", floatArrayOf(w, h))
        }

        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply { this.shader = shader }
            drawRect(0f, 0f, w, h, paint)
        }
    }
}
