package com.lebaillyapp.narratix.gyrosensor


import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.AXIS_MINUS_X
import android.hardware.SensorManager.AXIS_X
import android.hardware.SensorManager.AXIS_Y
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@SuppressLint("ServiceCast")
class SensorViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    // Utilise le Context de l'Application pour les services système
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _tilt = MutableStateFlow(Offset.Zero)
    val tilt: StateFlow<Offset> = _tilt

    // Variables pour le filtre passe-bas (Low-Pass Filter)
    private var lastX = 0f
    private var lastY = 0f
    private val SMOOTHING = 0.2f // Coefficient de lissage

    init {
        // Enregistrement avec SENSOR_DELAY_GAME
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
        } else {
            Log.e("ReactiveBox", "Capteur de Rotation Vector non disponible sur cet appareil.")
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        val matrix = FloatArray(9)
        val remapped = FloatArray(9)

        // 1. Conversion du vecteur de rotation en matrice de rotation
        SensorManager.getRotationMatrixFromVector(matrix, event.values)

        // 2. Remappage du système de coordonnées
        // AXIS_Y et AXIS_MINUS_X pour contourner le Gimbal Lock et stabiliser le Roll/Pitch.
        SensorManager.remapCoordinateSystem(matrix, AXIS_Y, AXIS_MINUS_X, remapped)

        val orientation = FloatArray(3)
        // 3. Conversion de la matrice remappée en angles (Roll, Pitch, Yaw)
        SensorManager.getOrientation(remapped, orientation)

        // orientation[1] = Pitch (inclinaison avant/arrière)
        // orientation[2] = Roll (inclinaison latérale)

        // Le mouvement Horizontal (X) doit être contrôlé par le Pitch (inclinaison AV/AR, orientation[1])
        // Le mouvement Vertical (Y) doit être contrôlé par le Roll (inclinaison Latérale, orientation[2])

        // Assignation X: Pitch (inversé pour le comportement visuel)
        val rawX = -orientation[1]
        // Assignation Y: Roll (pas d'inversion, ou ajuster si nécessaire après test)
        val rawY = orientation[2]

        // 4. Application du filtre passe-bas (Low-Pass Filter)
        val tiltX = lastX + (rawX - lastX) * SMOOTHING
        val tiltY = lastY + (rawY - lastY) * SMOOTHING

        // 5. Clamping (Bornage) des valeurs
        lastX = tiltX.coerceIn(-1.5f, 1.5f)
        lastY = tiltY.coerceIn(-1.5f, 1.5f)

        // Mise à jour de l'état
        _tilt.value = Offset(lastX, lastY)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.w("ReactiveBox", "Capteur peu fiable.")
        }
    }

    // 6. Nettoyage
    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
        Log.i("ReactiveBox", "Capteur désenregistré.")
    }
}