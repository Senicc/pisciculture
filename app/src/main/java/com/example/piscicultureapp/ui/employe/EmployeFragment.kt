package com.example.piscicultureapp.ui.employe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piscicultureapp.DatabaseHelper
import com.example.piscicultureapp.R
import com.example.piscicultureapp.models.Employe

class EmployeFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: EmployeAdapter
    private var listData = mutableListOf<Employe>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_employe, container, false)
        dbHelper = DatabaseHelper(requireContext())

        setupRecyclerView()
        setupAddButton()

        return rootView
    }

    private fun setupRecyclerView() {
        listData.clear()
        listData.addAll(dbHelper.getAllEmployes())

        adapter = EmployeAdapter(listData,
            onEdit = { showEditDialog(it) },
            onDelete = { confirmDelete(it) }
        )

        val recyclerView = rootView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_employes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupAddButton() {
        val addBtn = rootView.findViewById<android.widget.Button>(R.id.btn_add_employe)
        addBtn.setOnClickListener {
            val id = dbHelper.addEmploye(
                nom = "Rakoto",
                prenom = "Jean",
                role = "Pisciculteur",
                telephone = "0341234567"
            )
            if (id != -1L) {
                setupRecyclerView()
                Toast.makeText(requireContext(), "Employé ajouté", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditDialog(employe: Employe) {
        val newName = "${employe.nom} (Modifié)"
        val rows = dbHelper.updateEmploye(
            employe.id, newName, employe.prenom, employe.role, employe.telephone
        )
        if (rows > 0) {
            setupRecyclerView()
            Toast.makeText(requireContext(), "Employé modifié", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDelete(employe: Employe) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Supprimer")
            .setMessage("Supprimer ${employe.nom} ${employe.prenom} ?")
            .setPositiveButton("Oui") { _, _ ->
                if (dbHelper.deleteEmploye(employe.id) > 0) {
                    setupRecyclerView()
                    Toast.makeText(requireContext(), "Employé supprimé", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Non", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}