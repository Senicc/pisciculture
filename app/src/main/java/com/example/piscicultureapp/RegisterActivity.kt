package com.example.piscicultureapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.piscicultureapp.network.MySqlApiConfig
import com.example.piscicultureapp.network.SimpleApiClient
import com.example.piscicultureapp.session.SessionManager
import org.json.JSONObject
import kotlin.concurrent.thread

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        val etNom = findViewById<EditText>(R.id.et_nom)
        val etUsername = findViewById<EditText>(R.id.et_register_username)
        val etPassword = findViewById<EditText>(R.id.et_register_password)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val btnBackToLogin = findViewById<Button>(R.id.btn_back_to_login)

        btnBackToLogin.setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            val nom = etNom.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty() || nom.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            
            // On essaie l'API d'abord
            thread {
                try {
                    val jsonData = JSONObject().apply {
                        put("nom", nom)
                        put("username", username)
                        put("password", password)
                    }
                    
                    val response = SimpleApiClient.postJson("auth_register.php", jsonData)
                    val ok = response?.optBoolean("ok", false) ?: false
                    
                    if (ok && response != null) {
                        val data = response.optJSONObject("data")
                        val role = data?.optString("role", "user") ?: "user"
                        
                        // Enregistrer aussi localement pour le mode hors ligne
                        dbHelper.register(username, password, nom)
                        
                        runOnUiThread {
                            sessionManager.createLoginSession(username, role, nom)
                            Toast.makeText(this@RegisterActivity, "Inscription réussie", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        }
                    } else {
                        val error = response?.optString("error", "Erreur réseau") ?: "Erreur réseau"
                        
                        runOnUiThread {
                            // En cas d'erreur de connexion, on autorise l'inscription locale
                            if (error == "Erreur réseau") {
                                registerLocally(username, password, nom)
                            } else {
                                Toast.makeText(this@RegisterActivity, error, Toast.LENGTH_LONG).show()
                                btnRegister.isEnabled = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        registerLocally(username, password, nom)
                    }
                }
            }
        }
    }
    
    private fun registerLocally(username: String, password: String, nom: String) {
        val (success, msg) = dbHelper.register(username, password, nom)
        if (success) {
            sessionManager.createLoginSession(username, "user", nom)
            Toast.makeText(this, "Compte créé localement (Hors ligne)", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
        } else {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            findViewById<Button>(R.id.btn_register).isEnabled = true
        }
    }
}
