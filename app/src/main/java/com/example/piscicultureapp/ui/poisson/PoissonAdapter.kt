package com.example.piscicultureapp.ui.poisson

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.piscicultureapp.R
import com.example.piscicultureapp.models.Poisson

class PoissonAdapter(
    private val list: MutableList<Poisson>,
    private val onEdit: (Poisson) -> Unit,
    private val onDelete: (Poisson) -> Unit,
    private val onEditPrice: (Poisson) -> Unit,
    private val onTransfer: (Poisson) -> Unit
) : RecyclerView.Adapter<PoissonAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEspece: TextView = itemView.findViewById(R.id.tv_espece)
        val tvBassin: TextView = itemView.findViewById(R.id.tv_bassin)
        val tvQuantite: TextView = itemView.findViewById(R.id.tv_quantite)
        val tvMortalite: TextView = itemView.findViewById(R.id.tv_mortalite)
        val tvPoids: TextView = itemView.findViewById(R.id.tv_poids)
        val tvStock: TextView = itemView.findViewById(R.id.tv_stock)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)
        val btnEditPrice: ImageButton = itemView.findViewById(R.id.btn_edit_price)
        val btnTransfer: ImageButton = itemView.findViewById(R.id.btn_transfer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_poisson, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        
        // Informations de base
        holder.tvEspece.text = "Espèce : ${item.nomEspece}"
        holder.tvBassin.text = "Bassin : ${item.nomBassin}"
        holder.tvQuantite.text = "Quantité : ${item.quantite}"
        holder.tvMortalite.text = "Mortalité : ${item.mortalite}"
        
        // Poids réel (transformé de "poids moyen" en "poids")
        val poids = item.poidsMoyen ?: 0f
        holder.tvPoids.text = "Poids : ${String.format("%.1f", poids)} g"
        
        // Stock total calculé (quantité - mortalité) * poids
        val stockTotal = (item.quantite - item.mortalite) * poids / 1000 // en kg
        holder.tvStock.text = "Stock total : ${String.format("%.2f", stockTotal)} kg"

        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
        holder.btnEditPrice.setOnClickListener { onEditPrice(item) }
        holder.btnTransfer.setOnClickListener { onTransfer(item) }
    }

    override fun getItemCount() = list.size
}