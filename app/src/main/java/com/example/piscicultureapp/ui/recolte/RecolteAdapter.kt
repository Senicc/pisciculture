package com.example.piscicultureapp.ui.recolte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.piscicultureapp.R
import com.example.piscicultureapp.models.Recolte

class RecolteAdapter(
    private val list: MutableList<Recolte>,
    private val onEdit: (Recolte) -> Unit,
    private val onDelete: (Recolte) -> Unit
) : RecyclerView.Adapter<RecolteAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvBassin: TextView = itemView.findViewById(R.id.tv_bassin)
        val tvQuantite: TextView = itemView.findViewById(R.id.tv_quantite)
        val tvPoids: TextView = itemView.findViewById(R.id.tv_poids)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recolte, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val item = list[position]
            android.util.Log.d("RecolteAdapter", "Affichage item $position: ${item.dateRecolte}, ${item.nomBassin}, ${item.quantite}")
            holder.tvDate.text = "Date : ${item.dateRecolte ?: "-"}"
            holder.tvBassin.text = "Bassin : ${item.nomBassin}"
            holder.tvQuantite.text = "Quantité : ${item.quantite} poissons"
            holder.tvPoids.text = "Poids total : ${item.poidsTotal} kg"

            holder.btnEdit.setOnClickListener { onEdit(item) }
            holder.btnDelete.setOnClickListener { onDelete(item) }
        } catch (e: Exception) {
            android.util.Log.e("RecolteAdapter", "Erreur onBindViewHolder position $position: ${e.message}", e)
        }
    }

    override fun getItemCount() = list.size
}