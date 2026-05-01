package com.example.piscicultureapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: com.example.piscicultureapp.session.SessionManager
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = com.example.piscicultureapp.session.SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            val intent = Intent(this, MainActivity::class.java)
            // On peut passer le role stocké dans la session
            intent.putExtra("ROLE", sessionManager.getRole())
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)
        dbHelper.addDefaultUsers()
        dbHelper.addDefaultReferenceData()

        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnGoRegister = findViewById<Button>(R.id.btn_go_register)

        btnGoRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false

            // Essayer de se connecter via API d'abord
            kotlin.concurrent.thread {
                try {
                    val jsonData = org.json.JSONObject().apply {
                        put("username", username)
                        put("password", password)
                    }
                    val response = com.example.piscicultureapp.network.SimpleApiClient.postJson("auth_login.php", jsonData)
                    
                    if (response != null && response.optBoolean("ok", false)) {
                        val data = response.optJSONObject("data")
                        val role = data?.optString("role", "user") ?: "user"
                        val nom = data?.optString("nom", username) ?: username

                        runOnUiThread {
                            sessionManager.createLoginSession(username, role, nom)
                            Toast.makeText(this@LoginActivity, "Connexion réussie (Online)", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("ROLE", role)
                            startActivity(intent)
                            finishAffinity()
                        }
                    } else {
                        // Error from API, fallback to local
                        runOnUiThread {
                            loginLocally(username, password)
                        }
                    }
                } catch (e: Exception) {
                    // Exception, fallback to local
                    runOnUiThread {
                        loginLocally(username, password)
                    }
                }
            }
        }
    }

    private fun loginLocally(username: String, password: String) {
        val (success, role) = dbHelper.login(username, password)

        if (success && role != null) {
            sessionManager.createLoginSession(username, role, username)
            Toast.makeText(this, "Connexion réussie (Hors ligne)", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("ROLE", role ?: "user")
            startActivity(intent)
            finishAffinity()
        } else {
            Toast.makeText(this, "Identifiants incorrects (Hors ligne)", Toast.LENGTH_LONG).show()
            findViewById<Button>(R.id.btn_login).isEnabled = true
        }
    }
}