package com.example.piscicultureapp.ui.nourrissage

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.piscicultureapp.DatabaseHelper
import com.example.piscicultureapp.R
import com.example.piscicultureapp.models.Nourrissage
import java.util.Calendar

class NourrissageFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: NourrissageAdapter
    private var listData = mutableListOf<Nourrissage>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_nourrissage, container, false)
        dbHelper = DatabaseHelper(requireContext())

        setupRecyclerView()
        setupSearch()
        setupAddButton()
        setupScheduleButton()

        return rootView
    }

    private fun setupScheduleButton() {
        try {
            rootView.findViewById<android.widget.Button>(R.id.btn_schedule_feeding).setOnClickListener {
                showAddNourrissageDialog()
            }
        } catch (e: Exception) {}
    }

    private fun setupRecyclerView() {
        listData.clear()
        listData.addAll(dbHelper.getAllNourrissages())

        adapter = NourrissageAdapter(listData,
            onEdit = { showEditDialog(it) },
            onDelete = { confirmDelete(it) }
        )

        val recyclerView = rootView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_nourrissage)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        val searchView = rootView.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = if (newText.isNullOrEmpty()) {
                    dbHelper.getAllNourrissages()
                } else {
                    dbHelper.getAllNourrissages().filter {
                        (it.nomBassin.contains(newText, ignoreCase = true)) ||
                                (it.nomAliment.contains(newText, ignoreCase = true))
                    }
                }
                listData.clear()
                listData.addAll(filtered)
                adapter.notifyDataSetChanged()
                return true
            }
        })
    }

    private fun setupAddButton() {
        val addBtn = rootView.findViewById<android.widget.Button>(R.id.btn_add_nourrissage)
        addBtn.setOnClickListener {
            showAddNourrissageDialog()
        }
    }

    private fun showAddNourrissageDialog() {
        try {
            val bassins = dbHelper.getAllBassins()
            if (bassins.isEmpty()) {
                Toast.makeText(requireContext(), "Ajoutez d'abord des bassins", Toast.LENGTH_SHORT).show()
                return
            }

            val container = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(48, 24, 48, 8)
            }

            val btnDate = Button(requireContext()).apply {
                text = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            }
            val spinnerBassin = Spinner(requireContext())
            val spinnerAliment = Spinner(requireContext())
            val aliments = try {
                dbHelper.getAllAlimentations()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erreur lors du chargement des aliments", Toast.LENGTH_SHORT).show()
                return
            }
            
            if (aliments.isEmpty()) {
                Toast.makeText(requireContext(), "Ajoutez d'abord des aliments", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Validation supplémentaire pour éviter les crashes
            if (aliments.any { it.nomAliment.isNullOrBlank() }) {
                Toast.makeText(requireContext(), "Erreur: Certains aliments ont des noms invalides", Toast.LENGTH_SHORT).show()
                return
            }
            
            spinnerAliment.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                aliments.map { it.nomAliment }
            )
            val etQuantite = EditText(requireContext()).apply {
                hint = "Quantité (kg)"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            }

            var selectedDate = btnDate.text.toString()
            btnDate.setOnClickListener {
                try {
                    val c = Calendar.getInstance()
                    DatePickerDialog(requireContext(), { _, y, m, d ->
                        selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                        btnDate.text = selectedDate
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Erreur de sélection de date", Toast.LENGTH_SHORT).show()
                }
            }

            spinnerBassin.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, bassins.map { it.nomBassin })

            container.addView(btnDate)
            container.addView(spinnerBassin)
            container.addView(spinnerAliment)
            container.addView(etQuantite)

            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Ajouter un nourrissage")
                .setView(container)
                .setPositiveButton("Enregistrer") { _, _ ->
                    try {
                        val quantite = etQuantite.text.toString().toFloatOrNull() ?: 0f
                        
                        if (spinnerAliment.selectedItemPosition >= aliments.size) {
                            Toast.makeText(requireContext(), "Sélection d'aliment invalide", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        val selectedAliment = aliments[spinnerAliment.selectedItemPosition]

                        if (quantite <= 0f) {
                            Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        if (spinnerBassin.selectedItemPosition >= bassins.size) {
                            Toast.makeText(requireContext(), "Sélection de bassin invalide", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        val bassin = bassins[spinnerBassin.selectedItemPosition]
                        val alimentId = selectedAliment.id
                        
                        // Validation de l'ID aliment pour éviter le crash
                        if (alimentId > Int.MAX_VALUE) {
                            Toast.makeText(requireContext(), "Erreur: ID d'aliment trop grand", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        
                        val id = dbHelper.addNourrissage(selectedDate, quantite, bassin.id, alimentId.toInt())
                        if (id != -1L) {
                            setupRecyclerView()
                            Toast.makeText(requireContext(), "Nourrissage ajouté avec succès", Toast.LENGTH_SHORT).show()
                        } else {
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

    private fun showEditDialog(nourrissage: Nourrissage) {
        val container = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }

        val btnDate = Button(requireContext()).apply {
            text = nourrissage.dateNourrissage ?: java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        }
        val etQuantite = EditText(requireContext()).apply {
            hint = "Nouvelle quantité (kg)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(nourrissage.quantite.toString())
        }
        val etTypeAliment = EditText(requireContext()).apply {
            hint = "Type d'aliment"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            setText(nourrissage.nomAliment)
        }

        var selectedDate = btnDate.text.toString()
        btnDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                btnDate.text = selectedDate
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        container.addView(btnDate)
        container.addView(etQuantite)
        container.addView(etTypeAliment)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Modifier le nourrissage")
            .setMessage("Nourrissage du ${nourrissage.dateNourrissage}")
            .setView(container)
            .setPositiveButton("Modifier") { _, _ ->
                val newQuantite = etQuantite.text.toString().toFloatOrNull() ?: nourrissage.quantite
                val newTypeAliment = etTypeAliment.text.toString().trim()

                if (newQuantite <= 0f || newTypeAliment.isEmpty()) {
                    Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Ajouter ou mettre à jour l'aliment si nécessaire
                var alimentId = dbHelper.getAllAlimentations().find { it.nomAliment.equals(newTypeAliment, ignoreCase = true) }?.id
                if (alimentId == null) {
                    alimentId = dbHelper.addAlimentation(newTypeAliment, "Aliment ajouté manuellement").toInt()
                    if (alimentId == -1) {
                        Toast.makeText(requireContext(), "Erreur lors de l'ajout de l'aliment", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                }

                val rows = dbHelper.updateNourrissage(
                    id = nourrissage.id,
                    quantite = newQuantite,
                    dateNourrissage = selectedDate,
                    idAliment = alimentId.toInt(),
                    idBassin = nourrissage.idBassin
                )
                if (rows > 0) {
                    setupRecyclerView()
                    Toast.makeText(requireContext(), "Nourrissage modifié avec succès", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Erreur lors de la modification", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun confirmDelete(nourrissage: Nourrissage) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Supprimer nourrissage")
            .setMessage("Supprimer le nourrissage du ${nourrissage.dateNourrissage} ?")
            .setPositiveButton("Oui") { _, _ ->
                if (dbHelper.deleteNourrissage(nourrissage.id) > 0) {
                    setupRecyclerView()
                    Toast.makeText(requireContext(), "Nourrissage supprimé", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Non", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Pas de binding à nettoyer
    }
}