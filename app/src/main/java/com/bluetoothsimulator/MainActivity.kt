package com.bluetoothsimulator

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.io.IOException
import java.util.UUID
import kotlin.concurrent.thread
import android.widget.SeekBar

class MainActivity : ComponentActivity() {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val appUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var serverSocket: BluetoothServerSocket? = null
    private var isListening = false

    // Temperatura controlada pelo SeekBar
    private var currentTemp = 50.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.statusText)
        val clientMacText = findViewById<TextView>(R.id.clientMacText)
        val listenButton = findViewById<Button>(R.id.listenButton)
        val tempSeekBar = findViewById<SeekBar>(R.id.tempSeekBar)
        val tempValueText = findViewById<TextView>(R.id.tempValueText)

        // Configura SeekBar
        tempSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Range 20–80 °C
                currentTemp = 20 + (progress / 100.0) * 60
                tempValueText.text = "Temperatura: %.1f °C".format(currentTemp)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        listenButton.setOnClickListener {
            if (!isListening) {
                startListening(statusText, clientMacText)
            }
        }
    }

    private fun startListening(statusText: TextView, clientMacText: TextView) {
        isListening = true
        thread {
            try {
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord("BluetoothSim", appUuid)
                runOnUiThread { statusText.text = "Aguardando conexão..." }

                val socket: BluetoothSocket = serverSocket!!.accept()

                val clientDevice = socket.remoteDevice
                val clientMac = clientDevice.address

                runOnUiThread {
                    statusText.text = "Conectado!"
                    clientMacText.text = "MAC do cliente: $clientMac"
                }

                thread {
                    try {
                        val output = socket.outputStream

                        while (true) {
                            val message = "Temp: %.1f\n".format(currentTemp)
                            output.write(message.toByteArray())
                            output.flush()

                            runOnUiThread {
                                statusText.text = "Enviado: $message"
                                clientMacText.text = "MAC do cliente: $clientMac\nTemp: %.1f".format(currentTemp)
                            }

                            Thread.sleep(1000)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        runOnUiThread {
                            statusText.text = "Erro no envio: ${e.message}"
                        }
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    statusText.text = "Erro ao conectar: ${e.message}"
                }
            }
        }
    }
}
