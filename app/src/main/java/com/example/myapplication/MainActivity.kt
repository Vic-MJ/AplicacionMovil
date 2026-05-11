package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { vista, insets ->
            val barrasSistema = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(barrasSistema.left, barrasSistema.top, barrasSistema.right, barrasSistema.bottom)
            insets
        }

        val cajaTextoEntrada = findViewById<EditText>(R.id.cajaTexto)
        val botonTransferir = findViewById<Button>(R.id.botonProcesar)
        val etiquetaResultado = findViewById<TextView>(R.id.etiquetaResultado)

        botonTransferir.setOnClickListener {
            val textoIngresado = cajaTextoEntrada.text.toString()
            etiquetaResultado.text = textoIngresado
            mostrarAlertaExito()
        }
    }

    private fun mostrarAlertaExito() {
        val constructorAlerta = AlertDialog.Builder(this)
        constructorAlerta.setTitle("Operación Exitosa")
        constructorAlerta.setMessage("¡El texto se ha pasado correctamente!")
        constructorAlerta.setPositiveButton("Aceptar") { dialogo, _ ->
            dialogo.dismiss()
        }
        
        val alerta = constructorAlerta.create()
        alerta.show()
    }
}
