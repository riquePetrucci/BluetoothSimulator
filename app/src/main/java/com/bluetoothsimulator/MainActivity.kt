package com.bluetoothsimulator

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var isSending = false
    private val handler = Handler(Looper.getMainLooper())
    private var temperature = 20

    private lateinit var temperatureText: TextView
    private lateinit var simulatedOutput: TextView

    private val sendTemperatureRunnable = object : Runnable {
        override fun run() {
            if (isSending) {
                sendTemperature(temperature)
                simulatedOutput.text = "Enviado: ${temperature}°C"
                handler.postDelayed(this, 2000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val deviceName = "Henrique celular"
        val device: BluetoothDevice? = getBondedDeviceByName(deviceName)

        temperatureText = findViewById(R.id.textViewTemp)
        simulatedOutput = findViewById(R.id.textViewStatus)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val startButton = findViewById<Button>(R.id.buttonStart)
        val stopButton = findViewById<Button>(R.id.buttonStop)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                temperature = 20 + progress
                temperatureText.text = "Temperatura: ${temperature}°C"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        startButton.setOnClickListener {
            if (device != null) {
                connectToDevice(device)
                isSending = true
                handler.post(sendTemperatureRunnable)
            } else {
                simulatedOutput.text = "Dispositivo não encontrado"
            }
        }

        stopButton.setOnClickListener {
            isSending = false
            disconnectFromDevice()
        }
    }

    private fun getBondedDeviceByName(deviceName: String): BluetoothDevice? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // Permissão não concedida
                return null
            }
        }

        return bluetoothAdapter?.bondedDevices?.find { it.name == deviceName }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        val uuid = device.uuids?.firstOrNull()?.uuid ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
        } catch (e: IOException) {
            simulatedOutput.text = "Erro ao conectar"
        }
    }

    private fun disconnectFromDevice() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (_: IOException) {
        }
    }

    private fun sendTemperature(temp: Int) {
        try {
            outputStream?.write("$temp\n".toByteArray())
        } catch (_: IOException) {
        }
    }
}
