package com.example.piscicultureapp.ui.employe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.piscicultureapp.R
import com.example.piscicultureapp.models.Employe

class EmployeAdapter(
    private val list: List<Employe>,
    private val onEdit: (Employe) -> Unit,
    private val onDelete: (Employe) -> Unit
) : RecyclerView.Adapter<EmployeAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNom: TextView = itemView.findViewById(R.id.tv_nom)
        val tvRole: TextView = itemView.findViewById(R.id.tv_role)
        val tvTelephone: TextView = itemView.findViewById(R.id.tv_telephone)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_employe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNom.text = "${item.nom} ${item.prenom ?: ""}".trim()
        holder.tvRole.text = "Rôle : ${item.role ?: "Non défini"}"
        holder.tvTelephone.text = "Tél : ${item.telephone ?: "Non renseigné"}"

        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = list.size
}