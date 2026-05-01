package com.example.piscicultureapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

            if (navHostFragment == null) {
                Toast.makeText(this, "Erreur de navigation: hote introuvable", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            val navController = navHostFragment.navController
            navView.setupWithNavController(navController)
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur au chargement: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}