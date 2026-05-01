package com.example.piscicultureapp.ui.bassin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.piscicultureapp.DatabaseHelper
import com.example.piscicultureapp.R
import com.example.piscicultureapp.models.Bassin
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BassinFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: BassinAdapter
    private val listData = mutableListOf<Bassin>()
    
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_bassin, container, false)
        dbHelper = DatabaseHelper(requireContext())

        setupRecyclerView(view)
        setupSearch(view)
        setupButtons(view)

        return view
    }

    private fun setupRecyclerView(view: View) {
        try {
            listData.clear()
            val bassins = dbHelper.getAllBassins()
            listData.addAll(bassins)

            adapter = BassinAdapter(listData,
                onEdit = { showEditDialog(it) },
                onDelete = { confirmDelete(it) }
            )

            val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_bassins)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur lors du chargement des bassins", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSearch(view: View) {
        try {
            val searchView = view.findViewById<SearchView>(R.id.searchView)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = false

                override fun onQueryTextChange(newText: String?): Boolean {
                    try {
                        val filtered = if (newText.isNullOrEmpty()) {
                            dbHelper.getAllBassins()
                        } else {
                            dbHelper.searchBassins(newText)
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

    private fun setupButtons(view: View) {
        try {
            view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_add_bassin).setOnClickListener {
                showAddDialog()
            }
        } catch (e: Exception) {
            // Ignorer si le bouton n'existe pas
        }

        try {
            view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_qualite_eau).setOnClickListener {
                showQualiteEauDialog()
            }
        } catch (e: Exception) {
            // Ignorer si le bouton n'existe pas
        }

        try {
            view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_stats_bassin).setOnClickListener {
                findNavController().navigate(R.id.nav_stats)
            }
        } catch (e: Exception) {
            // Ignorer si le bouton n'existe pas
        }

        // FAB
        try {
            view.findViewById<View>(R.id.fab_add_bassin).setOnClickListener {
                showAddDialog()
            }
        } catch (e: Exception) {
            // Ignorer si le FAB n'existe pas
        }
    }

    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_bassin, null)
        val etNom = dialogView.findViewById<EditText>(R.id.et_nom_bassin)
        val etCapacite = dialogView.findViewById<EditText>(R.id.et_capacite)
        val etLocalisation = dialogView.findViewById<EditText>(R.id.et_localisation)
        val etType = dialogView.findViewById<AutoCompleteTextView>(R.id.et_type_bassin)
        val etEtat = dialogView.findViewById<AutoCompleteTextView>(R.id.et_etat)

        setupDialogDropdowns(etType, etEtat)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Ajouter un bassin")
            .setView(dialogView)
            .setPositiveButton("Ajouter") { _, _ ->
                val nom = etNom.text.toString().trim()
                val capacite = etCapacite.text.toString().toIntOrNull() ?: 0
                val type = etType.text.toString().trim().ifEmpty { null }
                val localisation = etLocalisation.text.toString().trim().ifEmpty { null }

                if (nom.isBlank() || capacite <= 0) {
                    Toast.makeText(requireContext(), "Nom et capacite valides requis", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val id = dbHelper.addBassin(
                    nom = nom,
                    capacite = capacite,
                    type = type,
                    localisation = localisation
                )
                if (id != -1L) {
                    setupRecyclerView(requireView())
                    Toast.makeText(requireContext(), "Bassin ajouté avec succès", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showEditDialog(bassin: Bassin) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_bassin, null)
        val etNom = dialogView.findViewById<EditText>(R.id.et_nom_bassin)
        val etCapacite = dialogView.findViewById<EditText>(R.id.et_capacite)
        val etLocalisation = dialogView.findViewById<EditText>(R.id.et_localisation)
        val etType = dialogView.findViewById<AutoCompleteTextView>(R.id.et_type_bassin)
        val etEtat = dialogView.findViewById<AutoCompleteTextView>(R.id.et_etat)

        setupDialogDropdowns(etType, etEtat)
        etNom.setText(bassin.nomBassin)
        etCapacite.setText(bassin.capacite.toString())
        etLocalisation.setText(bassin.localisation ?: "")
        etType.setText(bassin.typeBassin ?: "", false)
        etEtat.setText(bassin.etat, false)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Modifier le bassin")
            .setView(dialogView)
            .setPositiveButton("Modifier") { _, _ ->
                val nom = etNom.text.toString().trim()
                val capacite = etCapacite.text.toString().toIntOrNull() ?: 0
                val type = etType.text.toString().trim().ifEmpty { null }
                val localisation = etLocalisation.text.toString().trim().ifEmpty { null }

                if (nom.isBlank() || capacite <= 0) {
                    Toast.makeText(requireContext(), "Nom et capacite valides requis", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val rows = dbHelper.updateBassin(bassin.id, nom, capacite, type, localisation)
                if (rows > 0) {
                    setupRecyclerView(requireView())
                    Toast.makeText(requireContext(), "Bassin modifié avec succès", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun setupDialogDropdowns(
        typeView: AutoCompleteTextView,
        etatView: AutoCompleteTextView
    ) {
        val types = listOf("Beton", "Terre", "Bache", "Mixte")
        val etats = listOf("actif", "inactif", "maintenance")

        typeView.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, types))
        etatView.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, etats))
        if (etatView.text.isNullOrBlank()) {
            etatView.setText("actif", false)
        }
    }

    private fun confirmDelete(bassin: Bassin) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Supprimer le bassin")
            .setMessage("Êtes-vous sûr de vouloir supprimer le bassin \"${bassin.nomBassin}\" ?")
            .setIcon(R.drawable.ic_pool)
            .setPositiveButton("Supprimer") { _, _ ->
                if (dbHelper.deleteBassin(bassin.id) > 0) {
                    setupRecyclerView(requireView())
                    Toast.makeText(requireContext(), "Bassin supprimé avec succès", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Erreur lors de la suppression", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showQualiteEauDialog() {
        try {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_qualite_eau, null)
            val etTemperature = dialogView.findViewById<EditText>(R.id.et_temperature)
            val etPh = dialogView.findViewById<EditText>(R.id.et_ph)
            val etOxygene = dialogView.findViewById<EditText>(R.id.et_oxygene)
            val etDateMesure = dialogView.findViewById<EditText>(R.id.et_date_mesure)
            val spinnerBassin = dialogView.findViewById<android.widget.Spinner>(R.id.spinner_bassin)

            // Remplir le spinner avec les bassins disponibles
            val bassins = dbHelper.getAllBassins()
            val bassinAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                bassins.map { "${it.nomBassin} (Capacité: ${it.capacite})" }
            )
            spinnerBassin.adapter = bassinAdapter

            // Mettre la date du jour par défaut
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            etDateMesure.setText(today)

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Ajouter une mesure de qualité d'eau")
                .setView(dialogView)
                .setPositiveButton("Enregistrer") { _, _ ->
                    try {
                        val temperature = etTemperature.text.toString().toFloatOrNull()
                        val ph = etPh.text.toString().toFloatOrNull()
                        val oxygene = etOxygene.text.toString().toFloatOrNull()
                        val dateMesure = etDateMesure.text.toString().trim()
                        val selectedBassinPosition = spinnerBassin.selectedItemPosition

                        if (temperature == null || ph == null || oxygene == null || dateMesure.isBlank() || selectedBassinPosition < 0) {
                            Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        val selectedBassin = bassins[selectedBassinPosition]
                        val result = dbHelper.addQualiteEau(
                            temperature = temperature,
                            ph = ph,
                            oxygene = oxygene,
                            dateMesure = dateMesure,
                            idBassin = selectedBassin.id
                        )

                        if (result != -1L) {
                            Toast.makeText(requireContext(), "Mesure de qualité d'eau ajoutée avec succès", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Erreur lors de l'ajout de la mesure", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur lors de l'ouverture du dialogue", Toast.LENGTH_SHORT).show()
        }
    }
}