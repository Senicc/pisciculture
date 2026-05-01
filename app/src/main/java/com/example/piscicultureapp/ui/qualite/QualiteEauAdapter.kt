package com.example.piscicultureapp.ui.qualite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.piscicultureapp.R
import com.example.piscicultureapp.models.QualiteEau

class QualiteEauAdapter(
    private val list: MutableList<QualiteEau>,
    private val onEdit: (QualiteEau) -> Unit,
    private val onDelete: (QualiteEau) -> Unit
) : RecyclerView.Adapter<QualiteEauAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvBassin: TextView = itemView.findViewById(R.id.tv_bassin)
        val tvTemp: TextView = itemView.findViewById(R.id.tv_temp)
        val tvPh: TextView = itemView.findViewById(R.id.tv_ph)
        val tvOxygene: TextView = itemView.findViewById(R.id.tv_oxygene)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_qualite_eau, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvDate.text = "Date : ${item.dateMesure ?: "-"}"
        holder.tvBassin.text = "Bassin : ${item.nomBassin}"
        holder.tvTemp.text = "Temp : ${item.temperature}°C"
        holder.tvPh.text = "pH : ${item.ph}"
        holder.tvOxygene.text = "O₂ : ${item.oxygene} mg/L"

        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = list.size
}