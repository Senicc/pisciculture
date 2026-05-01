package com.example.piscicultureapp.ui.nourrissage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.piscicultureapp.R
import com.example.piscicultureapp.models.Nourrissage

class NourrissageAdapter(
    private val list: MutableList<Nourrissage>,
    private val onEdit: (Nourrissage) -> Unit,
    private val onDelete: (Nourrissage) -> Unit
) : RecyclerView.Adapter<NourrissageAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvBassin: TextView = itemView.findViewById(R.id.tv_bassin)
        val tvAliment: TextView = itemView.findViewById(R.id.tv_aliment)
        val tvQuantite: TextView = itemView.findViewById(R.id.tv_quantite)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nourrissage, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvDate.text = "Date : ${item.dateNourrissage ?: "-"}"
        holder.tvBassin.text = "Bassin : ${item.nomBassin}"
        holder.tvAliment.text = "Aliment : ${item.nomAliment}"
        holder.tvQuantite.text = "Quantité : ${item.quantite} kg"

        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = list.size
}