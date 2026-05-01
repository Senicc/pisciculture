package com.example.piscicultureapp.ui.export

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.piscicultureapp.DatabaseHelper
import com.example.piscicultureapp.R
import com.google.android.material.button.MaterialButton
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ExportDataFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var dbHelper: DatabaseHelper
    
    // TextViews pour les statistiques
    private lateinit var tvTotalBassins: TextView
    private lateinit var tvTotalPoissons: TextView
    private lateinit var tvTotalRecoltes: TextView
    private lateinit var tvTotalVentes: TextView
    private lateinit var tvStockTotal: TextView
    private lateinit var tvTotalRevenus: TextView
    
    // Boutons d'exportation
    private lateinit var btnExportCSV: MaterialButton
    private lateinit var btnExportJSON: MaterialButton
    private lateinit var btnShareData: MaterialButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_export_data, container, false)
        dbHelper = DatabaseHelper(requireContext())

        // Initialisation des vues
        initViews()
        
        // Mise à jour des statistiques
        updateStats()
        
        // Configuration des listeners
        setupListeners()

        return rootView
    }

    private fun initViews() {
        // TextViews statistiques
        tvTotalBassins = rootView.findViewById(R.id.tv_total_bassins)
        tvTotalPoissons = rootView.findViewById(R.id.tv_total_poissons)
        tvTotalRecoltes = rootView.findViewById(R.id.tv_total_recoltes)
        tvTotalVentes = rootView.findViewById(R.id.tv_total_ventes)
        tvStockTotal = rootView.findViewById(R.id.tv_stock_total)
        tvTotalRevenus = rootView.findViewById(R.id.tv_total_revenus)
        
        // Boutons d'exportation
        btnExportCSV = rootView.findViewById(R.id.btn_export_csv)
        btnExportJSON = rootView.findViewById(R.id.btn_export_json)
        btnShareData = rootView.findViewById(R.id.btn_share_data)
    }

    private fun updateStats() {
        try {
            val stats = dbHelper.getExportStats()
            
            tvTotalBassins.text = stats["total_bassins"].toString()
            tvTotalPoissons.text = stats["total_poissons"].toString()
            tvTotalRecoltes.text = stats["total_recoltes"].toString()
            tvTotalVentes.text = stats["total_ventes"].toString()
            tvStockTotal.text = "${String.format("%.1f", stats["stock_total_kg"] as Double)}kg"
            tvTotalRevenus.text = "${String.format("%.0f", stats["total_revenus"] as Double)}Ar"
        } catch (e: Exception) {
            // Valeurs par défaut en cas d'erreur
            tvTotalBassins.text = "0"
            tvTotalPoissons.text = "0"
            tvTotalRecoltes.text = "0"
            tvTotalVentes.text = "0"
            tvStockTotal.text = "0kg"
            tvTotalRevenus.text = "0Ar"
        }
    }

    private fun setupListeners() {
        btnExportCSV.setOnClickListener {
            exportToCSV()
        }
        
        btnExportJSON.setOnClickListener {
            exportToJSON()
        }
        
        btnShareData.setOnClickListener {
            shareData()
        }
    }

    private fun exportToCSV() {
        try {
            val csvData = dbHelper.exportAllDataToCSV()
            if (csvData != null) {
                val fileName = "pisciculture_data_${SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())}.csv"
                val file = saveToFile(fileName, csvData)
                
                if (file != null) {
                    Toast.makeText(requireContext(), "Données exportées avec succès: ${file.name}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Erreur lors de l'exportation", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Aucune donnée à exporter", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportToJSON() {
        try {
            val jsonData = dbHelper.exportDataToJSON()
            if (jsonData != null) {
                val fileName = "pisciculture_data_${SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())}.json"
                val file = saveToFile(fileName, jsonData)
                
                if (file != null) {
                    Toast.makeText(requireContext(), "Données exportées avec succès: ${file.name}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Erreur lors de l'exportation", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Aucune donnée à exporter", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareData() {
        try {
            val csvData = dbHelper.exportAllDataToCSV()
            if (csvData != null) {
                val fileName = "pisciculture_data_${SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())}.csv"
                val file = saveToFile(fileName, csvData)
                
                if (file != null) {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, android.net.Uri.fromFile(file))
                        putExtra(Intent.EXTRA_TEXT, "Données de pisciculture exportées")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    
                    startActivity(Intent.createChooser(shareIntent, "Partager les données"))
                } else {
                    Toast.makeText(requireContext(), "Erreur lors de la préparation au partage", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Aucune donnée à partager", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToFile(fileName: String, data: String): File? {
        return try {
            // Créer le répertoire Downloads s'il n'existe pas
            val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "PiscicultureApp")
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            // Créer le fichier
            val file = File(downloadsDir, fileName)
            val fos = FileOutputStream(file)
            fos.write(data.toByteArray())
            fos.close()
            
            file
        } catch (e: Exception) {
            null
        }
    }
}
