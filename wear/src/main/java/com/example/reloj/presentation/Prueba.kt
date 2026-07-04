package com.example.reloj.presentation

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.reloj.R

class Prueba : ComponentActivity(), SensorEventListener {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null
    private var sensorType = Sensor.TYPE_HEART_RATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(sensorType)

        setContentView(R.layout.prueba)
        startSensor()

        mediaPlayer = MediaPlayer.create(this, R.raw.pacman)
        val botonVentana2: Button = findViewById(R.id.btn2)
        botonVentana2.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
            }
        }
    }

    fun startSensor() {
        if (checkSelfPermission(android.Manifest.permission.BODY_SENSORS)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.BODY_SENSORS), 1001)
            return
        }
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == sensorType) {
            val lectura = event.values[0]
            Log.d("onSensorChanged", "Lectura: ${lectura}")
            findViewById<TextView>(R.id.txtBpm).text = "BPM: $lectura"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}