package com.lebaillyapp.narratix.assistant.ui.screen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import com.lebaillyapp.narratix.R
import com.lebaillyapp.narratix.gyrosensor.SensorViewModel
import kotlin.math.roundToInt

@Composable
fun ParallaxScreen(
    viewModel: SensorViewModel,
    modifier: Modifier = Modifier
) {
    val tilt by viewModel.tilt.collectAsState()

    // Liste de tes layers avec vitesse et facteur de zoom
    val layers = listOf(
        Layer(R.drawable.sky, 0.2f, 1.2f, Modifier.fillMaxSize()), // zoom + scaleX/scaleY
        Layer(R.drawable.forest, 0.4f, 1.25f, Modifier.fillMaxWidth()),
        Layer(R.drawable.caravan, 0.6f, 1.2f, Modifier.fillMaxWidth())  // pas de zoom pour le front
    )

    Box(modifier = modifier.fillMaxSize()) {
        layers.forEach { layer ->
            val offsetX by animateFloatAsState(
                targetValue = tilt.x * layer.speed * 200f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            )

            Image(
                painter = painterResource(id = layer.resId),
                contentDescription = null,
                modifier = layer.mod
                    .graphicsLayer(
                        translationX = offsetX,
                        scaleX = layer.zoom,
                        scaleY = layer.zoom
                    )
                    .align(Alignment.BottomCenter),
                contentScale = ContentScale.Crop
            )
        }
    }
}

data class Layer(
    val resId: Int,
    val speed: Float,
    val zoom: Float = 1f,
    val mod: Modifier = Modifier
)