package com.example.piscicultureapp.ui.vente

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.piscicultureapp.R
import com.example.piscicultureapp.models.Vente
import com.example.piscicultureapp.util.CurrencyFormat

class VenteAdapter(
    private val list: MutableList<Vente>,
    private val onEdit: (Vente) -> Unit,
    private val onDelete: (Vente) -> Unit
) : RecyclerView.Adapter<VenteAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvClient: TextView = itemView.findViewById(R.id.tv_client)
        val tvBassin: TextView = itemView.findViewById(R.id.tv_bassin)
        val tvPrix: TextView = itemView.findViewById(R.id.tv_prix)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vente, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvDate.text = "Date : ${item.dateVente ?: "-"}"
        holder.tvClient.text = "Client : ${item.client}"
        holder.tvBassin.text = "Espèce : ${item.nomEspece}"
        holder.tvPrix.text = "Montant : ${CurrencyFormat.formatAriary(item.prixTotal.toDouble())} · Ariary"

        // Animation d'entrée fluide
        animateItemEntry(holder.itemView, position)

        // Modernisation des icônes avec animations de clic
        holder.btnEdit.setOnClickListener { 
            animateButtonClick(holder.btnEdit)
            onEdit(item) 
        }
        holder.btnDelete.setOnClickListener { 
            animateButtonClick(holder.btnDelete)
            onDelete(item) 
        }
    }

    override fun getItemCount() = list.size

    private fun animateItemEntry(view: View, position: Int) {
        view.alpha = 0f
        view.translationY = 50f
        
        val animatorSet = AnimatorSet()
        val fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        val slideUp = ObjectAnimator.ofFloat(view, "translationY", 50f, 0f)
        
        animatorSet.playTogether(fadeIn, slideUp)
        animatorSet.duration = 300
        animatorSet.startDelay = (position * 50).toLong()
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()
    }

    private fun animateButtonClick(button: View) {
        val scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.8f, 1f)
        val scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.8f, 1f)
        
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.duration = 150
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()
    }

    fun addItem(item: Vente) {
        val position = list.size
        list.add(item)
        notifyItemInserted(position)
    }

    fun removeItem(item: Vente) {
        val position = list.indexOf(item)
        if (position != -1) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateItem(item: Vente) {
        val position = list.indexOfFirst { it.id == item.id }
        if (position != -1) {
            list[position] = item
            notifyItemChanged(position)
        }
    }
}