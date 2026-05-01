package com.example.piscicultureapp.ui.qualite

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.piscicultureapp.DatabaseHelper
import com.example.piscicultureapp.R
import com.example.piscicultureapp.models.QualiteEau
import java.util.Calendar

class QualiteEauFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: QualiteEauAdapter
    private var listData = mutableListOf<QualiteEau>()
    
    // Dashboard TextViews
    private lateinit var tvAvgTemperature: TextView
    private lateinit var tvAvgPH: TextView
    private lateinit var tvAvgOxygene: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_qualite_eau, container, false)
        dbHelper = DatabaseHelper(requireContext())

        // Initialisation des TextView du dashboard
        tvAvgTemperature = rootView.findViewById(R.id.tv_avg_temperature)
        tvAvgPH = rootView.findViewById(R.id.tv_avg_ph)
        tvAvgOxygene = rootView.findViewById(R.id.tv_avg_oxygene)

        setupRecyclerView()
        setupSearch()
        setupAddButton()
        updateDashboard()

        return rootView
    }

    private fun setupRecyclerView() {
        listData.clear()
        listData.addAll(dbHelper.getAllQualiteEau())

        adapter = QualiteEauAdapter(listData,
            onEdit = { showEditDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        val recyclerView = rootView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_qualite)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        val searchView = rootView.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = if (newText.isNullOrEmpty()) {
                    dbHelper.getAllQualiteEau()
                } else {
                    dbHelper.getAllQualiteEau().filter {
                        it.nomBassin.contains(newText, ignoreCase = true)
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
        val addBtn = rootView.findViewById<android.widget.Button>(R.id.btn_add_qualite)
        addBtn.setOnClickListener {
            showAddQualiteDialog()
        }
    }

    private fun showAddQualiteDialog() {
        val bassins = dbHelper.getAllBassins()
        if (bassins.isEmpty()) {
            Toast.makeText(requireContext(), "Ajoutez d'abord un bassin", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_qualite, null)
        val btnDate = dialogView.findViewById<Button>(R.id.btn_select_date)
        val spinnerBassin = dialogView.findViewById<Spinner>(R.id.spinner_bassin)
        val etTemp = dialogView.findViewById<EditText>(R.id.et_temperature)
        val etPh = dialogView.findViewById<EditText>(R.id.et_ph)
        val etOxy = dialogView.findViewById<EditText>(R.id.et_oxygene)

        var selectedDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        btnDate.text = selectedDate
        btnDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                btnDate.text = selectedDate
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        spinnerBassin.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, bassins.map { it.nomBassin })

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Ajouter une mesure")
            .setView(dialogView)
            .setPositiveButton("Enregistrer") { _, _ ->
                val temp = etTemp.text.toString().toFloatOrNull() ?: 0f
                val ph = etPh.text.toString().toFloatOrNull() ?: 0f
                val oxy = etOxy.text.toString().toFloatOrNull() ?: 0f

                if (temp <= 0f || ph <= 0f || oxy <= 0f) {
                    Toast.makeText(requireContext(), "Valeurs invalides", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val bassin = bassins[spinnerBassin.selectedItemPosition]
                val id = dbHelper.addQualiteEau(temp, ph, oxy, selectedDate, bassin.id)
                if (id != -1L) {
                    setupRecyclerView()
                    Toast.makeText(requireContext(), "Mesure ajoutee", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showEditDialog(qualite: QualiteEau) {
        val bassins = dbHelper.getAllBassins()
        if (bassins.isEmpty()) {
            Toast.makeText(requireContext(), "Aucun bassin disponible", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_qualite, null)
        val btnDate = dialogView.findViewById<Button>(R.id.btn_select_date)
        val spinnerBassin = dialogView.findViewById<Spinner>(R.id.spinner_bassin)
        val etTemp = dialogView.findViewById<EditText>(R.id.et_temperature)
        val etPh = dialogView.findViewById<EditText>(R.id.et_ph)
        val etOxy = dialogView.findViewById<EditText>(R.id.et_oxygene)

        // Pré-remplir avec les valeurs existantes
        var selectedDate = qualite.dateMesure ?: java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        btnDate.text = selectedDate
        etTemp.setText(qualite.temperature.toString())
        etPh.setText(qualite.ph.toString())
        etOxy.setText(qualite.oxygene.toString())

        // Sélectionner le bassin actuel
        val bassinNames = bassins.map { it.nomBassin }
        spinnerBassin.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, bassinNames)
        val currentBassinIndex = bassinNames.indexOfFirst { it == qualite.nomBassin }
        if (currentBassinIndex >= 0) {
            spinnerBassin.setSelection(currentBassinIndex)
        }

        btnDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                btnDate.text = selectedDate
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Modifier la mesure")
            .setView(dialogView)
            .setPositiveButton("Enregistrer") { _, _ ->
                val temp = etTemp.text.toString().toFloatOrNull() ?: 0f
                val ph = etPh.text.toString().toFloatOrNull() ?: 0f
                val oxy = etOxy.text.toString().toFloatOrNull() ?: 0f

                if (temp <= 0f || ph <= 0f || oxy <= 0f) {
                    Toast.makeText(requireContext(), "Valeurs invalides", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val bassin = bassins[spinnerBassin.selectedItemPosition]
                val rows = dbHelper.updateQualiteEau(
                    qualite.id, temp, ph, oxy, selectedDate, bassin.id
                )
                if (rows > 0) {
                    setupRecyclerView()
                    Toast.makeText(requireContext(), "Mesure modifiée avec succès", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Erreur lors de la modification", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun confirmDelete(qualite: QualiteEau) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Supprimer mesure")
            .setMessage("Supprimer la mesure du ${qualite.dateMesure} ?")
            .setPositiveButton("Oui") { _, _ ->
                if (dbHelper.deleteQualiteEau(qualite.id) > 0) {
                    setupRecyclerView()
                    Toast.makeText(requireContext(), "Mesure supprimée", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun updateDashboard() {
        try {
            val stats = dbHelper.getQualiteEauStats()
            
            tvAvgTemperature.text = "${String.format("%.1f", stats["avgTemperature"] as Float)}°C"
            tvAvgPH.text = String.format("%.1f", stats["avgPH"] as Float)
            tvAvgOxygene.text = "${String.format("%.1f", stats["avgOxygene"] as Float)}mg/L"
        } catch (e: Exception) {
            tvAvgTemperature.text = "0°C"
            tvAvgPH.text = "0.0"
            tvAvgOxygene.text = "0mg/L"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Pas de binding à nettoyer
    }
}