package com.example.piscicultureapp.ui.bassin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.piscicultureapp.DatabaseHelper
import com.example.piscicultureapp.R
import com.example.piscicultureapp.models.Bassin

class BassinAdapter(
    private val list: MutableList<Bassin>,
    private val onEdit: (Bassin) -> Unit,
    private val onDelete: (Bassin) -> Unit
) : RecyclerView.Adapter<BassinAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNom: TextView = itemView.findViewById(R.id.tv_nom_bassin)
        val tvCapacite: TextView = itemView.findViewById(R.id.tv_capacite)
        val tvLocalisation: TextView = itemView.findViewById(R.id.tv_localisation)
        val tvType: TextView = itemView.findViewById(R.id.tv_type)
        val tvEtat: TextView = itemView.findViewById(R.id.tv_etat)
        val tvFillRate: TextView = itemView.findViewById(R.id.tv_fill_rate)
        val tvStockDetails: TextView = itemView.findViewById(R.id.tv_stock_details)
        val progressFillRate: ProgressBar = itemView.findViewById(R.id.progress_fill_rate)
        
        // Statistiques Qualité Eau (nullable pour éviter les crashes)
        val tvTemperatureBassinItem: TextView? = itemView.findViewById(R.id.tv_temperature_bassin_item)
        val tvPHBassinItem: TextView? = itemView.findViewById(R.id.tv_ph_bassin_item)
        val tvOxygeneBassinItem: TextView? = itemView.findViewById(R.id.tv_oxygene_bassin_item)
        
        val btnEdit: View = itemView.findViewById(R.id.btn_edit)
        val btnDelete: View = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bassin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        
        holder.tvNom.text = item.nomBassin
        holder.tvCapacite.text = "${item.capacite} poissons"
        holder.tvLocalisation.text = item.localisation ?: "Non spécifiée"
        holder.tvType.text = item.typeBassin ?: "Non spécifié"
        holder.tvEtat.text = item.etat.replaceFirstChar { ch ->
            if (ch.isLowerCase()) ch.titlecase() else ch.toString()
        }

        // Configuration de l'état
        when (item.etat.lowercase()) {
            "actif" -> {
                holder.tvEtat.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.success))
                holder.tvEtat.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.rounded_background_green)
            }
            "inactif" -> {
                holder.tvEtat.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.gray_medium))
                holder.tvEtat.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.rounded_background_gray)
            }
            "maintenance" -> {
                holder.tvEtat.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.warning))
                holder.tvEtat.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.rounded_background_orange)
            }
            else -> {
                holder.tvEtat.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.gray_medium))
                holder.tvEtat.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.rounded_background_gray)
            }
        }

        // Calcul du taux de remplissage réel et du poids moyen
        val dbHelper = DatabaseHelper(holder.itemView.context)
        val currentStock = dbHelper.getTotalPoissonsInBassin(item.id)
        val fillRate = if (item.capacite > 0) (currentStock.toFloat() / item.capacite * 100).toInt() else 0
        
        // Calcul du stock total en kg et du poids moyen en g pour ce bassin
        val stockTotalKg = dbHelper.getStockTotalInBassin(item.id)
        val poidsMoyenG = if (currentStock > 0) (stockTotalKg * 1000) / currentStock else 0.0
        
        holder.progressFillRate.progress = fillRate
        holder.tvFillRate.text = "$fillRate%"
        holder.tvStockDetails.text = "$currentStock / ${item.capacite} poissons | ${String.format("%.1f", stockTotalKg)} kg | ${String.format("%.0f", poidsMoyenG)} g"

        // Configuration de la couleur de la progressBar selon le taux
        when {
            fillRate >= 90 -> {
                holder.progressFillRate.progressTintList = ContextCompat.getColorStateList(holder.itemView.context, R.color.error)
            }
            fillRate >= 75 -> {
                holder.progressFillRate.progressTintList = ContextCompat.getColorStateList(holder.itemView.context, R.color.warning)
            }
            else -> {
                holder.progressFillRate.progressTintList = ContextCompat.getColorStateList(holder.itemView.context, R.color.water_blue)
            }
        }

        // Afficher les statistiques qualité de l'eau pour ce bassin
        try {
            val qualiteEauStats = dbHelper.getQualiteEauStatsByBassin(item.id)
            
            // Vérifier si les TextView existent avant de les utiliser
            holder.tvTemperatureBassinItem?.text = "${String.format("%.1f", qualiteEauStats["avgTemperature"] as Float)}°C"
            holder.tvPHBassinItem?.text = String.format("%.1f", qualiteEauStats["avgPH"] as Float)
            holder.tvOxygeneBassinItem?.text = "${String.format("%.1f", qualiteEauStats["avgOxygene"] as Float)}mg/L"
        } catch (e: Exception) {
            // Gérer les erreurs silencieusement pour ne pas causer de crash
            try {
                holder.tvTemperatureBassinItem?.text = "0°C"
                holder.tvPHBassinItem?.text = "0.0"
                holder.tvOxygeneBassinItem?.text = "0mg/L"
            } catch (ex: Exception) {
                // Ignorer si les TextView n'existent pas
            }
        }

        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = list.size
}