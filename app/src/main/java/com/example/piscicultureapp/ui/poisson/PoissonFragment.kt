package com.example.piscicultureapp.ui.poisson

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.piscicultureapp.DatabaseHelper
import com.example.piscicultureapp.R
import com.example.piscicultureapp.models.Poisson

class PoissonFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: PoissonAdapter
    private var listData = mutableListOf<Poisson>()
    private lateinit var tvTotalStock: TextView
    private lateinit var tvAvgWeight: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_poisson, container, false)
        dbHelper = DatabaseHelper(requireContext())

        // Initialisation des TextView du dashboard
        tvTotalStock = rootView.findViewById(R.id.tv_total_stock)
        tvAvgWeight = rootView.findViewById(R.id.tv_avg_weight)

        setupRecyclerView()
        setupSearch()
        setupAddButton()
        setupTransferButton()
        updateDashboard()

        return rootView
    }

    private fun setupTransferButton() {
        try {
            rootView.findViewById<android.widget.Button>(R.id.btn_transfer_poisson).setOnClickListener {
                Toast.makeText(requireContext(), "Pour transférer des poissons, utilisez le bouton de transfert (icône flèche) sur un lot spécifique dans la liste ci-dessous.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {}
    }

    private fun setupRecyclerView() {
        listData.clear()
        listData.addAll(dbHelper.getAllPoissons())

        adapter = PoissonAdapter(listData,
            onEdit = { showEditDialog(it) },
            onDelete = { confirmDelete(it) },
            onEditPrice = { showEditPriceDialog(it) },
            onTransfer = { showTransferDialog(it) }
        )
        val recyclerView = rootView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_poissons)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun updateDashboard() {
        try {
            // Calcul du stock total en kg (quantité × poids moyen en grammes / 1000)
            val stockTotalKg = dbHelper.getStockTotal()
            val poidsMoyenGlobal = dbHelper.getPoidsMoyenGlobal()
            
            tvTotalStock.text = "${String.format("%.1f", stockTotalKg)} kg"
            val poidsMoyenGrammes = poidsMoyenGlobal * 1000
            tvAvgWeight.text = when {
                poidsMoyenGrammes >= 1000 -> "${String.format("%.1f", poidsMoyenGrammes / 1000)} kg"
                poidsMoyenGrammes >= 1 -> "${String.format("%.0f", poidsMoyenGrammes)} g"
                else -> "< 1 g"
            }
        } catch (e: Exception) {
            tvTotalStock.text = "0 kg"
            tvAvgWeight.text = "0 g"
        }
    }

    private fun setupSearch() {
        val searchView = rootView.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = if (newText.isNullOrEmpty()) {
                    dbHelper.getAllPoissons()
                } else {
                    dbHelper.getAllPoissons().filter {
                        it.nomEspece.contains(newText, ignoreCase = true) ||
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
        val addBtn = rootView.findViewById<android.widget.Button>(R.id.btn_add_poisson)
        addBtn.setOnClickListener {
            showAddPoissonDialog()
        }
    }

    private fun showAddPoissonDialog() {
        val bassins = dbHelper.getAllBassins()

        if (bassins.isEmpty()) {
            Toast.makeText(requireContext(), "Ajoutez d'abord des bassins", Toast.LENGTH_SHORT).show()
            return
        }

        val container = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }

        val etQuantite = EditText(requireContext()).apply {
            hint = "Quantité de poissons"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val etPoids = EditText(requireContext()).apply {
            hint = "Poids moyen (g)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val spinnerEspece = Spinner(requireContext())
        val especes = dbHelper.getAllEspeces()
        
        spinnerEspece.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            especes.map { "${it.nomEspece} (${it.prixUnitaire} Ar/kg)" }
        )
        val etMortalite = EditText(requireContext()).apply {
            hint = "Nombre de poissons morts (0 si aucun)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText("0")
        }
        val spinnerBassin = Spinner(requireContext())

        spinnerBassin.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            bassins.map { it.nomBassin }
        )

        container.addView(spinnerEspece)
        container.addView(etQuantite)
        container.addView(etPoids)
        container.addView(etMortalite)
        container.addView(spinnerBassin)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Ajouter un lot de poissons")
            .setView(container)
            .setPositiveButton("Ajouter") { _, _ ->
                val quantite = etQuantite.text.toString().toIntOrNull() ?: 0
                val poids = etPoids.text.toString().toFloatOrNull() ?: 0f
                val selectedEspece = especes[spinnerEspece.selectedItemPosition]
                val mortalite = etMortalite.text.toString().toIntOrNull() ?: 0

                if (quantite <= 0 || poids <= 0f) {
                    Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (mortalite < 0 || mortalite > quantite) {
                    Toast.makeText(requireContext(), "Nombre de morts invalide", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val bassin = bassins[spinnerBassin.selectedItemPosition]
                val especeId = selectedEspece.id
                
                val (id, capacityExceeded) = dbHelper.addPoisson(
                    quantite = quantite,
                    dateIntroduction = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                    poidsMoyen = poids,
                    mortalite = mortalite,
                    idEspece = especeId.toInt(),
                    idBassin = bassin.id
                )
                if (capacityExceeded) {
                    Toast.makeText(requireContext(), "⚠️ Capacité du bassin dépassée!", Toast.LENGTH_LONG).show()
                } else if (id != -1L) {
                    setupRecyclerView()
                    updateDashboard()
                    Toast.makeText(requireContext(), "Lot de poissons ajouté avec succès", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showEditDialog(poisson: Poisson) {
        val container = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }

        val etQuantite = EditText(requireContext()).apply {
            hint = "Nouvelle quantité"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(poisson.quantite.toString())
        }
        val etPoids = EditText(requireContext()).apply {
            hint = "Nouveau poids moyen (g)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(poisson.poidsMoyen.toString())
        }
        val etMortalite = EditText(requireContext()).apply {
            hint = "Nombre de poissons morts"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(poisson.mortalite.toString())
        }

        container.addView(etQuantite)
        container.addView(etPoids)
        container.addView(etMortalite)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Modifier le lot de poissons")
            .setMessage("Lot actuel: ${poisson.nomEspece} - ${poisson.quantite} poissons")
            .setView(container)
            .setPositiveButton("Modifier") { _, _ ->
                val newQuantite = etQuantite.text.toString().toIntOrNull() ?: poisson.quantite
                val newPoids = etPoids.text.toString().toFloatOrNull() ?: poisson.poidsMoyen
                val newMortalite = etMortalite.text.toString().toIntOrNull() ?: poisson.mortalite

                if (newQuantite <= 0 || newPoids == null || newPoids <= 0f) {
                    Toast.makeText(requireContext(), "Valeurs invalides", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newMortalite < 0 || newMortalite > newQuantite) {
                    Toast.makeText(requireContext(), "Nombre de morts invalide", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val rows = dbHelper.updatePoisson(
                    id = poisson.id,
                    quantite = newQuantite,
                    dateIntroduction = poisson.dateIntroduction,
                    poidsMoyen = newPoids,
                    mortalite = newMortalite,
                    idEspece = poisson.idEspece,
                    idBassin = poisson.idBassin
                )

                if (rows > 0) {
                    setupRecyclerView()
                    updateDashboard()
                    Toast.makeText(requireContext(), "Lot modifié avec succès", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Erreur lors de la modification", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun confirmDelete(poisson: Poisson) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Supprimer lot")
            .setMessage("Supprimer ce lot de ${poisson.nomEspece} (${poisson.quantite} poissons) ?")
            .setPositiveButton("Oui") { _, _ ->
                if (dbHelper.deletePoisson(poisson.id) > 0) {
                    setupRecyclerView()
                    updateDashboard()
                    Toast.makeText(requireContext(), "Lot supprimé", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun showEditPriceDialog(poisson: Poisson) {
        // Obtenir les informations de l'espèce du poisson
        val espece = dbHelper.getEspeceById(poisson.idEspece)
        if (espece == null) {
            Toast.makeText(requireContext(), "Espèce non trouvée", Toast.LENGTH_SHORT).show()
            return
        }

        val container = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }

        val etPrix = android.widget.EditText(requireContext()).apply {
            hint = "Prix unitaire (Ar/kg)"
            setText(espece.prixUnitaire.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val tvInfo = android.widget.TextView(requireContext()).apply {
            text = "Espèce: ${espece.nomEspece}"
            textSize = 16f
            setPadding(0, 0, 0, 16)
        }

        container.addView(tvInfo)
        container.addView(etPrix)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Modifier le prix unitaire")
            .setView(container)
            .setPositiveButton("Enregistrer") { _, _ ->
                val prix = etPrix.text.toString().toFloatOrNull() ?: 0f

                if (prix <= 0f) {
                    Toast.makeText(requireContext(), "Veuillez entrer un prix valide", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val rows = dbHelper.updatePrixEspece(espece.id, prix)
                if (rows > 0) {
                    setupRecyclerView()
                    Toast.makeText(requireContext(), "Prix modifié avec succès", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Erreur lors de la modification", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showTransferDialog(poisson: Poisson) {
        try {
            val bassins = dbHelper.getAllBassins()
            if (bassins.size < 2) {
                Toast.makeText(requireContext(), "Il faut au moins 2 bassins pour effectuer un transfert", Toast.LENGTH_SHORT).show()
                return
            }

            val container = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(48, 24, 48, 8)
            }

            // Spinner pour sélectionner le bassin de destination
            val spinnerBassin = android.widget.Spinner(requireContext())
            val bassinsDestination = bassins.filter { it.id != poisson.idBassin }
            
            spinnerBassin.adapter = android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                bassinsDestination.map { "${it.nomBassin} (Capacité restante: ${dbHelper.getBassinCapaciteRestante(it.id)})" }
            )

            // Champ pour la quantité à transférer
            val etQuantite = android.widget.EditText(requireContext()).apply {
                hint = "Quantité à transférer"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                setText(poisson.quantite.toString())
            }

            // Information sur le poisson
            val tvInfo = android.widget.TextView(requireContext()).apply {
                text = "Transférer depuis: ${poisson.nomBassin}\nEspèce: ${poisson.nomEspece}\nQuantité disponible: ${poisson.quantite} poissons\n\n⚠️ Si un lot de la même espèce existe déjà dans le bassin de destination, ils seront combinés automatiquement."
                textSize = 14f
                setPadding(0, 0, 0, 16)
            }

            container.addView(tvInfo)
            container.addView(spinnerBassin)
            container.addView(etQuantite)

            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Transférer des poissons")
                .setMessage("Sélectionnez le bassin de destination et la quantité")
                .setView(container)
                .setPositiveButton("Transférer") { _, _ ->
                    val quantite = etQuantite.text.toString().toIntOrNull() ?: 0
                    
                    if (quantite <= 0) {
                        Toast.makeText(requireContext(), "Veuillez entrer une quantité valide", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    if (quantite > poisson.quantite) {
                        Toast.makeText(requireContext(), "Quantité supérieure à la quantité disponible", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    if (spinnerBassin.selectedItemPosition >= bassinsDestination.size) {
                        Toast.makeText(requireContext(), "Sélection de bassin invalide", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val bassinDestination = bassinsDestination[spinnerBassin.selectedItemPosition]
                    
                    // Vérifier la capacité du bassin de destination
                    val capaciteRestante = dbHelper.getBassinCapaciteRestante(bassinDestination.id)
                    if (quantite > capaciteRestante) {
                        Toast.makeText(requireContext(), "Capacité insuffisante dans le bassin de destination (${capaciteRestante} places restantes)", Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    val success = dbHelper.transferPoisson(poisson.id, bassinDestination.id, quantite)
                    if (success) {
                        setupRecyclerView()
                        updateDashboard()
                        Toast.makeText(requireContext(), "Transfert réussi: $quantite poissons vers ${bassinDestination.nomBassin}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Erreur lors du transfert", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Pas de binding à nettoyer
    }
}