package com.lebaillyapp.narratix.gyrosensor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// Ajoutez la fonction d'extension de formatage (si elle n'est pas déjà là)
fun Float.format(decimals: Int) = "%.${decimals}f".format(this)
fun Double.format(decimals: Int) = "%.${decimals}f".format(this)


@Composable
fun SensorCrosshairDebug(viewModel: SensorViewModel) {

    val tilt by viewModel.tilt.collectAsState()

    val sensitivity = 350f
    val offsetPx = Offset(
        tilt.x * sensitivity,
        tilt.y * sensitivity
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E12)),
        contentAlignment = Alignment.Center
    ) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)

            // ---- GRILLE ----
            val step = 80f
            for (x in 0..(w / step).toInt()) {
                drawLine(
                    color = Color(0x3318FFFF),
                    start = Offset(x * step, 0f),
                    end = Offset(x * step, h),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(h / step).toInt()) {
                drawLine(
                    color = Color(0x3318FFFF),
                    start = Offset(0f, y * step),
                    end = Offset(w, y * step),
                    strokeWidth = 1f
                )
            }

            // ---- CERCLE DE MAGNITUDE ----
            val mag = offsetPx.getDistance()
            drawCircle(
                color = Color(0x4418FFFF),
                radius = mag,
                center = center,
                style = Stroke(3f)
            )

            // ---- RÉPÈRE FIXE AU CENTRE ----
            drawLine(
                Color.Cyan,
                Offset(center.x - 60, center.y),
                Offset(center.x + 60, center.y),
                strokeWidth = 3f
            )
            drawLine(
                Color.Cyan,
                Offset(center.x, center.y - 60),
                Offset(center.x, center.y + 60),
                strokeWidth = 3f
            )

            // ---- CROSSHAIR MOBILE ----
            val moving = center + offsetPx

            // croix mobile
            drawLine(
                Color.White,
                Offset(moving.x - 45, moving.y),
                Offset(moving.x + 45, moving.y),
                strokeWidth = 4f
            )
            drawLine(
                Color.White,
                Offset(moving.x, moving.y - 45),
                Offset(moving.x, moving.y + 45),
                strokeWidth = 4f
            )

            // point central
            drawCircle(
                color = Color.Red,
                radius = 6f,
                center = moving
            )

            // lien depuis le centre
            drawLine(
                color = Color(0x55FFAA00),
                start = center,
                end = moving,
                strokeWidth = 3f
            )
        }

        // ---- TEXTE OVERLAY ----
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(24.dp)
        ) {

            val degX = Math.toDegrees(tilt.x.toDouble())
            val degY = Math.toDegrees(tilt.y.toDouble())

            Text("Tilt X (Roll): ${tilt.x.format(3)} rad | ${degX.format(1)}°", color = Color.Cyan)
            Text("Tilt Y (Pitch): ${tilt.y.format(3)} rad | ${degY.format(1)}°", color = Color.Cyan)



        }
    }
}


@Composable
fun SensorCrosshairDebugOverkill(viewModel: SensorViewModel) {

    val tilt by viewModel.tilt.collectAsState()

    val sensitivity = 300f
    val offsetPx = Offset(
        tilt.x * sensitivity,
        tilt.y * sensitivity
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E12)),
        contentAlignment = Alignment.Center
    ) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)

            // ---- GRILLE ----
            val step = 80f
            for (x in 0..(w / step).toInt()) {
                drawLine(Color(0x3318FFFF), Offset(x * step, 0f), Offset(x * step, h), 1f)
            }
            for (y in 0..(h / step).toInt()) {
                drawLine(Color(0x3318FFFF), Offset(0f, y * step), Offset(w, y * step), 1f)
            }

            // ---- CERCLE DE MAGNITUDE ----
            val mag = offsetPx.getDistance()
            drawCircle(Color(0x4418FFFF), mag, center, style = Stroke(3f))

            // ---- RÉPÈRE FIXE AU CENTRE ----
            drawLine(Color.Cyan, Offset(center.x - 60, center.y), Offset(center.x + 60, center.y), 3f)
            drawLine(Color.Cyan, Offset(center.x, center.y - 60), Offset(center.x, center.y + 60), 3f)

            // ---- ARCS D’AMPLITUDE MAX ----
            val maxAnglePx = 1.5f * sensitivity
            drawArc(
                color = Color(0xFFFF1744),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - maxAnglePx, center.y - maxAnglePx),
                size = androidx.compose.ui.geometry.Size(maxAnglePx * 2, maxAnglePx * 2),
                style = Stroke(2f)
            )

            // ---- CROSSHAIR MOBILE ----
            val moving = center + offsetPx

            drawLine(Color.White, Offset(moving.x - 45, moving.y), Offset(moving.x + 45, moving.y), 4f)
            drawLine(Color.White, Offset(moving.x, moving.y - 45), Offset(moving.x, moving.y + 45), 4f)
            drawCircle(Color.Red, 6f, moving)
            drawLine(Color(0xC9FFAA00), center, moving, 3f)

            // ---- HUD STYLE JAUGES ----
            val arcStep = 15
            for (i in 0 until 360 step arcStep) {
                val radius = maxAnglePx + 10
                val rad = Math.toRadians(i.toDouble())
                val start = Offset(center.x + (radius - 5) * cos(rad).toFloat(), center.y + (radius - 5) * sin(rad).toFloat())
                val end = Offset(center.x + radius * cos(rad).toFloat(), center.y + radius * sin(rad).toFloat())
                drawLine(Color(0xFFC6FF00), start, end, 1.5f)
            }
        }

        // ---- TEXTE OVERLAY ----
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(24.dp)
        ) {

            val degX = Math.toDegrees(tilt.x.toDouble())
            val degY = Math.toDegrees(tilt.y.toDouble())
            val mag = offsetPx.getDistance()

            Text("Tilt X (Roll): ${tilt.x.format(3)} rad | ${degX.format(1)}°", color = Color.Cyan)
            Text("Tilt Y (Pitch): ${tilt.y.format(3)} rad | ${degY.format(1)}°", color = Color.Cyan)
            Text("Magnitude: ${mag.format(1)} px", color = Color.Cyan)
            Text("Max Angle: ±1.5 rad", color = Color.Gray)
        }
    }
}