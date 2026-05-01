package com.example.piscicultureapp.sync

import android.content.Context
import android.util.Log
import com.example.piscicultureapp.DatabaseHelper
import com.example.piscicultureapp.models.*
import com.example.piscicultureapp.network.MySqlApiConfig
import com.example.piscicultureapp.network.SimpleApiClient
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

class DataSyncManager(private val context: Context) {
    
    private val dbHelper = DatabaseHelper(context)
    private val TAG = "DataSyncManager"
    
    sealed class SyncResult {
        object Success : SyncResult()
        data class Error(val message: String) : SyncResult()
        data class Partial(val successCount: Int, val totalCount: Int) : SyncResult()
    }
    
    /**
     * Synchronise toutes les données locales vers MySQL
     */
    fun syncAllToMySQL(callback: (SyncResult) -> Unit) {
        thread {
            try {
                var successCount = 0
                var totalCount = 0
                
                // Synchroniser les bassins
                val bassinsResult = syncBassinsToMySQL()
                totalCount += bassinsResult.second
                successCount += bassinsResult.first
                
                // Synchroniser les espèces
                val especesResult = syncEspecesToMySQL()
                totalCount += especesResult.second
                successCount += especesResult.first
                
                // Synchroniser les poissons
                val poissonsResult = syncPoissonsToMySQL()
                totalCount += poissonsResult.second
                successCount += poissonsResult.first
                
                // Synchroniser les aliments
                val alimentsResult = syncAlimentationsToMySQL()
                totalCount += alimentsResult.second
                successCount += alimentsResult.first
                
                // Synchroniser les nourrissages
                val nourrissagesResult = syncNourrissagesToMySQL()
                totalCount += nourrissagesResult.second
                successCount += nourrissagesResult.first
                
                // Synchroniser les ventes
                val ventesResult = syncVentesToMySQL()
                totalCount += ventesResult.second
                successCount += ventesResult.first
                
                // Synchroniser les récoltes
                val recoltesResult = syncRecoltesToMySQL()
                totalCount += recoltesResult.second
                successCount += recoltesResult.first
                
                if (successCount == totalCount) {
                    callback(SyncResult.Success)
                } else {
                    callback(SyncResult.Partial(successCount, totalCount))
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de la synchronisation: ${e.message}", e)
                callback(SyncResult.Error("Erreur de synchronisation: ${e.message}"))
            }
        }
    }
    
    /**
     * Synchronise toutes les données depuis MySQL vers SQLite
     */
    fun syncAllFromMySQL(callback: (SyncResult) -> Unit) {
        thread {
            try {
                var successCount = 0
                var totalCount = 7 // Nombre de tables à synchroniser
                
                // Synchroniser depuis MySQL
                if (syncBassinsFromMySQL()) successCount++
                if (syncEspecesFromMySQL()) successCount++
                if (syncPoissonsFromMySQL()) successCount++
                if (syncAlimentationsFromMySQL()) successCount++
                if (syncNourrissagesFromMySQL()) successCount++
                if (syncVentesFromMySQL()) successCount++
                if (syncRecoltesFromMySQL()) successCount++
                
                if (successCount == totalCount) {
                    callback(SyncResult.Success)
                } else {
                    callback(SyncResult.Partial(successCount, totalCount))
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de la synchronisation depuis MySQL: ${e.message}", e)
                callback(SyncResult.Error("Erreur de synchronisation: ${e.message}"))
            }
        }
    }
    
    // === MÉTHODES DE SYNCHRONISATION VERS MYSQL ===
    
    private fun syncBassinsToMySQL(): Pair<Int, Int> {
        val bassins = dbHelper.getAllBassins(true)
        var successCount = 0
        
        bassins.forEach { bassin ->
            try {
                val jsonData = JSONObject().apply {
                    put("id_bassin", bassin.id)
                    put("nom_bassin", bassin.nomBassin)
                    put("capacite", bassin.capacite)
                    put("type_bassin", bassin.typeBassin ?: "")
                    put("localisation", bassin.localisation ?: "")
                    put("etat", bassin.etat)
                }
                
                val response = SimpleApiClient.postJson("bassin_save.php", jsonData)
                if (response?.optBoolean("ok", false) == true) {
                    successCount++
                    dbHelper.markAsSynced("BASSIN", "ID_bassin", bassin.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erreur synchronisation bassin ${bassin.id}: ${e.message}")
            }
        }
        
        return Pair(successCount, bassins.size)
    }
    
    private fun syncEspecesToMySQL(): Pair<Int, Int> {
        val especes = dbHelper.getAllEspeces(true)
        var successCount = 0
        
        especes.forEach { espece ->
            try {
                val jsonData = JSONObject().apply {
                    put("id_espece", espece.id)
                    put("nom_espece", espece.nomEspece)
                    put("description", espece.description ?: "")
                    put("prix_unitaire", espece.prixUnitaire)
                }
                
                val response = SimpleApiClient.postJson("espece_save.php", jsonData)
                if (response?.optBoolean("ok", false) == true) {
                    successCount++
                    dbHelper.markAsSynced("ESPECE", "ID_espece", espece.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erreur synchronisation espece ${espece.id}: ${e.message}")
            }
        }
        
        return Pair(successCount, especes.size)
    }
    
    private fun syncPoissonsToMySQL(): Pair<Int, Int> {
        val poissons = dbHelper.getAllPoissons(true)
        var successCount = 0
        
        poissons.forEach { poisson ->
            try {
                val jsonData = JSONObject().apply {
                    put("id_poisson", poisson.id)
                    put("quantite", poisson.quantite)
                    put("date_introduction", poisson.dateIntroduction ?: "")
                    put("poids_moyen", poisson.poidsMoyen ?: 0f)
                    put("mortalite", poisson.mortalite)
                    put("id_espece", poisson.idEspece)
                    put("id_bassin", poisson.idBassin)
                }
                
                val response = SimpleApiClient.postJson("poisson_save.php", jsonData)
                if (response?.optBoolean("ok", false) == true) {
                    successCount++
                    dbHelper.markAsSynced("POISSON", "ID_poisson", poisson.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erreur synchronisation poisson ${poisson.id}: ${e.message}")
            }
        }
        
        return Pair(successCount, poissons.size)
    }
    
    private fun syncAlimentationsToMySQL(): Pair<Int, Int> {
        val aliments = dbHelper.getAllAlimentations(true)
        var successCount = 0
        
        aliments.forEach { aliment ->
            try {
                val jsonData = JSONObject().apply {
                    put("id_aliment", aliment.id)
                    put("nom_aliment", aliment.nomAliment)
                    put("type_aliment", aliment.typeAliment ?: "")
                    put("stock", aliment.stock)
                }
                
                val response = SimpleApiClient.postJson("aliment_save.php", jsonData)
                if (response?.optBoolean("ok", false) == true) {
                    successCount++
                    dbHelper.markAsSynced("ALIMENTATION", "ID_aliment", aliment.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erreur synchronisation aliment ${aliment.id}: ${e.message}")
            }
        }
        
        return Pair(successCount, aliments.size)
    }
    
    private fun syncNourrissagesToMySQL(): Pair<Int, Int> {
        val nourrissages = dbHelper.getAllNourrissages(true)
        var successCount = 0
        
        nourrissages.forEach { nourrissage ->
            try {
                val jsonData = JSONObject().apply {
                    put("id_nourrissage", nourrissage.id)
                    put("date_nourrissage", nourrissage.dateNourrissage ?: "")
                    put("quantite", nourrissage.quantite)
                    put("id_bassin", nourrissage.idBassin)
                    put("id_aliment", nourrissage.idAliment)
                }
                
                val response = SimpleApiClient.postJson("nourrissage_save.php", jsonData)
                if (response?.optBoolean("ok", false) == true) {
                    successCount++
                    dbHelper.markAsSynced("NOURRISSAGE", "ID_nourrissage", nourrissage.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erreur synchronisation nourrissage ${nourrissage.id}: ${e.message}")
            }
        }
        
        return Pair(successCount, nourrissages.size)
    }
    
    private fun syncVentesToMySQL(): Pair<Int, Int> {
        val ventes = dbHelper.getAllVentes(true)
        var successCount = 0
        
        ventes.forEach { vente ->
            try {
                val jsonData = JSONObject().apply {
                    put("id_vente", vente.id)
                    put("client", vente.client)
                    put("quantite", vente.quantite)
                    put("id_espece", vente.idEspece)
                    put("prix_unitaire", vente.prixUnitaire)
                    put("prix_total", vente.prixTotal)
                    put("date_vente", vente.dateVente ?: "")
                }
                
                val response = SimpleApiClient.postJson("vente_save.php", jsonData)
                if (response?.optBoolean("ok", false) == true) {
                    successCount++
                    dbHelper.markAsSynced("VENTE", "ID_vente", vente.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erreur synchronisation vente ${vente.id}: ${e.message}")
            }
        }
        
        return Pair(successCount, ventes.size)
    }
    
    private fun syncRecoltesToMySQL(): Pair<Int, Int> {
        val recoltes = dbHelper.getAllRecoltes(true)
        var successCount = 0
        
        recoltes.forEach { recolte ->
            try {
                val jsonData = JSONObject().apply {
                    put("id_recolte", recolte.id)
                    put("date_recolte", recolte.dateRecolte ?: "")
                    put("quantite", recolte.quantite)
                    put("poids_total", recolte.poidsTotal)
                    put("id_bassin", recolte.idBassin)
                }
                
                val response = SimpleApiClient.postJson("recolte_save.php", jsonData)
                if (response?.optBoolean("ok", false) == true) {
                    successCount++
                    dbHelper.markAsSynced("RECOLTE", "ID_recolte", recolte.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erreur synchronisation récolte ${recolte.id}: ${e.message}")
            }
        }
        
        return Pair(successCount, recoltes.size)
    }
    
    // === MÉTHODES DE SYNCHRONISATION DEPUIS MYSQL ===
    
    private fun syncBassinsFromMySQL(): Boolean {
        return try {
            val response = SimpleApiClient.getJson("bassins_list.php")
            val bassinsArray = response?.optJSONArray("data") ?: JSONArray()
            
            for (i in 0 until bassinsArray.length()) {
                val bassinJson = bassinsArray.getJSONObject(i)
                val bassin = Bassin(
                    id = bassinJson.getInt("id_bassin"),
                    nomBassin = bassinJson.getString("nom_bassin"),
                    capacite = bassinJson.getInt("capacite"),
                    typeBassin = bassinJson.optString("type_bassin", ""),
                    localisation = bassinJson.optString("localisation", ""),
                    etat = bassinJson.optString("etat", "actif")
                )
                
                // Mettre à jour ou insérer dans SQLite
                dbHelper.updateBassin(
                    bassin.id, bassin.nomBassin, bassin.capacite, 
                    bassin.typeBassin, bassin.localisation
                )
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur sync bassins depuis MySQL: ${e.message}")
            false
        }
    }
    
    private fun syncEspecesFromMySQL(): Boolean {
        return try {
            val response = SimpleApiClient.getJson("especes_list.php")
            val especesArray = response?.optJSONArray("data") ?: JSONArray()
            
            for (i in 0 until especesArray.length()) {
                val especeJson = especesArray.getJSONObject(i)
                val espece = Espece(
                    id = especeJson.getInt("id_espece"),
                    nomEspece = especeJson.getString("nom_espece"),
                    description = especeJson.optString("description", ""),
                    prixUnitaire = especeJson.optDouble("prix_unitaire", 0.0).toFloat()
                )
                
                dbHelper.updateEspece(espece.id, espece.nomEspece, espece.description)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur sync espèces depuis MySQL: ${e.message}")
            false
        }
    }
    
    private fun syncPoissonsFromMySQL(): Boolean {
        return try {
            val response = SimpleApiClient.getJson("poissons_list.php")
            val poissonsArray = response?.optJSONArray("data") ?: JSONArray()
            
            for (i in 0 until poissonsArray.length()) {
                val poissonJson = poissonsArray.getJSONObject(i)
                val poisson = Poisson(
                    id = poissonJson.getInt("id_poisson"),
                    quantite = poissonJson.getInt("quantite"),
                    dateIntroduction = poissonJson.optString("date_introduction", ""),
                    poidsMoyen = poissonJson.optDouble("poids_moyen", 0.0).toFloat(),
                    mortalite = poissonJson.optInt("mortalite", 0),
                    idEspece = poissonJson.getInt("id_espece"),
                    idBassin = poissonJson.getInt("id_bassin")
                )
                
                dbHelper.updatePoisson(
                    poisson.id, poisson.quantite, poisson.dateIntroduction,
                    poisson.poidsMoyen, poisson.mortalite, poisson.idEspece, poisson.idBassin
                )
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur sync poissons depuis MySQL: ${e.message}")
            false
        }
    }
    
    private fun syncAlimentationsFromMySQL(): Boolean {
        return try {
            val response = SimpleApiClient.getJson("aliments_list.php")
            val alimentsArray = response?.optJSONArray("data") ?: JSONArray()
            
            for (i in 0 until alimentsArray.length()) {
                val alimentJson = alimentsArray.getJSONObject(i)
                val aliment = Alimentation(
                    id = alimentJson.getInt("id_aliment"),
                    nomAliment = alimentJson.getString("nom_aliment"),
                    typeAliment = alimentJson.optString("type_aliment", ""),
                    stock = alimentJson.optDouble("stock", 0.0).toFloat()
                )
                
                dbHelper.updateAlimentation(
                    aliment.id, aliment.nomAliment, aliment.typeAliment ?: "",
                    aliment.stock
                )
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur sync aliments depuis MySQL: ${e.message}")
            false
        }
    }
    
    private fun syncNourrissagesFromMySQL(): Boolean {
        return try {
            val response = SimpleApiClient.getJson("nourrissages_list.php")
            val nourrissagesArray = response?.optJSONArray("data") ?: JSONArray()
            
            for (i in 0 until nourrissagesArray.length()) {
                val nourrissageJson = nourrissagesArray.getJSONObject(i)
                val nourrissage = Nourrissage(
                    id = nourrissageJson.getInt("id_nourrissage"),
                    dateNourrissage = nourrissageJson.optString("date_nourrissage", ""),
                    quantite = nourrissageJson.optDouble("quantite", 0.0).toFloat(),
                    idBassin = nourrissageJson.getInt("id_bassin"),
                    idAliment = nourrissageJson.getInt("id_aliment")
                )
                
                dbHelper.updateNourrissage(
                    nourrissage.id, nourrissage.quantite, 
                    nourrissage.dateNourrissage ?: "", nourrissage.idAliment, nourrissage.idBassin
                )
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur sync nourrissages depuis MySQL: ${e.message}")
            false
        }
    }
    
    private fun syncVentesFromMySQL(): Boolean {
        return try {
            val response = SimpleApiClient.getJson("ventes_list.php")
            val ventesArray = response?.optJSONArray("data") ?: JSONArray()
            
            for (i in 0 until ventesArray.length()) {
                val venteJson = ventesArray.getJSONObject(i)
                val vente = Vente(
                    id = venteJson.getInt("id_vente"),
                    client = venteJson.getString("client"),
                    quantite = venteJson.getInt("quantite"),
                    idEspece = venteJson.getInt("id_espece"),
                    prixUnitaire = venteJson.optDouble("prix_unitaire", 0.0).toFloat(),
                    prixTotal = venteJson.optDouble("prix_total", 0.0).toFloat(),
                    dateVente = venteJson.optString("date_vente", "")
                )
                
                dbHelper.updateVente(
                    vente.id, vente.client, vente.quantite, 
                    vente.idEspece, vente.dateVente ?: ""
                )
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur sync ventes depuis MySQL: ${e.message}")
            false
        }
    }
    
    private fun syncRecoltesFromMySQL(): Boolean {
        return try {
            val response = SimpleApiClient.getJson("recoltes_list.php")
            val recoltesArray = response?.optJSONArray("data") ?: JSONArray()
            
            for (i in 0 until recoltesArray.length()) {
                val recolteJson = recoltesArray.getJSONObject(i)
                val recolte = Recolte(
                    id = recolteJson.getInt("id_recolte"),
                    dateRecolte = recolteJson.optString("date_recolte", ""),
                    quantite = recolteJson.getInt("quantite"),
                    poidsTotal = recolteJson.optDouble("poids_total", 0.0).toFloat(),
                    idBassin = recolteJson.getInt("id_bassin")
                )
                
                dbHelper.updateRecolte(
                    recolte.id, recolte.dateRecolte ?: "", 
                    recolte.quantite, recolte.poidsTotal, recolte.idBassin
                )
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur sync récoltes depuis MySQL: ${e.message}")
            false
        }
    }
    
    /**
     * Vérifie la connexion avec MySQL
     */
    fun checkMySQLConnection(): Boolean {
        return try {
            val response = SimpleApiClient.getJson("health.php")
            response?.optBoolean("success", false) == true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur connexion MySQL: ${e.message}")
            false
        }
    }
}
