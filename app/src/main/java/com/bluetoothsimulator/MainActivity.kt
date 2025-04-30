package com.bluetoothsimulator

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.widget.*
import android.content.pm.PackageManager
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothSocket: BluetoothSocket? = null
    private var connectedDevice: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        val textViewTemp = findViewById<TextView>(R.id.textViewTemp)
        val textViewStatus = findViewById<TextView>(R.id.textViewStatus)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val buttonStart = findViewById<Button>(R.id.buttonStart)
        val buttonStop = findViewById<Button>(R.id.buttonStop)
        val editTextMac = findViewById<EditText>(R.id.editTextDeviceAddress)
        val buttonConnect = findViewById<Button>(R.id.buttonConnect)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textViewTemp.text = "Temperatura: ${progress + 20}°C"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        buttonConnect.setOnClickListener {
            val macAddress = editTextMac.text.toString()

            if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 1)
                return@setOnClickListener
            }

            if (BluetoothAdapter.checkBluetoothAddress(macAddress)) {
                val device = bluetoothAdapter.getRemoteDevice(macAddress)
                connectToDevice(device, textViewStatus)
            } else {
                Toast.makeText(this, "Endereço MAC inválido", Toast.LENGTH_SHORT).show()
            }
        }

        buttonStart.setOnClickListener {
            sendData("START")
            textViewStatus.text = "Status: iniciado"
        }

        buttonStop.setOnClickListener {
            sendData("STOP")
            textViewStatus.text = "Status: parado"
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice, statusView: TextView) {
        thread {
            try {
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                val socket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothAdapter.cancelDiscovery()
                socket.connect()
                bluetoothSocket = socket
                connectedDevice = device
                runOnUiThread {
                    Toast.makeText(this, "Conectado a ${device.name}", Toast.LENGTH_SHORT).show()
                    statusView.text = "Status: conectado"
                }
            } catch (e: IOException) {
                runOnUiThread {
                    Toast.makeText(this, "Erro ao conectar: ${e.message}", Toast.LENGTH_LONG).show()
                    statusView.text = "Status: erro na conexão"
                }
            }
        }
    }

    private fun sendData(data: String) {
        thread {
            try {
                bluetoothSocket?.outputStream?.write(data.toByteArray())
            } catch (e: IOException) {
                runOnUiThread {
                    Toast.makeText(this, "Erro ao enviar dados", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
