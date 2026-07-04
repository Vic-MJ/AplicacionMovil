package com.example.reloj.presentation

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.example.reloj.R
import com.example.reloj.presentation.theme.RELOJTheme
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
class MainActivity : ComponentActivity(),
    CoroutineScope by MainScope(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    var activityContext: Context? = null
    private var deviceConnected: Boolean = false
    private val PAYLOAD_PATH = "/APP_OPEN"
    lateinit var nodeID: String

    private lateinit var cajaTexto: EditText
    private lateinit var botonEnviar: Button
    private lateinit var etiquetaResultado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityContext = this

        getNodes(this)

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
                agregarMensaje("Buscando celular...")
                getNodes(this)
            }
        }
    }

    private fun getNodes(context: Context) {
        launch(Dispatchers.Default) {
            val nodeList = Wearable.getNodeClient(context).connectedNodes
            try {
                val nodes = Tasks.await(nodeList)
                for (node in nodes) {
                    nodeID = node.id
                    deviceConnected = true
                    runOnUiThread {
                        agregarMensaje("Conexión: ${node.displayName}")
                    }
                }
            } catch (exception: Exception) {
                Log.d("Error", exception.toString())
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
            Log.d("sendMessage", "Mensaje enviado")
        }
        sendMessage.addOnFailureListener { e ->
            Log.d("sendMessage", "Error al enviar mensaje ${e.message}")
            runOnUiThread {
                agregarMensaje("Error: ${e.javaClass.simpleName} ${e.message}")
            }
        }
    }

    override fun onMessageReceived(ME: MessageEvent) {
        Log.d("onMessageReceived", ME.toString())
        Log.d("onMessageReceived", "ID del nodo ${ME.sourceNodeId}")
        Log.d("onMessageReceived", "Payload: ${ME.path}")
        val message = String(ME.data, StandardCharsets.UTF_8)
        Log.d("onMessageReceived", "Mensaje: ${message}")

        nodeID = ME.sourceNodeId
        deviceConnected = true

        runOnUiThread {
            agregarMensaje("Celular: $message")
        }
    }

    private fun agregarMensaje(texto: String) {
        etiquetaResultado.append("\n$texto")
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {}
    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {}
}

@Composable
fun WearApp(greetingName: String) {
    RELOJTheme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(
                scrollState = listState,
                edgeButton = {
                    EdgeButton(
                        onClick = { /*TODO*/ },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                    ) {
                        Text("More")
                    }
                },
            ) { contentPadding ->
                TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                    item {
                        ListHeader(
                            modifier =
                                Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = stringResource(R.string.hello_world, greetingName))
                        }
                    }
                }
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}
