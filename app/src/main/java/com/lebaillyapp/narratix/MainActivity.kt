package com.lebaillyapp.narratix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.lebaillyapp.narratix.assistant.ui.screen.CinematicDemoScreen
import com.lebaillyapp.narratix.assistant.ui.screen.DemoScreen1
import com.lebaillyapp.narratix.assistant.ui.screen.ParallaxScreen
import com.lebaillyapp.narratix.gyrosensor.SensorCrosshairDebugOverkill
import com.lebaillyapp.narratix.gyrosensor.SensorViewModel
import com.lebaillyapp.narratix.ui.theme.NarratixTheme
import kotlin.getValue

class MainActivity : ComponentActivity() {

    private val sensorViewModel: SensorViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()
        setContent {
            NarratixTheme {
                //DemoScreen1()

              //  SensorCrosshairDebugOverkill(viewModel = sensorViewModel)


                ParallaxScreen(viewModel = sensorViewModel )

              //  CinematicDemoScreen()
            }
        }
    }
}
