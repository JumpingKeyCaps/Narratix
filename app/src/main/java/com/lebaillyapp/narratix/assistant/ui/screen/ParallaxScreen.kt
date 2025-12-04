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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lebaillyapp.narratix.R
import com.lebaillyapp.narratix.assistant.ui.composition.AssistantOverlayAlt
import com.lebaillyapp.narratix.assistant.ui.viewmodel.AssistantViewModel
import com.lebaillyapp.narratix.assistant.ui.viewmodel.factory.AssistantViewModelFactory
import com.lebaillyapp.narratix.gyrosensor.SensorViewModel
import kotlin.math.roundToInt

@Composable
fun ParallaxScreen(
    viewModel: SensorViewModel,
    modifier: Modifier = Modifier,
    demoScriptId: String = "DEMO_2"
) {
    val context = LocalContext.current
    val tilt by viewModel.tilt.collectAsState()

    // Assistant ViewModel
    val assistantViewModel: AssistantViewModel = viewModel(
        factory = AssistantViewModelFactory()
    )
    // Collect AssistantState
    val assistantState by assistantViewModel.assistantState.collectAsState()


    // lance le script de dialogue au montage
    LaunchedEffect(demoScriptId) {
        assistantViewModel.startDialogue(context, demoScriptId)
    }

    // OFFSET HUD transmises par l’overlay
    var hudOffset by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val hudOffsetPx = with(density) { hudOffset.roundToPx() }

    // Largeur de l'écran en Dp (basé sur les pixels réels de l'appareil)
    val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels.dp

    val layers = listOf(
        Layer(R.drawable.sky, 0.4f, 1.05f, Modifier.fillMaxSize()),
        Layer(R.drawable.forest, 0.6f, 1.10f, Modifier.fillMaxWidth()),
        Layer(R.drawable.caravan, 0.9f, 1.15f, Modifier.fillMaxWidth())
    )

    Box(modifier = modifier.fillMaxSize()) {

        // *** PARALLAXE AVEC SÉCURITÉ ANTI-BORDURE (POINT 1) ***
        layers.forEach { layer ->

            // 1. Calculer la marge de manœuvre disponible en DP
            // Note: Nous utilisons le ratio pour calculer l'espace disponible
            val maxTranslationRatio = (layer.zoom - 1f) / 2f
            val maxTranslationDp = screenWidth * maxTranslationRatio

            // 2. Calculer l'offset souhaité
            val desiredOffset = tilt.x * layer.speed * 200f

            // 3. Limiter l'offset à la marge disponible
            val constrainedOffsetDp = desiredOffset.coerceIn(
                minimumValue = -maxTranslationDp.value, // Limite négative
                maximumValue = maxTranslationDp.value    // Limite positive
            ).dp.value // On récupère la valeur Float en DP

            val offsetX by animateFloatAsState(
                targetValue = constrainedOffsetDp,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "parallaxOffsetX"
            )

            Image(
                painter = painterResource(id = layer.resId),
                contentDescription = null,
                modifier = layer.mod
                    .graphicsLayer(
                        translationX = offsetX, // Utilisation de l'offset contraint
                        scaleX = layer.zoom,
                        scaleY = layer.zoom
                    )
                    .align(Alignment.BottomCenter),
                contentScale = ContentScale.Crop
            )
        }

        // *** OVERLAY AVEC PASSAGE DE LA TRANSLATION  ***
        val overlayOffsetX by animateFloatAsState(
            targetValue = tilt.x * -200f,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "overlayOffsetX"
        )

        AssistantOverlayAlt(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset { IntOffset(0, 0 - hudOffsetPx) },
            state = assistantState,
            onClose = { assistantViewModel.closeAssistant() },
            onHudOffsetCallback = { dp -> hudOffset = dp },
            onAvatarChangeInVm = { id -> assistantViewModel.updateAvatar(id) },
            tiltTranslationX = overlayOffsetX
        )
    }
}

data class Layer(
    val resId: Int,
    val speed: Float,
    val zoom: Float = 1f,
    val mod: Modifier = Modifier
)