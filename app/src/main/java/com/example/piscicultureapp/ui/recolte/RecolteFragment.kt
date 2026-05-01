package com.example.piscicultureapp.ui.recolte

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.piscicultureapp.DatabaseHelper
import com.example.piscicultureapp.R
import com.example.piscicultureapp.models.Recolte

class RecolteFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: RecolteAdapter
    private var listData = mutableListOf<Recolte>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_recolte, container, false)
        dbHelper = DatabaseHelper(requireContext())

        setupRecyclerView()
        setupSearch()
        setupAddButton()
        setupReportButton()

        return rootView
    }

    private fun setupReportButton() {
        try {
            rootView.findViewById<android.widget.Button>(R.id.btn_harvest_report).setOnClickListener {
                findNavController().navigate(R.id.nav_export)
            }
        } catch (e: Exception) {}
    }

    private fun setupRecyclerView() {
        try {
            android.util.Log.d("RecolteFragment", "Début setupRecyclerView")
            listData.clear()
            val recoltes = dbHelper.getAllRecoltes()
            android.util.Log.d("RecolteFragment", "Nombre de récoltes chargées: ${recoltes.size}")
            listData.addAll(recoltes)

            adapter = RecolteAdapter(listData,
                onEdit = { showEditDialog(it) },
                onDelete = { confirmDelete(it) }
            )
            val recyclerView = rootView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_recoltes)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter
            android.util.Log.d("RecolteFragment", "RecyclerView configuré avec ${listData.size} items")
        } catch (e: Exception) {
            android.util.Log.e("RecolteFragment", "Erreur setupRecyclerView: ${e.message}", e)
            Toast.makeText(requireContext(), "Erreur lors du chargement des récoltes", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSearch() {
        try {
            val searchView = rootView.findViewById<SearchView>(R.id.searchView)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = false

                override fun onQueryTextChange(newText: String?): Boolean {
                    try {
                        val filtered = if (newText.isNullOrEmpty()) {
                            dbHelper.getAllRecoltes()
                        } else {
                            dbHelper.getAllRecoltes().filter {
                                it.nomBassin.contains(newText, ignoreCase = true)
                            }
                        }
                        listData.clear()
                        listData.addAll(filtered)
                        adapter?.notifyDataSetChanged()
                    } catch (e: Exception) {
                        // Ignorer les erreurs de recherche
                    }
                    return true
                }
            })
        } catch (e: Exception) {
            // Ignorer les erreurs d'initialisation de la recherche
        }
    }

    private fun setupAddButton() {
        try {
            val addBtn = rootView.findViewById<android.widget.Button>(R.id.btn_add_recolte)
            addBtn.setOnClickListener {
                showAddRecolteDialog()
            }
        } catch (e: Exception) {
            // Ignorer si le bouton n'existe pas
        }
    }

    private fun showAddRecolteDialog() {
        try {
            val bassins = dbHelper.getAllBassins()
            if (bassins.isEmpty()) {
                Toast.makeText(requireContext(), "Ajoutez d'abord un bassin", Toast.LENGTH_SHORT).show()
                return
            }

            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_recolte, null)
            val btnDate = dialogView.findViewById<Button>(R.id.btn_select_date)
            val spinnerBassin = dialogView.findViewById<Spinner>(R.id.spinner_bassin)
            val etQuantite = dialogView.findViewById<EditText>(R.id.et_quantite)
            val etPoids = dialogView.findViewById<EditText>(R.id.et_poids_total)

            var selectedDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            btnDate.text = selectedDate
            btnDate.setOnClickListener {
                try {
                    val c = java.util.Calendar.getInstance()
                    DatePickerDialog(requireContext(), { _, y, m, d ->
                        selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                        btnDate.text = selectedDate
                    }, c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH), c.get(java.util.Calendar.DAY_OF_MONTH)).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Erreur de sélection de date", Toast.LENGTH_SHORT).show()
                }
            }

            spinnerBassin.adapter = android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                bassins.map { it.nomBassin }
            )

            AlertDialog.Builder(requireContext())
                .setTitle("Ajouter une recolte")
                .setView(dialogView)
                .setPositiveButton("Enregistrer") { _, _ ->
                    try {
                        val quantite = etQuantite.text.toString().toIntOrNull() ?: 0
                        val poids = etPoids.text.toString().toFloatOrNull() ?: 0f
                        
                        if (quantite <= 0 || poids <= 0f) {
                            Toast.makeText(requireContext(), "Valeurs invalides", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        if (spinnerBassin.selectedItemPosition >= bassins.size) {
                            Toast.makeText(requireContext(), "Sélection de bassin invalide", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        
                        val bassin = bassins[spinnerBassin.selectedItemPosition]
                        
                        // Validation: vérifier si le nombre à récolter ne dépasse pas le stock disponible
                        val stockDisponible = dbHelper.getTotalPoissonsInBassin(bassin.id)
                        if (quantite > stockDisponible) {
                            Toast.makeText(requireContext(), "Erreur: Impossible de récolter $quantite poissons. Stock disponible: $stockDisponible poissons dans le bassin ${bassin.nomBassin}", Toast.LENGTH_LONG).show()
                            return@setPositiveButton
                        }
                        
                        val id = dbHelper.addRecolte(selectedDate, quantite, poids, bassin.id)
                        android.util.Log.d("RecolteFragment", "Ajout récolte: id=$id, date=$selectedDate, quantite=$quantite, poids=$poids, bassinId=${bassin.id}")
                        if (id != -1L) {
                            android.util.Log.d("RecolteFragment", "Récolte ajoutée avec succès, mise à jour du RecyclerView")
                            try {
                                setupRecyclerView()
                                android.util.Log.d("RecolteFragment", "RecyclerView mis à jour avec succès")
                            } catch (e: Exception) {
                                android.util.Log.e("RecolteFragment", "Erreur mise à jour RecyclerView: ${e.message}")
                            }
                            Toast.makeText(requireContext(), "Recolte ajoutee", Toast.LENGTH_SHORT).show()
                        } else {
                            android.util.Log.e("RecolteFragment", "Échec ajout récolte")
                            Toast.makeText(requireContext(), "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur lors de l'ouverture du dialogue: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditDialog(recolte: Recolte) {
        try {
            val bassins = dbHelper.getAllBassins()
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_recolte, null)
            val btnDate = dialogView.findViewById<Button>(R.id.btn_select_date)
            val spinnerBassin = dialogView.findViewById<Spinner>(R.id.spinner_bassin)
            val etQuantite = dialogView.findViewById<EditText>(R.id.et_quantite)
            val etPoids = dialogView.findViewById<EditText>(R.id.et_poids_total)
            val btnSave = dialogView.findViewById<Button>(R.id.btn_save_recolte)
            
            btnSave.visibility = View.GONE // Dialog builder has its own buttons
            
            var selectedDate = recolte.dateRecolte ?: ""
            btnDate.text = selectedDate.ifEmpty { "Sélectionner la date" }
            
            btnDate.setOnClickListener {
                val c = java.util.Calendar.getInstance()
                DatePickerDialog(requireContext(), { _, y, m, d ->
                    selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                    btnDate.text = selectedDate
                }, c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH), c.get(java.util.Calendar.DAY_OF_MONTH)).show()
            }

            spinnerBassin.adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, bassins.map { it.nomBassin })
            val currentBassinIndex = bassins.indexOfFirst { it.id == recolte.idBassin }
            if (currentBassinIndex >= 0) spinnerBassin.setSelection(currentBassinIndex)
            
            etQuantite.setText(recolte.quantite.toString())
            etPoids.setText(recolte.poidsTotal.toString())

            AlertDialog.Builder(requireContext())
                .setTitle("Modifier la récolte")
                .setView(dialogView)
                .setPositiveButton("Mettre à jour") { _, _ ->
                    val quantite = etQuantite.text.toString().toIntOrNull() ?: 0
                    val poids = etPoids.text.toString().toFloatOrNull() ?: 0f
                    if (quantite <= 0 || poids <= 0f) {
                        Toast.makeText(requireContext(), "Valeurs invalides", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    val bassinId = bassins[spinnerBassin.selectedItemPosition].id
                    
                    val rows = dbHelper.updateRecolte(recolte.id, selectedDate, quantite, poids, bassinId)
                    if (rows > 0) {
                        setupRecyclerView()
                        Toast.makeText(requireContext(), "Récolte modifiée", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur lors de la modification: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDelete(recolte: Recolte) {
        try {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Supprimer récolte")
                .setMessage("Supprimer la récolte du ${recolte.dateRecolte} ?")
                .setPositiveButton("Oui") { _, _ ->
                    try {
                        if (dbHelper.deleteRecolte(recolte.id) > 0) {
                            setupRecyclerView()
                            Toast.makeText(requireContext(), "Récolte supprimée", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Erreur lors de la suppression", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Non", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur lors de l'ouverture du dialogue", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Pas de binding à nettoyer
    }
}