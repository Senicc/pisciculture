package com.example.piscicultureapp.ui.espece

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piscicultureapp.DatabaseHelper
import com.example.piscicultureapp.R
import com.example.piscicultureapp.models.Espece

class EspeceFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: EspeceAdapter
    private var listData = mutableListOf<Espece>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_espece, container, false)
        dbHelper = DatabaseHelper(requireContext())

        setupRecyclerView()
        setupSearch()
        setupAddButton()

        return rootView
    }

    private fun setupRecyclerView() {
        listData.clear()
        listData.addAll(dbHelper.getAllEspeces())

        adapter = EspeceAdapter(listData,
            onEdit = { showEditDialog(it) },
            onDelete = { confirmDelete(it) }
        )

        val recyclerView = rootView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_especes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        val searchView = rootView.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = if (newText.isNullOrEmpty()) {
                    dbHelper.getAllEspeces()
                } else {
                    dbHelper.getAllEspeces().filter {
                        it.nomEspece.contains(newText, ignoreCase = true)
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
        val addBtn = rootView.findViewById<android.widget.Button>(R.id.btn_add_espece)
        addBtn.setOnClickListener {
            val id = dbHelper.addEspece("Tilapia", "Espèce très résistante pour élevage intensif")
            if (id != -1L) {
                setupRecyclerView()
                Toast.makeText(requireContext(), "Espèce ajoutée", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditDialog(espece: Espece) {
        val container = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }

        val etNom = android.widget.EditText(requireContext()).apply {
            hint = "Nom de l'espèce"
            setText(espece.nomEspece)
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }
        
        val etDescription = android.widget.EditText(requireContext()).apply {
            hint = "Description"
            setText(espece.description ?: "")
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            minHeight = 100
        }
        
        val etPrix = android.widget.EditText(requireContext()).apply {
            hint = "Prix unitaire (Ar/kg)"
            setText(espece.prixUnitaire.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        container.addView(etNom)
        container.addView(etDescription)
        container.addView(etPrix)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Modifier l'espèce")
            .setView(container)
            .setPositiveButton("Enregistrer") { _, _ ->
                val nom = etNom.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val prix = etPrix.text.toString().toFloatOrNull() ?: 0f

                if (nom.isBlank() || prix <= 0f) {
                    Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val rows = dbHelper.updateEspece(espece.id, nom, description.ifEmpty { null })
                if (rows > 0) {
                    dbHelper.updatePrixEspece(espece.id, prix)
                    setupRecyclerView()
                    Toast.makeText(requireContext(), "Espèce modifiée avec succès", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Erreur lors de la modification", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun confirmDelete(espece: Espece) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Supprimer espèce")
            .setMessage("Supprimer ${espece.nomEspece} ?")
            .setPositiveButton("Oui") { _, _ ->
                if (dbHelper.deleteEspece(espece.id) > 0) {
                    setupRecyclerView()
                    Toast.makeText(requireContext(), "Espèce supprimée", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Non", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}