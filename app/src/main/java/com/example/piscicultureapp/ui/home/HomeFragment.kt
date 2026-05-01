package com.example.piscicultureapp.ui.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.piscicultureapp.DatabaseHelper
import com.example.piscicultureapp.R
import com.example.piscicultureapp.network.MySqlApiConfig
import com.example.piscicultureapp.network.SimpleApiClient
import com.example.piscicultureapp.sync.DataSyncManager
import com.example.piscicultureapp.util.CurrencyFormat
import com.google.android.material.snackbar.Snackbar
import android.widget.ProgressBar
// Imports temporaires - remplacer par MPAndroidChart quand disponible
// import com.github.mikephil.charting.charts.BarChart
// import com.github.mikephil.charting.charts.LineChart
// import com.github.mikephil.charting.charts.PieChart
import kotlin.concurrent.thread

class HomeFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var tvTotalBassins: TextView
    private lateinit var tvTotalPoissons: TextView
    private lateinit var tvStockTotal: TextView
    private lateinit var tvPoidsMoyen: TextView
    private lateinit var tvTotalRecoltes: TextView
    private lateinit var tvTotalPoissonsRecoltes: TextView
    private lateinit var tvPoidsRecoltes: TextView
    private lateinit var tvTotalVentes: TextView
    private lateinit var tvTotalRevenus: TextView
    private lateinit var tvMysqlStatus: TextView
    private lateinit var btnOpenSales: Button
    private lateinit var btnExport: Button
    // Remplacer par des TextViews temporaires
    private lateinit var chartPoissons: TextView
    private lateinit var chartRecoltes: TextView
    private lateinit var chartRemplissage: TextView
    private lateinit var chartMortalite: TextView
    private lateinit var pbSync: ProgressBar
    private lateinit var dataSyncManager: DataSyncManager
    
    // Nouveaux indicateurs
    private lateinit var tvTauxRemplissage: TextView
    private lateinit var tvDetailsRemplissage: TextView
    private lateinit var tvPoidsMoyenGlobal: TextView
    private lateinit var tvMortaliteTotale: TextView
    
    // Statistiques Qualité Eau
    private lateinit var tvAvgTemperatureHome: TextView
    private lateinit var tvAvgPHHome: TextView
    private lateinit var tvAvgOxygeneHome: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            val view = inflater.inflate(R.layout.fragment_home, container, false)

            dbHelper = DatabaseHelper(requireContext())

            tvTotalBassins = view.findViewById(R.id.tv_total_bassins)
            tvTotalPoissons = view.findViewById(R.id.tv_total_poissons)
            tvStockTotal = view.findViewById(R.id.tv_stock_total)
            tvPoidsMoyen = view.findViewById(R.id.tv_poids_moyen)
            tvTotalRecoltes = view.findViewById(R.id.tv_total_recoltes)
            tvTotalPoissonsRecoltes = view.findViewById(R.id.tv_total_poissons_recoltes)
            tvPoidsRecoltes = view.findViewById(R.id.tv_poids_recoltes)
            tvTotalVentes = view.findViewById(R.id.tv_total_ventes)
            tvTotalRevenus = view.findViewById(R.id.tv_total_revenus)
            tvMysqlStatus = view.findViewById(R.id.tv_mysql_status)
            pbSync = view.findViewById(R.id.pb_sync)
            btnOpenSales = view.findViewById(R.id.btn_open_sales)
            val btnOpenStats = view.findViewById<Button>(R.id.btn_open_stats)
            btnExport = view.findViewById(R.id.btn_export_csv)
            dataSyncManager = DataSyncManager(requireContext())
            chartPoissons = view.findViewById(R.id.chart_poissons)
            chartRecoltes = view.findViewById(R.id.chart_recoltes)
            chartRemplissage = view.findViewById(R.id.chart_remplissage)
            chartMortalite = view.findViewById(R.id.chart_mortalite)
            
            // Nouveaux indicateurs
            tvTauxRemplissage = view.findViewById(R.id.tv_taux_remplissage)
            tvDetailsRemplissage = view.findViewById(R.id.tv_details_remplissage)
            tvPoidsMoyenGlobal = view.findViewById(R.id.tv_poids_moyen_global)
            tvMortaliteTotale = view.findViewById(R.id.tv_mortalite_totale)
            
            // Statistiques Qualité Eau
            tvAvgTemperatureHome = view.findViewById(R.id.tv_avg_temperature_home)
            tvAvgPHHome = view.findViewById(R.id.tv_avg_ph_home)
            tvAvgOxygeneHome = view.findViewById(R.id.tv_avg_oxygene_home)

            chargerStatistiques()
            setupCharts()
            setupExportButton()
            setupNavigationButtons()
            setupSyncClick()

            return view
        } catch (e: Exception) {
            val context = requireContext()
            val errorView = TextView(context).apply {
                text = "Erreur de chargement: ${e.message}\n\nVeuillez redémarrer l'application."
                setPadding(32, 32, 32, 32)
                gravity = android.view.Gravity.CENTER
                textSize = 16f
            }
            Toast.makeText(context, "Erreur critique: ${e.message}", Toast.LENGTH_LONG).show()
            return errorView
        }
    }

    private fun setupSyncClick() {
        tvMysqlStatus.setOnClickListener {
            if (pbSync.visibility == View.VISIBLE) return@setOnClickListener
            
            pbSync.visibility = View.VISIBLE
            tvMysqlStatus.text = "Synchronisation..."
            
            dataSyncManager.syncAllToMySQL { result ->
                activity?.runOnUiThread {
                    pbSync.visibility = View.GONE
                    val message = when (result) {
                        is DataSyncManager.SyncResult.Success -> "Synchronisation réussie !"
                        is DataSyncManager.SyncResult.Partial -> "Synchro partielle (${result.successCount}/${result.totalCount})"
                        is DataSyncManager.SyncResult.Error -> "Erreur de synchronisation"
                    }
                    Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
                    chargerStatistiques() // Rafraîchir les stats après sync
                }
            }
        }
    }

    private fun chargerStatistiques() {
        try {
            if (!::dbHelper.isInitialized) return

            tvMysqlStatus.text = getString(R.string.mysql_checking)
            tvMysqlStatus.setBackgroundResource(R.drawable.rounded_background_gray)
            tvMysqlStatus.setTextColor(Color.WHITE)

            thread {
                try {
                    val url = MySqlApiConfig.baseUrl + "stats.php"
                    Log.d("SYNC_DEBUG", "URL: $url")
                    Log.d("SYNC_DEBUG", "Starting network request...")
                    val response = SimpleApiClient.getJson("stats.php")
                    Log.d("SYNC_DEBUG", "Response: $response")
                    val ok = response != null &&
                        response.optBoolean("ok", false) &&
                        response.opt("data") != null
                    Log.d("SYNC_DEBUG", "OK: $ok")

                    activity?.runOnUiThread {
                        if (ok && response != null) {
                            val data = response.optJSONObject("data")!!
                            tvTotalBassins.text = data.optInt("total_bassins", 0).toString()
                            tvTotalPoissons.text = data.optInt("total_poissons", 0).toString()
                            
                            // Stock total et poids moyen depuis MySQL (calculs réels)
                            val stockTotal = data.optDouble("stock_total_kg", 0.0)
                            val poidsMoyen = data.optDouble("poids_moyen_g", 0.0)
                            tvStockTotal.text = "${String.format("%.1f", stockTotal)} kg"
                            tvPoidsMoyen.text = "${String.format("%.0f", poidsMoyen)} g"
                            
                            // Nouveaux indicateurs depuis MySQL
                            val tauxRemplissage = data.optDouble("taux_remplissage", 0.0)
                            val bassinsRemplis = data.optInt("bassins_remplis", 0)
                            val totalBassins = data.optInt("total_bassins", 0)
                            val poidsMoyenGlobal = data.optDouble("poids_moyen_global", 0.0)
                            val mortaliteTotale = data.optInt("mortalite_totale", 0)
                            
                            tvTauxRemplissage.text = "${String.format("%.1f", tauxRemplissage)}%"
                            tvDetailsRemplissage.text = "$bassinsRemplis/$totalBassins bassins remplis"
                            tvPoidsMoyenGlobal.text = "${String.format("%.0f", poidsMoyenGlobal)} g"
                            tvMortaliteTotale.text = mortaliteTotale.toString()
                            
                            tvTotalRecoltes.text = data.optInt("total_recoltes", 0).toString()
                            tvTotalPoissonsRecoltes.text = "${data.optInt("total_poissons_recoltes", 0)} poissons"
                            tvPoidsRecoltes.text = "${String.format("%.1f", data.optDouble("poids_total_recoltes", 0.0))} kg"
                            tvTotalVentes.text = getString(
                                R.string.dashboard_sales_count,
                                data.optInt("total_ventes", 0)
                            )
                            val revenus = data.optDouble("total_revenus", 0.0)
                            tvTotalRevenus.text = CurrencyFormat.formatAriaryWithLabel(revenus)

                            tvMysqlStatus.text = getString(R.string.mysql_connected)
                            tvMysqlStatus.setBackgroundResource(R.drawable.rounded_background_green)
                            tvMysqlStatus.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.white)
                            )
                        } else {
                            Log.d("SYNC_DEBUG", "Response null or invalid, using local data")
                            applyStatsLocale()
                            tvMysqlStatus.text = getString(R.string.mysql_local_sqlite)
                            tvMysqlStatus.setBackgroundResource(R.drawable.rounded_background_orange)
                            tvMysqlStatus.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.white)
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SYNC_DEBUG", "Network exception: ${e.message}", e)
                    Log.e("SYNC_DEBUG", "Exception type: ${e.javaClass.simpleName}")
                    activity?.runOnUiThread {
                        if (::tvTotalBassins.isInitialized) tvTotalBassins.text = "0"
                        if (::tvTotalPoissons.isInitialized) tvTotalPoissons.text = "0"
                        if (::tvStockTotal.isInitialized) tvStockTotal.text = "0 kg"
                        if (::tvPoidsMoyen.isInitialized) tvPoidsMoyen.text = "0 g"
                        if (::tvTotalRecoltes.isInitialized) tvTotalRecoltes.text = "0"
                        if (::tvTotalPoissonsRecoltes.isInitialized) tvTotalPoissonsRecoltes.text = "0 poissons"
                        if (::tvPoidsRecoltes.isInitialized) tvPoidsRecoltes.text = "0.0 kg"
                        if (::tvTotalVentes.isInitialized) {
                            tvTotalVentes.text = getString(R.string.dashboard_sales_count, 0)
                        }
                        if (::tvTotalRevenus.isInitialized) {
                            tvTotalRevenus.text = CurrencyFormat.formatAriaryWithLabel(0.0)
                        }
                        tvMysqlStatus.text = getString(R.string.mysql_local_sqlite)
                        tvMysqlStatus.setBackgroundResource(R.drawable.rounded_background_orange)
                        tvMysqlStatus.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.white)
                        )
                        Toast.makeText(requireContext(), "Erreur stats: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SYNC_DEBUG", "Main exception: ${e.message}", e)
            if (::tvTotalBassins.isInitialized) tvTotalBassins.text = "0"
            if (::tvTotalPoissons.isInitialized) tvTotalPoissons.text = "0"
            if (::tvStockTotal.isInitialized) tvStockTotal.text = "0 kg"
            if (::tvPoidsMoyen.isInitialized) tvPoidsMoyen.text = "0 g"
            if (::tvTotalRecoltes.isInitialized) tvTotalRecoltes.text = "0"
            if (::tvTotalPoissonsRecoltes.isInitialized) tvTotalPoissonsRecoltes.text = "0 poissons"
            if (::tvTotalVentes.isInitialized) {
                tvTotalVentes.text = getString(R.string.dashboard_sales_count, 0)
            }
            if (::tvTotalRevenus.isInitialized) {
                tvTotalRevenus.text = CurrencyFormat.formatAriaryWithLabel(0.0)
            }
            Toast.makeText(requireContext(), "Erreur stats: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyStatsLocale() {
        val totalBassins = dbHelper.getTotalBassins()
        val totalPoissons = dbHelper.getTotalPoissons()
        val totalRecoltes = dbHelper.getTotalRecoltes()
        val totalVentes = dbHelper.getTotalVentes()
        val totalRevenus = dbHelper.getTotalRevenue()
        
        // Calcul du stock total et poids moyen depuis SQLite (calculs réels)
        val stockTotal = dbHelper.getStockTotal()
        val poidsMoyen = if (totalPoissons > 0) (stockTotal * 1000) / totalPoissons else 0.0
        
        // Calcul du taux de remplissage réel
        val bassins = dbHelper.getAllBassins()
        var bassinsRemplis = 0
        var capaciteTotale = 0
        var stockTotalPoissons = 0
        
        for (bassin in bassins) {
            val stockBassin = dbHelper.getTotalPoissonsInBassin(bassin.id)
            capaciteTotale += bassin.capacite
            stockTotalPoissons += stockBassin
            if (stockBassin > 0) bassinsRemplis++
        }
        
        val tauxRemplissage = if (capaciteTotale > 0) (stockTotalPoissons.toDouble() / capaciteTotale * 100) else 0.0
        
        // Calcul de la mortalité totale
        val mortaliteTotale = dbHelper.getTotalMortalite()
        
        // Calcul du poids moyen global réel
        val poidsMoyenGlobal = if (totalPoissons > 0) poidsMoyen else 0.0

        tvTotalBassins.text = totalBassins.toString()
        tvTotalPoissons.text = totalPoissons.toString()
        tvStockTotal.text = "${String.format("%.1f", stockTotal)} kg"
        tvPoidsMoyen.text = "${String.format("%.0f", poidsMoyen)} g"
        
        // Nouveaux indicateurs
        tvTauxRemplissage.text = "${String.format("%.1f", tauxRemplissage)}%"
        tvDetailsRemplissage.text = "$bassinsRemplis/$totalBassins bassins remplis"
        tvPoidsMoyenGlobal.text = "${String.format("%.0f", poidsMoyenGlobal)} g"
        tvMortaliteTotale.text = mortaliteTotale.toString()
        
        tvTotalRecoltes.text = totalRecoltes.toString()
        tvTotalPoissonsRecoltes.text = "${dbHelper.getTotalPoissonsRecoltes()} poissons"
        tvPoidsRecoltes.text = "${String.format("%.1f", dbHelper.getPoidsTotalRecoltes())} kg"
        tvTotalVentes.text = getString(R.string.dashboard_sales_count, totalVentes)
        tvTotalRevenus.text = CurrencyFormat.formatAriaryWithLabel(totalRevenus.toDouble())
        
        // Mettre à jour les statistiques de qualité de l'eau
        try {
            val qualiteEauStats = dbHelper.getQualiteEauStats()
            val avgTemp = qualiteEauStats["avgTemperature"] as Float
            val avgPH = qualiteEauStats["avgPH"] as Float
            val avgOxy = qualiteEauStats["avgOxygene"] as Float
            val totalMesures = qualiteEauStats["totalMesures"] as Int
            
            android.util.Log.d("HomeFragment", "Stats qualité eau: temp=$avgTemp, ph=$avgPH, oxy=$avgOxy, total=$totalMesures")
            
            tvAvgTemperatureHome.text = "${String.format("%.1f", avgTemp)}°C"
            tvAvgPHHome.text = String.format("%.1f", avgPH)
            tvAvgOxygeneHome.text = "${String.format("%.1f", avgOxy)}mg/L"
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Erreur stats qualité eau: ${e.message}")
            tvAvgTemperatureHome.text = "0°C"
            tvAvgPHHome.text = "0.0"
            tvAvgOxygeneHome.text = "0mg/L"
        }
    }

    private fun setupNavigationButtons() {
        if (::btnOpenSales.isInitialized) {
            btnOpenSales.setOnClickListener {
                androidx.navigation.Navigation.findNavController(requireView()).navigate(R.id.nav_vente)
            }
        }
        
        view?.findViewById<android.widget.Button>(R.id.btn_open_stats)?.setOnClickListener {
            androidx.navigation.Navigation.findNavController(requireView()).navigate(R.id.nav_stats)
        }
    }

    private fun setupExportButton() {
        if (::btnExport.isInitialized) {
            btnExport.setOnClickListener {
                try {
                    Toast.makeText(requireContext(), "Export CSV — bientôt relié à MySQL", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Erreur export: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            chargerStatistiques()
            if (::chartPoissons.isInitialized && ::chartRecoltes.isInitialized) {
                loadChartData()
            }
        } catch (_: Exception) {
        }
    }

    private fun setupCharts() {
        // Simplifié temporairement - utiliser des TextViews
        loadChartData()
    }

    // Méthodes de graphiques simplifiées temporairement

    private fun loadChartData() {
        try {
            // Version simplifiée avec TextViews
            val bassins = dbHelper.getAllBassins()
            
            // Données pour le graphique de stocks
            var stocksInfo = "Répartition des stocks:\n"
            bassins.forEach { bassin ->
                val stock = dbHelper.getTotalPoissonsInBassin(bassin.id)
                stocksInfo += "${bassin.nomBassin}: $stock poissons\n"
            }
            chartPoissons.text = stocksInfo
            
            // Données pour les récoltes
            val recoltes = dbHelper.getAllRecoltes()
            var recoltesInfo = "Récoltes récentes:\n"
            recoltes.takeLast(5).forEach { recolte ->
                recoltesInfo += "${recolte.dateRecolte}: ${recolte.poidsTotal} kg\n"
            }
            chartRecoltes.text = recoltesInfo
            
            // Données pour le taux de remplissage
            var remplissageInfo = "Taux de remplissage:\n"
            bassins.forEach { bassin ->
                val stock = dbHelper.getTotalPoissonsInBassin(bassin.id)
                val taux = if (bassin.capacite > 0) (stock.toDouble() / bassin.capacite * 100) else 0.0
                remplissageInfo += "${bassin.nomBassin}: ${String.format("%.1f", taux)}%\n"
            }
            chartRemplissage.text = remplissageInfo
            
            // Données pour la mortalité
            val poissons = dbHelper.getAllPoissons()
            var mortaliteInfo = "Mortalité par lot:\n"
            poissons.takeLast(5).forEach { poisson ->
                mortaliteInfo += "${poisson.nomEspece}: ${poisson.mortalite} morts\n"
            }
            chartMortalite.text = mortaliteInfo
            
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur données: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
