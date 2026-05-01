package com.example.piscicultureapp.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("PiscicultureSession", Context.MODE_PRIVATE)

    fun createLoginSession(username: String, role: String, nom: String = "") {
        val editor = prefs.edit()
        editor.putBoolean("IS_LOGGED_IN", true)
        editor.putString("KEY_USERNAME", username)
        editor.putString("KEY_ROLE", role)
        editor.putString("KEY_NOM", nom)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("IS_LOGGED_IN", false)
    }

    fun getUsername(): String? {
        return prefs.getString("KEY_USERNAME", null)
    }

    fun getRole(): String? {
        return prefs.getString("KEY_ROLE", "user")
    }
    
    fun getNom(): String? {
        return prefs.getString("KEY_NOM", "")
    }

    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
