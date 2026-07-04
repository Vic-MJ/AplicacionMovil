package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
class MainActivity : AppCompatActivity(),
    CoroutineScope by MainScope(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    lateinit var conectar: Button
    var activityContext: Context? = null

    private var deviceConnected: Boolean = false
    private val PAYLOAD_PATH = "/APP_OPEN"
    lateinit var nodeID: String
    private lateinit var cajaTexto: EditText
    private lateinit var botonEnviar: Button
    private lateinit var etiquetaResultado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityContext = this
        conectar = findViewById(R.id.boton)

        conectar.setOnClickListener {
            if (!deviceConnected) {
                agregarMensaje("Buscando reloj conectado...")
                val tempAct: Activity = activityContext as MainActivity
                getNodes(tempAct)
            } else {
                agregarMensaje("Ya estás conectado con el reloj")
            }
        }

        cajaTexto = findViewById(R.id.cajaTexto)
        botonEnviar = findViewById(R.id.botonEnviar)
        etiquetaResultado = findViewById(R.id.etiquetaResultado)

        botonEnviar.setOnClickListener {
            if (deviceConnected) {
                val texto = cajaTexto.text.toString()
                if (texto.isNotEmpty()) {
                    sendMessage(texto)
                    agregarMensaje("Yo: $texto")
                    cajaTexto.text.clear()
                }
            } else {
                agregarMensaje("Primero presiona \"Conectar con el reloj\"")
            }
        }
    }
    private fun getNodes(context: Context) {
        launch(Dispatchers.Default) {
            val nodeList = Wearable.getNodeClient(context).connectedNodes
            try {
                val nodes = Tasks.await(nodeList)
                if (nodes.isEmpty()) {
                    runOnUiThread {
                        agregarMensaje(
                            "No se encontró ningún reloj conectado.\n"
                        )
                    }
                }
                for (node in nodes) {
                    Log.d("NODO", node.toString())
                    Log.d("NODO", "El id del nodo es: ${node.id}")
                    nodeID = node.id
                    deviceConnected = true
                    runOnUiThread {
                        conectar.text = "Conectado: ${node.displayName}"
                        agregarMensaje("Conectado con: ${node.displayName}")
                    }
                }
            } catch (exception: Exception) {
                Log.d("Error en el nodo", exception.toString())
                runOnUiThread {
                    agregarMensaje("Error al buscar el reloj: ${exception.message}")
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            Wearable.getDataClient(activityContext!!).removeListener(this)
            Wearable.getMessageClient(activityContext!!).removeListener(this)
            Wearable.getCapabilityClient(activityContext!!).removeListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            Wearable.getDataClient(activityContext!!).addListener(this)
            Wearable.getMessageClient(activityContext!!).addListener(this)
            Wearable.getCapabilityClient(activityContext!!)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendMessage(mensaje: String) {
        val sendMessage = Wearable.getMessageClient(activityContext!!)
            .sendMessage(nodeID, PAYLOAD_PATH, mensaje.toByteArray())
        sendMessage.addOnSuccessListener {
            Log.d("sendMessage", "Mensaje enviado correctamente")
        }
        sendMessage.addOnFailureListener { e ->
            Log.d("sendMessage", "Error al enviar mensaje ${e.message}")
            runOnUiThread {
                agregarMensaje("Error al enviar mensaje: ${e.javaClass.simpleName} ${e.message}")
            }
        }
    }

    override fun onMessageReceived(ME: MessageEvent) {
        Log.d("onMessageReceived", ME.toString())
        Log.d("onMessageReceived", "ID del nodo ${ME.sourceNodeId}")
        Log.d("onMessageReceived", "Payload: ${ME.path}")
        val message = String(ME.data, StandardCharsets.UTF_8)
        Log.d("onMessageReceived", "Mensaje: ${message}")

        runOnUiThread {
            agregarMensaje("Reloj: $message")
        }
    }

    private fun agregarMensaje(texto: String) {
        etiquetaResultado.append("\n$texto")
    }
    override fun onDataChanged(dataEvents: DataEventBuffer) {}
    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {}
}
