package com.example.piscicultureapp.ui.espece

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.piscicultureapp.R
import com.example.piscicultureapp.models.Espece

class EspeceAdapter(
    private val list: MutableList<Espece>,
    private val onEdit: (Espece) -> Unit,
    private val onDelete: (Espece) -> Unit
) : RecyclerView.Adapter<EspeceAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNom: TextView = itemView.findViewById(R.id.tv_nom_espece)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_espece, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNom.text = item.nomEspece
        holder.tvDescription.text = "${item.description ?: "Pas de description"} - ${item.prixUnitaire} Ar/kg"

        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = list.size
}