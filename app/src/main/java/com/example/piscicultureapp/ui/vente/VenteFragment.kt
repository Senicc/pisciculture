package com.example.piscicultureapp.ui.vente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.text.TextWatcher
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piscicultureapp.DatabaseHelper
import com.example.piscicultureapp.R
import com.example.piscicultureapp.models.Recolte
import com.example.piscicultureapp.models.Vente
import com.example.piscicultureapp.network.SimpleApiClient
import com.example.piscicultureapp.util.CurrencyFormat
import com.google.android.material.button.MaterialButton
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class VenteFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: VenteAdapter
    private val listData = mutableListOf<Vente>()
    private val masterList = mutableListOf<Vente>()
    private lateinit var recyclerVentes: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var btnAddVente: MaterialButton
    private lateinit var tvTotalRevenue: TextView
    private lateinit var tvTotalSold: TextView
    private lateinit var tvTotalRecoltes: TextView

    /** Si true, les ventes affichées viennent de MySQL via l'API locale */
    private var preferRemote: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_vente, container, false)
        dbHelper = DatabaseHelper(requireContext())

        recyclerVentes = view.findViewById(R.id.recyclerVentes)
        searchView = view.findViewById(R.id.searchView)
        btnAddVente = view.findViewById(R.id.btnAddVente)
        tvTotalRevenue = view.findViewById(R.id.tv_total_revenue)
        tvTotalSold = view.findViewById(R.id.tv_total_sold)
        tvTotalRecoltes = TextView(requireContext()).apply {
            text = "Total poissons récoltés: 0"
            textSize = 14f
            setPadding(16, 8, 16, 8)
        }
        view.findViewById<View>(R.id.fab_add_vente).setOnClickListener { showAddVenteDialog() }

        setupRecyclerView()
        setupSearch()
        setupAddButton()
        loadVentes()
        updateTotals()

        return view
    }

    private fun setupRecyclerView() {
        adapter = VenteAdapter(listData,
            onEdit = { showEditDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        recyclerVentes.layoutManager = LinearLayoutManager(requireContext())
        recyclerVentes.adapter = adapter
    }

    private fun loadVentes() {
        thread {
            val remote = fetchVentesFromApi()
            activity?.runOnUiThread {
                preferRemote = remote != null
                val data = remote ?: dbHelper.getAllVentes()
                applyList(data)
            }
        }
    }

    private fun fetchVentesFromApi(): List<Vente>? {
        val resp = SimpleApiClient.getJson("ventes_list.php") ?: return null
        if (!resp.optBoolean("ok", false)) return null
        val arr: JSONArray = resp.optJSONArray("data") ?: return null
        val out = ArrayList<Vente>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Vente(
                    id = o.optInt("ID_vente"),
                    client = o.optString("Client"),
                    quantite = o.optInt("Quantite", 0),
                    idEspece = o.optInt("ID_espece", 0),
                    prixUnitaire = o.optDouble("Prix_unitaire", 0.0).toFloat(),
                    prixTotal = o.optDouble("Prix_total").toFloat(),
                    dateVente = o.optString("Date_vente").takeIf { it.isNotEmpty() },
                    nomEspece = o.optString("Nom_espece", "")
                )
            )
        }
        return out
    }

    private fun fetchRecoltesFromApi(): List<Recolte>? {
        val resp = SimpleApiClient.getJson("recoltes_list.php") ?: return null
        if (!resp.optBoolean("ok", false)) return null
        val arr: JSONArray = resp.optJSONArray("data") ?: return null
        val out = ArrayList<Recolte>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Recolte(
                    id = o.optInt("ID_recolte"),
                    dateRecolte = o.optString("Date_recolte").takeIf { it.isNotEmpty() },
                    quantite = o.optInt("Quantite"),
                    poidsTotal = o.optDouble("Poids_total").toFloat(),
                    idBassin = o.optInt("ID_bassin"),
                    nomBassin = o.optString("Nom_bassin")
                )
            )
        }
        return out
    }

    private fun applyList(data: List<Vente>) {
        masterList.clear()
        masterList.addAll(data)
        listData.clear()
        listData.addAll(data)
        adapter.notifyDataSetChanged()
        updateHeaderTotals()
    }

    private fun updateHeaderTotals() {
        val sum = masterList.sumOf { it.prixTotal.toDouble() }
        tvTotalRevenue.text = CurrencyFormat.formatAriary(sum)
        val qty = masterList.size
        tvTotalSold.text = "$qty vente(s)"
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = if (newText.isNullOrEmpty()) {
                    masterList.toList()
                } else {
                    masterList.filter {
                        (it.client?.contains(newText, ignoreCase = true) == true) ||
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
        btnAddVente.setOnClickListener { showAddVenteDialog() }
    }

    private fun showAddVenteDialog() {
        thread {
            val recoltes = fetchRecoltesFromApi() ?: dbHelper.getAllRecoltes()
            activity?.runOnUiThread {
                if (recoltes.isEmpty()) {
                    Toast.makeText(requireContext(), "Ajoutez d'abord une récolte (MySQL ou local)", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                openAddDialog(recoltes)
            }
        }
    }

    private fun openAddDialog(recoltes: List<Recolte>) {
        val container = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }

        val btnDate = Button(requireContext()).apply {
            text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
        val etClient = EditText(requireContext()).apply {
            hint = "Nom du client"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }
        val etQuantite = EditText(requireContext()).apply {
            hint = "Poids de poisson (kg)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val spinnerEspece = Spinner(requireContext())
        val tvPrixCalcule = TextView(requireContext()).apply {
            text = "Prix calculé: 0 Ar"
            textSize = 16f
            setPadding(0, 16, 0, 0)
            setTextColor(android.graphics.Color.parseColor("#2E7D32"))
        }

        // Obtenir seulement les espèces qui ont été récoltés
        val recoltes = dbHelper.getAllRecoltes()
        val bassinIds = recoltes.map { it.idBassin }.distinct()
        
        // Obtenir les IDs d'espèces des bassins récoltés directement
        val especeIds = mutableListOf<Int>()
        bassinIds.forEach { bassinId ->
            // Obtenir les poissons de ce bassin
            val poissons = dbHelper.getPoissonsByBassin(bassinId)
            // Ajouter les IDs d'espèces de ces poissons
            especeIds.addAll(poissons.map { it.idEspece })
        }
        
        // Dédoublonner et obtenir les espèces uniques
        val especeIdsUniques = especeIds.distinct()
        val especes = especeIdsUniques.mapNotNull { especeId -> dbHelper.getEspeceById(especeId) }
        
        spinnerEspece.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            especes.map { "${it.nomEspece} (${it.prixUnitaire} Ar/kg)" }
        )

        var selectedDate = btnDate.text.toString()
        btnDate.setOnClickListener {
            val c = Calendar.getInstance()
            android.app.DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                btnDate.text = selectedDate
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Mettre à jour le prix calculé quand la quantité ou l'espèce change
        val updatePrix = {
            val quantite = etQuantite.text.toString().toIntOrNull() ?: 0
            val espece = especes.getOrNull(spinnerEspece.selectedItemPosition)
            val prixTotal = quantite * (espece?.prixUnitaire ?: 0f)
            tvPrixCalcule.text = "Prix calculé: ${String.format("%.0f", prixTotal)} Ar"
        }

        etQuantite.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { updatePrix() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        spinnerEspece.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updatePrix()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        container.addView(btnDate)
        container.addView(etClient)
        container.addView(spinnerEspece)
        container.addView(etQuantite)
        container.addView(tvPrixCalcule)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Nouvelle vente")
            .setView(container)
            .setPositiveButton("Enregistrer") { _, _ ->
                val client = etClient.text.toString().trim()
                val quantite = etQuantite.text.toString().toIntOrNull() ?: 0
                val espece = especes.getOrNull(spinnerEspece.selectedItemPosition)

                if (client.isBlank() || quantite <= 0 || espece == null) {
                    Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Validation: vérifier si le poids à vendre ne dépasse pas le poids disponible
                val poidsDisponibleKg = dbHelper.getStockTotalByEspece(espece.id)
                if (quantite > poidsDisponibleKg) {
                    Toast.makeText(requireContext(), "Erreur: Impossible de vendre $quantite kg. Poids disponible: ${String.format("%.1f", poidsDisponibleKg)} kg pour l'espèce ${espece.nomEspece}", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                thread {
                    val okRemote = if (preferRemote) {
                        val body = JSONObject().apply {
                            put("client", client)
                            put("quantite", quantite)
                            put("id_espece", espece.id)
                            put("prix_unitaire", espece.prixUnitaire)
                            put("prix_total", quantite * espece.prixUnitaire)
                            put("date_vente", selectedDate)
                        }
                        val resp = SimpleApiClient.postJson("vente_save.php", body)
                        resp?.optBoolean("ok", false) == true
                    } else false

                    if (!okRemote) {
                        val (id, prixTotal) = dbHelper.addVente(client, quantite, espece.id, selectedDate)
                    }

                    activity?.runOnUiThread {
                        loadVentes()
                        updateTotals()
                        Toast.makeText(
                            requireContext(),
                            if (okRemote) R.string.vente_saved_mysql else R.string.vente_saved_local,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun updateTotals() {
        val totalRecoltes = dbHelper.getTotalPoissonsRecoltes()
        val totalVendus = dbHelper.getTotalPoissonsVendus()
        val restants = totalRecoltes - totalVendus
        
        tvTotalRecoltes.text = "Total poissons récoltés: $totalRecoltes | Vendus: $totalVendus | Restants: $restants"
    }

    private fun showEditDialog(vente: Vente) {
        val newPrix = vente.prixTotal + 50_000f
        thread {
            val okRemote = if (preferRemote) {
                val body = JSONObject().apply {
                    put("id", vente.id)
                    put("client", vente.client ?: "")
                    put("prix_total", newPrix.toDouble())
                    put("date_vente", vente.dateVente ?: "")
                    put("id_espece", vente.idEspece)
                }
                val resp = SimpleApiClient.postJson("vente_save.php", body)
                resp?.optBoolean("ok", false) == true
            } else false

            if (!okRemote) {
                dbHelper.updateVente(
                    vente.id, vente.client ?: "", vente.quantite,
                    vente.idEspece, vente.dateVente ?: ""
                )
            }

            activity?.runOnUiThread {
                loadVentes()
                Toast.makeText(requireContext(), R.string.vente_updated, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDelete(vente: Vente) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Supprimer vente")
            .setMessage("Supprimer la vente pour ${vente.client} ?")
            .setPositiveButton("Oui") { _, _ ->
                thread {
                    if (preferRemote) {
                        val body = JSONObject().put("id", vente.id)
                        SimpleApiClient.postJson("vente_delete.php", body)
                    }
                    dbHelper.deleteVente(vente.id)
                    activity?.runOnUiThread { loadVentes() }
                }
            }
            .setNegativeButton("Non", null)
            .show()
    }
}
