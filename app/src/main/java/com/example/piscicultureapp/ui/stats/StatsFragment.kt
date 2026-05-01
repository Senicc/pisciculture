package com.example.piscicultureapp.ui.stats

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.piscicultureapp.DatabaseHelper
import com.example.piscicultureapp.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class StatsFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_stats, container, false)
        dbHelper = DatabaseHelper(requireContext())

        setupPieChart(root.findViewById(R.id.pieChartEspeces))
        setupBarChart(root.findViewById(R.id.barChartBassins))

        return root
    }

    private fun setupPieChart(pieChart: PieChart) {
        val poissons = dbHelper.getAllPoissons()
        val especesMap = HashMap<String, Int>()

        for (p in poissons) {
            val key = p.nomEspece.ifEmpty { "Inconnue" }
            especesMap[key] = (especesMap[key] ?: 0) + p.quantite
        }

        val entries = ArrayList<PieEntry>()
        for ((espece, quantite) in especesMap) {
            if (quantite > 0) entries.add(PieEntry(quantite.toFloat(), espece))
        }

        if (entries.isEmpty()) {
            pieChart.setNoDataText("Aucune donnée disponible")
            return
        }

        val dataSet = PieDataSet(entries, "Espèces")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Répartition"
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun setupBarChart(barChart: BarChart) {
        val bassins = dbHelper.getAllBassins()
        
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        for ((index, bassin) in bassins.withIndex()) {
            val quantite = dbHelper.getTotalPoissonsInBassin(bassin.id).toFloat()
            entries.add(BarEntry(index.toFloat(), quantite))
            labels.add(bassin.nomBassin)
        }

        if (entries.isEmpty()) {
            barChart.setNoDataText("Aucune donnée disponible")
            return
        }

        val dataSet = BarDataSet(entries, "Poissons")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.valueTextSize = 12f

        val data = BarData(dataSet)
        barChart.data = data
        
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate()
    }
}
