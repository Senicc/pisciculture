package com.example.piscicultureapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.example.piscicultureapp.models.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "pisciculture.db"
        const val DATABASE_VERSION = 3 // Incrémenté pour la synchronisation (isSynced)
    }

    private val mContext = context

    // ✅ Activation propre des Foreign Keys
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_BASSIN)
        db.execSQL(CREATE_ESPECE)
        db.execSQL(CREATE_POISSON)
        db.execSQL(CREATE_ALIMENTATION)
        db.execSQL(CREATE_NOURRISSAGE)
        db.execSQL(CREATE_QUALITE_EAU)
        db.execSQL(CREATE_EMPLOYE)
        db.execSQL(CREATE_GERER)
        db.execSQL(CREATE_TRAITEMENT)
        db.execSQL(CREATE_RECOLTE)
        db.execSQL(CREATE_VENTE)
        db.execSQL(CREATE_UTILISATEUR)
    }

    // ✅ Correction upgrade avec migration douce
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                db.execSQL("DROP TABLE IF EXISTS VENTE")
                db.execSQL("DROP TABLE IF EXISTS RECOLTE")
                db.execSQL("DROP TABLE IF EXISTS TRAITEMENT")
                db.execSQL("DROP TABLE IF EXISTS GERER")
                db.execSQL("DROP TABLE IF EXISTS EMPLOYE")
                db.execSQL("DROP TABLE IF EXISTS QUALITE_EAU")
                db.execSQL("DROP TABLE IF EXISTS NOURRISSAGE")
                db.execSQL("DROP TABLE IF EXISTS ALIMENTATION")
                db.execSQL("DROP TABLE IF EXISTS POISSON")
                db.execSQL("DROP TABLE IF EXISTS ESPECE")
                db.execSQL("DROP TABLE IF EXISTS BASSIN")
                db.execSQL("DROP TABLE IF EXISTS UTILISATEUR")
                onCreate(db)
            } catch (e: Exception) {
                Toast.makeText(mContext, "Erreur upgrade BDD: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        
        if (oldVersion < 3) {
            val tables = listOf("BASSIN", "ESPECE", "POISSON", "ALIMENTATION", "NOURRISSAGE", "QUALITE_EAU", "RECOLTE", "VENTE")
            for (table in tables) {
                try {
                    db.execSQL("ALTER TABLE ${table} ADD COLUMN isSynced INTEGER DEFAULT 0")
                } catch (e: Exception) {
                    // Ignorer si la colonne existe déjà
                }
            }
        }
    }

    // ================= TABLES =================

    private val CREATE_BASSIN = """
        CREATE TABLE BASSIN (
            ID_bassin INTEGER PRIMARY KEY AUTOINCREMENT,
            Nom_bassin TEXT NOT NULL,
            Capacite INTEGER NOT NULL,
            Type_bassin TEXT,
            Localisation TEXT,
            Etat TEXT DEFAULT 'actif',
            isSynced INTEGER DEFAULT 0
        )
    """.trimIndent()

    private val CREATE_UTILISATEUR = """
        CREATE TABLE UTILISATEUR (
            ID_user INTEGER PRIMARY KEY AUTOINCREMENT,
            Username TEXT UNIQUE NOT NULL,
            Password TEXT NOT NULL,
            Role TEXT NOT NULL,
            Nom TEXT
        )
    """.trimIndent()

    private val CREATE_ESPECE = """
        CREATE TABLE ESPECE (
            ID_espece INTEGER PRIMARY KEY AUTOINCREMENT,
            Nom_espece TEXT NOT NULL,
            Description TEXT,
            Prix_unitaire REAL DEFAULT 0.0,
            isSynced INTEGER DEFAULT 0
        )
    """.trimIndent()

    private val CREATE_POISSON = """
        CREATE TABLE POISSON (
            ID_poisson INTEGER PRIMARY KEY AUTOINCREMENT,
            Quantite INTEGER NOT NULL,
            Date_introduction TEXT,
            Poids_moyen REAL,
            Mortalite INTEGER DEFAULT 0,
            ID_espece INTEGER,
            ID_bassin INTEGER,
            isSynced INTEGER DEFAULT 0,
            FOREIGN KEY(ID_espece) REFERENCES ESPECE(ID_espece),
            FOREIGN KEY(ID_bassin) REFERENCES BASSIN(ID_bassin)
        )
    """.trimIndent()

    private val CREATE_ALIMENTATION = """
        CREATE TABLE ALIMENTATION (
            ID_aliment INTEGER PRIMARY KEY AUTOINCREMENT,
            Nom_aliment TEXT NOT NULL,
            Type_aliment TEXT,
            Stock REAL,
            Prix_unitaire REAL DEFAULT 0.0,
            isSynced INTEGER DEFAULT 0
        )
    """.trimIndent()

    private val CREATE_NOURRISSAGE = """
        CREATE TABLE NOURRISSAGE (
            ID_nourrissage INTEGER PRIMARY KEY AUTOINCREMENT,
            Date_nourrissage TEXT,
            Quantite REAL,
            ID_bassin INTEGER,
            ID_aliment INTEGER,
            isSynced INTEGER DEFAULT 0,
            FOREIGN KEY(ID_bassin) REFERENCES BASSIN(ID_bassin),
            FOREIGN KEY(ID_aliment) REFERENCES ALIMENTATION(ID_aliment)
        )
    """.trimIndent()

    private val CREATE_QUALITE_EAU = """
        CREATE TABLE QUALITE_EAU (
            ID_qualite INTEGER PRIMARY KEY AUTOINCREMENT,
            Temperature REAL,
            Ph REAL,
            Oxygene REAL,
            Date_mesure TEXT,
            ID_bassin INTEGER,
            isSynced INTEGER DEFAULT 0,
            FOREIGN KEY(ID_bassin) REFERENCES BASSIN(ID_bassin)
        )
    """.trimIndent()

    private val CREATE_EMPLOYE = """
        CREATE TABLE EMPLOYE (
            ID_employe INTEGER PRIMARY KEY AUTOINCREMENT,
            Nom TEXT NOT NULL,
            Prenom TEXT,
            Role TEXT,
            Telephone TEXT
        )
    """.trimIndent()

    private val CREATE_GERER = """
        CREATE TABLE GERER (
            ID_employe INTEGER,
            ID_bassin INTEGER,
            PRIMARY KEY(ID_employe, ID_bassin),
            FOREIGN KEY(ID_employe) REFERENCES EMPLOYE(ID_employe),
            FOREIGN KEY(ID_bassin) REFERENCES BASSIN(ID_bassin)
        )
    """.trimIndent()

    private val CREATE_TRAITEMENT = """
        CREATE TABLE TRAITEMENT (
            ID_traitement INTEGER PRIMARY KEY AUTOINCREMENT,
            Description TEXT,
            Date_traitement TEXT,
            ID_bassin INTEGER,
            FOREIGN KEY(ID_bassin) REFERENCES BASSIN(ID_bassin)
        )
    """.trimIndent()

    private val CREATE_RECOLTE = """
        CREATE TABLE RECOLTE (
            ID_recolte INTEGER PRIMARY KEY AUTOINCREMENT,
            Date_recolte TEXT,
            Quantite INTEGER,
            Poids_total REAL,
            ID_bassin INTEGER,
            isSynced INTEGER DEFAULT 0,
            FOREIGN KEY(ID_bassin) REFERENCES BASSIN(ID_bassin)
        )
    """.trimIndent()

    private val CREATE_VENTE = """
        CREATE TABLE VENTE (
            ID_vente INTEGER PRIMARY KEY AUTOINCREMENT,
            Client TEXT NOT NULL,
            Quantite INTEGER NOT NULL,
            ID_espece INTEGER NOT NULL,
            Prix_unitaire REAL NOT NULL,
            Prix_total REAL NOT NULL,
            Date_vente TEXT,
            isSynced INTEGER DEFAULT 0,
            FOREIGN KEY(ID_espece) REFERENCES ESPECE(ID_espece)
        )
    """.trimIndent()

    // ================= CRUD BASIQUE =================

    fun addBassin(nom: String, capacite: Int, type: String?, localisation: String?): Long {
        if (capacite <= 0) {
            Toast.makeText(mContext, "Capacité invalide", Toast.LENGTH_SHORT).show()
            return -1
        }
        val values = ContentValues().apply {
            put("Nom_bassin", nom)
            put("Capacite", capacite)
            put("Type_bassin", type)
            put("Localisation", localisation)
        }
        return writableDatabase.insert("BASSIN", null, values)
    }

    // ================= GET ALL (Lecture) =================
    
    fun markAsSynced(tableName: String, idColumn: String, idValue: Int) {
        val values = ContentValues().apply { put("isSynced", 1) }
        writableDatabase.update(tableName, values, "$idColumn = ?", arrayOf(idValue.toString()))
    }

    fun getAllBassins(onlyUnsynced: Boolean = false): List<Bassin> {
        val list = mutableListOf<Bassin>()
        val condition = if (onlyUnsynced) "WHERE isSynced = 0" else ""
        val cursor = readableDatabase.rawQuery("SELECT * FROM BASSIN $condition", null)

        while (cursor.moveToNext()) {
            list.add(Bassin(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getInt(2),
                cursor.getString(3) ?: "",
                cursor.getString(4) ?: "",
                cursor.getString(5) ?: ""
            ))
        }

        cursor.close()
        return list
    }

    fun updateBassin(id: Int, nom: String, capacite: Int, type: String?, localisation: String?): Int {
        val values = ContentValues().apply {
            put("Nom_bassin", nom)
            put("Capacite", capacite)
            put("Type_bassin", type)
            put("Localisation", localisation)
        }
        return writableDatabase.update("BASSIN", values, "ID_bassin = ?", arrayOf(id.toString()))
    }

    fun deleteBassin(id: Int): Int {
        return writableDatabase.delete("BASSIN", "ID_bassin = ?", arrayOf(id.toString()))
    }

    fun getBassinById(id: Int): Bassin? {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM BASSIN WHERE ID_bassin = ?", 
            arrayOf(id.toString())
        )
        
        return if (cursor.moveToFirst()) {
            val bassin = Bassin(
                id = cursor.getInt(0),
                nomBassin = cursor.getString(1),
                capacite = cursor.getInt(2),
                typeBassin = cursor.getString(3),
                localisation = cursor.getString(4)
            )
            cursor.close()
            bassin
        } else {
            cursor.close()
            null
        }
    }

    fun getPoissonsByBassin(bassinId: Int): List<Poisson> {
        val list = mutableListOf<Poisson>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM POISSON WHERE ID_bassin = ?", 
            arrayOf(bassinId.toString())
        )
        
        while (cursor.moveToNext()) {
            list.add(Poisson(
                id = cursor.getInt(0),
                quantite = cursor.getInt(1),
                dateIntroduction = cursor.getString(2),
                poidsMoyen = cursor.getFloat(3),
                mortalite = cursor.getInt(4),
                idEspece = cursor.getInt(5),
                idBassin = cursor.getInt(6)
            ))
        }
        cursor.close()
        return list
    }

    // ================= AUTH =================
    
    private fun hashPassword(password: String): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password
        }
    }

    fun login(username: String, password: String): Pair<Boolean, String?> {
        val cursor = readableDatabase.rawQuery(
            "SELECT Password, Role FROM UTILISATEUR WHERE Username = ?",
            arrayOf(username)
        )

        var result: Pair<Boolean, String?> = Pair(false, null)
        
        if (cursor.moveToFirst()) {
            val storedPassword = cursor.getString(0)
            val role = cursor.getString(1)
            
            // On vérifie soit le hash SHA-256, soit le mot de passe en clair (compatibilité ascendante)
            if (storedPassword == hashPassword(password) || storedPassword == password) {
                // Si c'était en clair, on met à jour vers SHA-256 transparentement
                if (storedPassword == password) {
                    val updateValues = ContentValues().apply {
                        put("Password", hashPassword(password))
                    }
                    writableDatabase.update("UTILISATEUR", updateValues, "Username = ?", arrayOf(username))
                }
                result = Pair(true, role)
            }
        }

        cursor.close()
        return result
    }
    
    fun register(username: String, password: String, nom: String): Pair<Boolean, String> {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM UTILISATEUR WHERE Username = ?", arrayOf(username))
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        
        if (count > 0) return Pair(false, "Nom d'utilisateur déjà pris")
        
        val values = ContentValues().apply {
            put("Username", username)
            put("Password", hashPassword(password))
            put("Role", "user")
            put("Nom", nom)
        }
        val id = writableDatabase.insert("UTILISATEUR", null, values)
        return if (id != -1L) Pair(true, "Inscription réussie") else Pair(false, "Erreur lors de l'inscription")
    }

    fun addDefaultUsers() {
        val defaultUsers = listOf(
            Triple("admin", "admin", "admin"),
            Triple("employe", "employe", "employe"),
            Triple("user", "user", "user")
        )

        for ((username, password, role) in defaultUsers) {
            val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM UTILISATEUR WHERE Username = ?", arrayOf(username))
            cursor.moveToFirst()
            val count = cursor.getInt(0)
            cursor.close()

            if (count == 0) {
                val values = ContentValues().apply {
                    put("Username", username)
                    put("Password", hashPassword(password))
                    put("Role", role)
                    put("Nom", when(role) {
                        "admin" -> "Administrateur"
                        "employe" -> "Employé"
                        else -> "Utilisateur Standard"
                    })
                }
                writableDatabase.insert("UTILISATEUR", null, values)
            }
        }
    }

    fun addDefaultReferenceData() {
        seedDefaultBassins()
        seedDefaultEspeces()
        seedDefaultAliments()
    }

    private fun seedDefaultBassins() {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM BASSIN", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        if (count > 0) return

        val defaultBassins = listOf(
            Bassin(nomBassin = "Bassin Nord", capacite = 600, typeBassin = "Béton", localisation = "Zone A"),
            Bassin(nomBassin = "Bassin Sud", capacite = 450, typeBassin = "Terre", localisation = "Zone B")
        )

        defaultBassins.forEach { bassin ->
            addBassin(bassin.nomBassin, bassin.capacite, bassin.typeBassin, bassin.localisation)
        }
    }

    private fun seedDefaultEspeces() {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM ESPECE", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        if (count > 0) return

        val defaultEspeces = listOf(
            Triple("Tilapia du Nil", "Poisson d'elevage tres courant, croissance rapide", 2500f),
            Triple("Carpe Commune", "Espece robuste en bassin, facile a elevage", 1800f),
            Triple("Carpe Miroir", "Variete de carpe sans ecailles", 2000f),
            Triple("Silure Africain", "Poisson de grande taille, croissance rapide", 3000f),
            Triple("Truite Arc-en-ciel", "Poisson d'eau froide, haute qualite", 4500f),
            Triple("Bar commun", "Poisson marin adapte a l'elevage", 5500f),
            Triple("Daurade Royale", "Poisson de haute valeur commerciale", 6000f),
            Triple("Crevette Geante", "Crustace d'elevage populaire", 8000f),
            Triple("Anguille Européenne", "Poisson de grande valeur", 7000f),
            Triple("Perche du Nil", "Predateur, croissance rapide", 4000f)
        )

        defaultEspeces.forEach { (nom, description, prix) ->
            addEspeceWithPrice(nom, description, prix)
        }
    }

    private fun seedDefaultAliments() {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM ALIMENTATION", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        if (count > 0) return

        val defaultAliments = listOf(
            Triple("Granules Croissance", "Granulé", 1500f),
            Triple("Granules Démarrage", "Granulé", 2000f),
            Triple("Granules Finition", "Granulé", 1800f),
            Triple("Farine de Poisson", "Farine", 2500f),
            Triple("Farine de Soja", "Farine", 1200f),
            Triple("Tourteau de Coton", "Tourteau", 1000f),
            Triple("Maïs Concassé", "Céréale", 800f),
            Triple("Blé", "Céréale", 900f),
            Triple("Sons de Riz", "Sons", 700f),
            Triple("Complément Vitaminé", "Complément", 3500f),
            Triple("Algues Séchées", "Naturel", 2200f),
            Triple("Larves d'Insectes", "Vivant", 4000f),
            Triple("Plancton", "Naturel", 1500f),
            Triple("Pulpe de Betterave", "Végétal", 600f)
        )

        defaultAliments.forEach { (nom, type, prix) ->
            addAlimentWithPrice(nom, type)
        }
    }

    // ================= EMPLOYE =================

    fun addEmploye(nom: String, prenom: String?, role: String?, telephone: String?): Long {
        if (nom.isBlank()) {
            // Toast supprimé pour éviter le crash
            return -1
        }
        val values = ContentValues().apply {
            put("Nom", nom)
            put("Prenom", prenom)
            put("Role", role)
            put("Telephone", telephone)
        }
        return writableDatabase.insert("EMPLOYE", null, values)
    }

    fun getAllEmployes(): List<Employe> {
        val list = mutableListOf<Employe>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM EMPLOYE", null)

        while (cursor.moveToNext()) {
            list.add(Employe(
                id = cursor.getInt(0),
                nom = cursor.getString(1),
                prenom = cursor.getString(2),
                role = cursor.getString(3),
                telephone = cursor.getString(4)
            ))
        }
        cursor.close()
        return list
    }

    fun updateEmploye(id: Int, nom: String, prenom: String?, role: String?, telephone: String?): Int {
        val values = ContentValues().apply {
            put("Nom", nom)
            put("Prenom", prenom)
            put("Role", role)
            put("Telephone", telephone)
        }
        return writableDatabase.update("EMPLOYE", values, "ID_employe = ?", arrayOf(id.toString()))
    }

    fun deleteEmploye(id: Int): Int {
        return writableDatabase.delete("EMPLOYE", "ID_employe = ?", arrayOf(id.toString()))
    }

    // ================= ESPECE =================

    fun addEspece(nom: String, description: String?): Long {
        if (nom.isBlank()) {
            // Toast supprimé pour éviter le crash
            return -1
        }
        val values = ContentValues().apply {
            put("Nom_espece", nom)
            put("Description", description ?: "")
        }
        return writableDatabase.insert("ESPECE", null, values)
    }

    fun addEspeceWithPrice(nom: String, description: String?, prixUnitaire: Float): Long {
        if (nom.isBlank()) {
            return -1
        }
        val values = ContentValues().apply {
            put("Nom_espece", nom)
            put("Description", description ?: "")
            put("Prix_unitaire", prixUnitaire)
        }
        return writableDatabase.insert("ESPECE", null, values)
    }

    fun addAlimentation(nom: String, description: String?): Long {
        if (nom.isBlank()) {
            return -1
        }
        val values = ContentValues().apply {
            put("Nom_aliment", nom)
            put("Description", description ?: "")
        }
        return writableDatabase.insert("ALIMENTATION", null, values)
    }

    fun addAlimentWithPrice(nom: String, type: String): Long {
        if (nom.isBlank()) {
            return -1
        }
        val values = ContentValues().apply {
            put("Nom_aliment", nom)
            put("Type_aliment", type)
            put("Stock", 0f) // Stock initial à 0
        }
        return writableDatabase.insert("ALIMENTATION", null, values)
    }

    fun getAllEspeces(onlyUnsynced: Boolean = false): List<Espece> {
        val list = mutableListOf<Espece>()
        val condition = if (onlyUnsynced) "WHERE isSynced = 0" else ""
        val cursor = readableDatabase.rawQuery("SELECT * FROM ESPECE $condition", null)

        while (cursor.moveToNext()) {
            list.add(Espece(
                id = cursor.getInt(0),
                nomEspece = cursor.getString(1),
                description = cursor.getString(2),
                prixUnitaire = cursor.getFloat(3)
            ))
        }
        cursor.close()
        return list
    }

    fun getEspeceById(id: Int): Espece? {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM ESPECE WHERE ID_espece = ?", 
            arrayOf(id.toString())
        )
        
        return if (cursor.moveToFirst()) {
            val espece = Espece(
                id = cursor.getInt(0),
                nomEspece = cursor.getString(1),
                description = cursor.getString(2),
                prixUnitaire = cursor.getFloat(3)
            )
            cursor.close()
            espece
        } else {
            cursor.close()
            null
        }
    }

    fun getAllAlimentations(onlyUnsynced: Boolean = false): List<Alimentation> {
        val list = mutableListOf<Alimentation>()
        val condition = if (onlyUnsynced) "WHERE isSynced = 0" else ""
        try {
            val cursor = readableDatabase.rawQuery("SELECT * FROM ALIMENTATION $condition", null)
            while (cursor.moveToNext()) {
                try {
                    list.add(
                        Alimentation(
                            id = cursor.getInt(0),
                            nomAliment = cursor.getString(1) ?: "",
                            typeAliment = cursor.getString(2) ?: "",
                            stock = cursor.getFloat(3)
                        )
                    )
                } catch (e: Exception) {
                    // Ignorer les lignes corrompues mais continuer avec les autres
                    continue
                }
            }
            cursor.close()
        } catch (e: Exception) {
            // Retourner une liste vide si la table n'existe pas ou a des problèmes
            return emptyList()
        }
        return list
    }

    fun updateEspece(id: Int, nom: String, description: String?): Int {
        val values = ContentValues().apply {
            put("Nom_espece", nom)
            put("Description", description)
        }
        return writableDatabase.update("ESPECE", values, "ID_espece = ?", arrayOf(id.toString()))
    }

    fun updatePrixEspece(id: Int, prixUnitaire: Float): Int {
        val values = ContentValues().apply {
            put("Prix_unitaire", prixUnitaire)
        }
        return writableDatabase.update("ESPECE", values, "ID_espece = ?", arrayOf(id.toString()))
    }

    // CRUD complet pour ALIMENTATION
    fun updateAlimentation(id: Int, nom: String, type: String, stock: Float): Int {
        val values = ContentValues().apply {
            put("Nom_aliment", nom)
            put("Type_aliment", type)
            put("Stock", stock)
        }
        return writableDatabase.update("ALIMENTATION", values, "ID_aliment = ?", arrayOf(id.toString()))
    }

    fun deleteAlimentation(id: Int): Int {
        return writableDatabase.delete("ALIMENTATION", "ID_aliment = ?", arrayOf(id.toString()))
    }

    fun getAlimentationById(id: Int): Alimentation? {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM ALIMENTATION WHERE ID_aliment = ?", 
            arrayOf(id.toString())
        )
        
        return if (cursor.moveToFirst()) {
            val aliment = Alimentation(
                id = cursor.getInt(0),
                nomAliment = cursor.getString(1),
                typeAliment = cursor.getString(2),
                stock = cursor.getFloat(3),
                            )
            cursor.close()
            aliment
        } else {
            cursor.close()
            null
        }
    }

    fun searchAlimentation(query: String): List<Alimentation> {
        val list = mutableListOf<Alimentation>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM ALIMENTATION WHERE Nom_aliment LIKE ? OR Type_aliment LIKE ?",
            arrayOf("%$query%", "%$query%")
        )

        while (cursor.moveToNext()) {
            list.add(
                Alimentation(
                    id = cursor.getInt(0),
                    nomAliment = cursor.getString(1),
                    typeAliment = cursor.getString(2),
                    stock = cursor.getFloat(3)
                )
            )
        }
        cursor.close()
        return list
    }

    fun deleteEspece(id: Int): Int {
        return writableDatabase.delete("ESPECE", "ID_espece = ?", arrayOf(id.toString()))
    }

    // Recherche dans les bassins
    fun searchBassins(query: String): List<Bassin> {
        val list = mutableListOf<Bassin>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM BASSIN WHERE Nom_bassin LIKE ?",
            arrayOf("%$query%")
        )

        while (cursor.moveToNext()) {
            list.add(Bassin(
                id = cursor.getInt(0),
                nomBassin = cursor.getString(1),
                capacite = cursor.getInt(2),
                typeBassin = cursor.getString(3),
                localisation = cursor.getString(4),
                etat = cursor.getString(5)
            ))
        }
        cursor.close()
        return list
    }

    // Méthodes CRUD pour Nourrissage
    fun updateNourrissage(id: Int, quantite: Float, dateNourrissage: String, idAliment: Int, idBassin: Int): Int {
        val values = ContentValues().apply {
            put("Quantite", quantite)
            put("Date_nourrissage", dateNourrissage)
            put("ID_aliment", idAliment)
            put("ID_bassin", idBassin)
        }
        return writableDatabase.update("NOURRISSAGE", values, "ID_nourrissage = ?", arrayOf(id.toString()))
    }

    // Méthodes manquantes pour les autres entités
    fun getAllPoissons(onlyUnsynced: Boolean = false): List<Poisson> {
        val list = mutableListOf<Poisson>()
        val condition = if (onlyUnsynced) "WHERE p.isSynced = 0" else ""
        val cursor = readableDatabase.rawQuery("""
            SELECT p.ID_poisson, p.Quantite, p.Date_introduction, p.Poids_moyen, p.Mortalite, 
                   p.ID_espece, p.ID_bassin, e.Nom_espece, b.Nom_bassin 
            FROM POISSON p 
            LEFT JOIN ESPECE e ON p.ID_espece = e.ID_espece 
            LEFT JOIN BASSIN b ON p.ID_bassin = b.ID_bassin
            $condition
        """.trimIndent(), null)

        while (cursor.moveToNext()) {
            list.add(Poisson(
                id = cursor.getInt(0),
                quantite = cursor.getInt(1),
                dateIntroduction = cursor.getString(2),
                poidsMoyen = cursor.getFloat(3),
                mortalite = cursor.getInt(4),
                idEspece = cursor.getInt(5),
                idBassin = cursor.getInt(6),
                nomEspece = cursor.getString(7) ?: "",
                nomBassin = cursor.getString(8) ?: ""
            ))
        }
        cursor.close()
        return list
    }

    fun getAllNourrissages(onlyUnsynced: Boolean = false): List<Nourrissage> {
        val list = mutableListOf<Nourrissage>()
        val condition = if (onlyUnsynced) "WHERE n.isSynced = 0" else ""
        val cursor = readableDatabase.rawQuery("""
            SELECT n.ID_nourrissage, n.Date_nourrissage, n.Quantite, 
                   n.ID_bassin, n.ID_aliment, b.Nom_bassin, a.Nom_aliment 
            FROM NOURRISSAGE n 
            LEFT JOIN BASSIN b ON n.ID_bassin = b.ID_bassin 
            LEFT JOIN ALIMENTATION a ON n.ID_aliment = a.ID_aliment
            $condition
        """.trimIndent(), null)

        while (cursor.moveToNext()) {
            list.add(Nourrissage(
                id = cursor.getInt(0),
                dateNourrissage = cursor.getString(1),
                quantite = cursor.getFloat(2),
                idBassin = cursor.getInt(3),
                idAliment = cursor.getInt(4),
                nomBassin = cursor.getString(5) ?: "",
                nomAliment = cursor.getString(6) ?: ""
            ))
        }
        cursor.close()
        return list
    }

    fun getAllQualiteEau(onlyUnsynced: Boolean = false): List<QualiteEau> {
        val list = mutableListOf<QualiteEau>()
        val condition = if (onlyUnsynced) "WHERE q.isSynced = 0" else ""
        val cursor = readableDatabase.rawQuery("""
            SELECT q.ID_qualite, q.Temperature, q.Ph, q.Oxygene, q.Date_mesure, 
                   q.ID_bassin, b.Nom_bassin 
            FROM QUALITE_EAU q 
            LEFT JOIN BASSIN b ON q.ID_bassin = b.ID_bassin
            $condition
        """.trimIndent(), null)

        while (cursor.moveToNext()) {
            list.add(QualiteEau(
                id = cursor.getInt(0),
                temperature = cursor.getFloat(1),
                ph = cursor.getFloat(2),
                oxygene = cursor.getFloat(3),
                dateMesure = cursor.getString(4),
                idBassin = cursor.getInt(5),
                nomBassin = cursor.getString(6) ?: ""
            ))
        }
        cursor.close()
        return list
    }

    fun getAllRecoltes(onlyUnsynced: Boolean = false): List<Recolte> {
        val list = mutableListOf<Recolte>()
        try {
            val condition = if (onlyUnsynced) "WHERE r.isSynced = 0" else ""
            android.util.Log.d("DatabaseHelper", "Début getAllRecoltes")
            val cursor = readableDatabase.rawQuery("""
                SELECT r.ID_recolte, r.Date_recolte, r.Quantite, r.Poids_total, 
                       r.ID_bassin, b.Nom_bassin 
                FROM RECOLTE r 
                LEFT JOIN BASSIN b ON r.ID_bassin = b.ID_bassin
                $condition
            """.trimIndent(), null)

            var count = 0
            while (cursor.moveToNext()) {
                list.add(Recolte(
                    id = cursor.getInt(0),
                    dateRecolte = cursor.getString(1),
                    quantite = cursor.getInt(2),
                    poidsTotal = cursor.getFloat(3),
                    idBassin = cursor.getInt(4),
                    nomBassin = cursor.getString(5) ?: ""
                ))
                count++
            }
            cursor.close()
            android.util.Log.d("DatabaseHelper", "getAllRecoltes terminé: $count récoltes trouvées")
        } catch (e: Exception) {
            android.util.Log.e("DatabaseHelper", "Erreur getAllRecoltes: ${e.message}", e)
        }
        return list
    }

    fun getAllVentes(onlyUnsynced: Boolean = false): List<Vente> {
        val list = mutableListOf<Vente>()
        val condition = if (onlyUnsynced) "WHERE v.isSynced = 0" else ""
        val cursor = readableDatabase.rawQuery("""
            SELECT v.ID_vente, v.Client, v.Quantite, v.ID_espece, v.Prix_unitaire, 
                   v.Prix_total, v.Date_vente, e.Nom_espece 
            FROM VENTE v 
            LEFT JOIN ESPECE e ON v.ID_espece = e.ID_espece
            $condition
        """.trimIndent(), null)

        while (cursor.moveToNext()) {
            list.add(Vente(
                id = cursor.getInt(0),
                client = cursor.getString(1),
                quantite = cursor.getInt(2),
                idEspece = cursor.getInt(3),
                prixUnitaire = cursor.getFloat(4),
                prixTotal = cursor.getFloat(5),
                dateVente = cursor.getString(6),
                nomEspece = cursor.getString(7) ?: ""
            ))
        }
        cursor.close()
        return list
    }

    // Vérifier la capacité du bassin
    fun checkBassinCapacity(bassinId: Int, additionalPoissons: Int): Triple<Boolean, Int, Int> {
        // Obtenir la capacité du bassin
        val cursor = readableDatabase.rawQuery(
            "SELECT Capacite FROM BASSIN WHERE ID_bassin = ?", 
            arrayOf(bassinId.toString())
        )
        
        val capacite = if (cursor.moveToFirst() && !cursor.isNull(0)) {
            cursor.getInt(0)
        } else {
            cursor.close()
            return Triple(false, 0, 0)
        }
        cursor.close()
        
        // Obtenir le nombre actuel de poissons dans le bassin
        val currentPoissons = getTotalPoissonsInBassin(bassinId)
        
        // Vérifier si l'ajout dépasse la capacité
        val wouldExceed = (currentPoissons + additionalPoissons) > capacite
        
        return Triple(wouldExceed, currentPoissons, capacite)
    }

    // Méthodes CRUD supplémentaires pour Poisson
    fun addPoisson(quantite: Int, dateIntroduction: String, poidsMoyen: Float, mortalite: Int, idEspece: Int, idBassin: Int): Pair<Long, Boolean> {
        // Vérifier la capacité du bassin
        val (wouldExceed, currentPoissons, capacite) = checkBassinCapacity(idBassin, quantite)
        
        if (wouldExceed) {
            // Retourner -1 et true pour indiquer que la capacité est dépassée
            return Pair(-1L, true)
        }
        
        val values = ContentValues().apply {
            put("Quantite", quantite)
            put("Date_introduction", dateIntroduction)
            put("Poids_moyen", poidsMoyen)
            put("Mortalite", mortalite)
            put("ID_espece", idEspece)
            put("ID_bassin", idBassin)
        }
        val result = writableDatabase.insert("POISSON", null, values)
        return Pair(result, false)
    }

    fun addNourrissage(dateNourrissage: String, quantite: Float, idBassin: Int, idAliment: Int): Long {
        val values = ContentValues().apply {
            put("Date_nourrissage", dateNourrissage)
            put("Quantite", quantite)
            put("ID_bassin", idBassin)
            put("ID_aliment", idAliment)
        }
        return writableDatabase.insert("NOURRISSAGE", null, values)
    }

    fun addQualiteEau(temperature: Float, ph: Float, oxygene: Float, dateMesure: String, idBassin: Int): Long {
        val values = ContentValues().apply {
            put("Temperature", temperature)
            put("Ph", ph)
            put("Oxygene", oxygene)
            put("Date_mesure", dateMesure)
            put("ID_bassin", idBassin)
        }
        return writableDatabase.insert("QUALITE_EAU", null, values)
    }

    fun addRecolte(dateRecolte: String, quantite: Int, poidsTotal: Float, idBassin: Int): Long {
        try {
            val values = ContentValues().apply {
                put("Date_recolte", dateRecolte)
                put("Quantite", quantite)
                put("Poids_total", poidsTotal)
                put("ID_bassin", idBassin)
            }
            
            // Insérer la récolte d'abord
            val recolteId = writableDatabase.insert("RECOLTE", null, values)
            
            // Si l'insertion a réussi, mettre à jour les poissons de manière simple
            if (recolteId != -1L) {
                try {
                    // Approche simple: décrémenter la quantité du premier lot de poissons trouvé
                    val cursor = readableDatabase.rawQuery(
                        "SELECT ID_poisson, Quantite FROM POISSON WHERE ID_bassin = ? AND Quantite > 0 LIMIT 1",
                        arrayOf(idBassin.toString())
                    )
                    
                    if (cursor.moveToFirst()) {
                        val poissonId = cursor.getInt(0)
                        val quantiteActuelle = cursor.getInt(1)
                        cursor.close()
                        
                        // Calculer la nouvelle quantité (ne pas aller en dessous de 0)
                        val nouvelleQuantite = maxOf(0, quantiteActuelle - quantite)
                        
                        // Mettre à jour seulement ce lot de poissons
                        val updateValues = ContentValues().apply {
                            put("Quantite", nouvelleQuantite)
                        }
                        writableDatabase.update("POISSON", updateValues, "ID_poisson = ?", arrayOf(poissonId.toString()))
                    } else {
                        cursor.close()
                    }
                } catch (e: Exception) {
                    // Si erreur lors de la mise à jour des poissons, on garde quand même la récolte
                    // Log l'erreur mais ne pas faire planter l'application
                }
            }
            
            return recolteId
        } catch (e: Exception) {
            // En cas d'erreur générale, retourner -1
            return -1L
        }
    }

    fun getTotalPoissonsRecoltes(): Int {
        val cursor = readableDatabase.rawQuery("SELECT SUM(Quantite) FROM RECOLTE", null)
        val total = if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }
        cursor.close()
        return total
    }

    fun getPoidsTotalRecoltes(): Double {
        val cursor = readableDatabase.rawQuery("SELECT SUM(Poids_total) FROM RECOLTE", null)
        val total = if (cursor.moveToFirst() && !cursor.isNull(0)) {
            cursor.getDouble(0)
        } else {
            0.0
        }
        cursor.close()
        return total
    }

    // Statistiques pour la qualité de l'eau
    fun getAverageTemperature(): Float {
        val cursor = readableDatabase.rawQuery("SELECT AVG(Temperature) FROM QUALITE_EAU WHERE Temperature IS NOT NULL", null)
        val avg = if (cursor.moveToFirst() && !cursor.isNull(0)) {
            cursor.getFloat(0)
        } else {
            0f
        }
        cursor.close()
        return avg
    }

    fun getAveragePH(): Float {
        val cursor = readableDatabase.rawQuery("SELECT AVG(Ph) FROM QUALITE_EAU WHERE Ph IS NOT NULL", null)
        val avg = if (cursor.moveToFirst() && !cursor.isNull(0)) {
            cursor.getFloat(0)
        } else {
            0f
        }
        cursor.close()
        return avg
    }

    fun getAverageOxygene(): Float {
        val cursor = readableDatabase.rawQuery("SELECT AVG(Oxygene) FROM QUALITE_EAU WHERE Oxygene IS NOT NULL", null)
        val avg = if (cursor.moveToFirst() && !cursor.isNull(0)) {
            cursor.getFloat(0)
        } else {
            0f
        }
        cursor.close()
        return avg
    }

    fun getLatestQualiteEau(): QualiteEau? {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM QUALITE_EAU ORDER BY dateMesure DESC LIMIT 1", null
        )
        return if (cursor.moveToFirst()) {
            val qualite = QualiteEau(
                id = cursor.getInt(0),
                temperature = cursor.getFloat(1),
                ph = cursor.getFloat(2),
                oxygene = cursor.getFloat(3),
                dateMesure = cursor.getString(4),
                idBassin = cursor.getInt(5)
            )
            cursor.close()
            qualite
        } else {
            cursor.close()
            null
        }
    }

    fun getTotalRevenus(): Double {
        val cursor = readableDatabase.rawQuery("SELECT SUM(Prix_total) FROM VENTE", null)
        val total = if (cursor.moveToFirst() && !cursor.isNull(0)) {
            cursor.getDouble(0)
        } else {
            0.0
        }
        cursor.close()
        return total
    }

    fun getQualiteEauStats(): Map<String, Any> {
        return mapOf(
            "avgTemperature" to getAverageTemperature(),
            "avgPH" to getAveragePH(),
            "avgOxygene" to getAverageOxygene(),
            "totalMesures" to getAllQualiteEau().size,
            "latestMesure" to (getLatestQualiteEau() ?: "null")
        )
    }
    
    fun getQualiteEauStatsByBassin(bassinId: Int): Map<String, Any> {
        return try {
            val cursor = readableDatabase.rawQuery(
                "SELECT AVG(Temperature), AVG(PH), AVG(Oxygene) FROM QUALITE_EAU WHERE ID_bassin = ?",
                arrayOf(bassinId.toString())
            )
            
            val stats = if (cursor.moveToFirst()) {
                mapOf(
                    "avgTemperature" to (if (!cursor.isNull(0)) cursor.getFloat(0) else 0f),
                    "avgPH" to (if (!cursor.isNull(1)) cursor.getFloat(1) else 0f),
                    "avgOxygene" to (if (!cursor.isNull(2)) cursor.getFloat(2) else 0f),
                    "totalMesures" to getQualiteEauCountByBassin(bassinId)
                )
            } else {
                mapOf(
                    "avgTemperature" to 0f,
                    "avgPH" to 0f,
                    "avgOxygene" to 0f,
                    "totalMesures" to 0
                )
            }
            cursor.close()
            stats
        } catch (e: Exception) {
            mapOf(
                "avgTemperature" to 0f,
                "avgPH" to 0f,
                "avgOxygene" to 0f,
                "totalMesures" to 0
            )
        }
    }
    
    private fun getQualiteEauCountByBassin(bassinId: Int): Int {
        return try {
            val cursor = readableDatabase.rawQuery(
                "SELECT COUNT(*) FROM QUALITE_EAU WHERE ID_bassin = ?",
                arrayOf(bassinId.toString())
            )
            val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
            cursor.close()
            count
        } catch (e: Exception) {
            0
        }
    }

    fun getTotalPoissonsVendus(): Int {
        val cursor = readableDatabase.rawQuery("SELECT SUM(Quantite) FROM VENTE", null)
        val total = if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }
        cursor.close()
        return total
    }

    fun addVente(client: String, quantite: Int, idEspece: Int, dateVente: String): Pair<Long, Float> {
        // Obtenir le prix unitaire de l'espèce
        val cursor = readableDatabase.rawQuery(
            "SELECT Prix_unitaire FROM ESPECE WHERE ID_espece = ?", 
            arrayOf(idEspece.toString())
        )
        
        val prixUnitaire = if (cursor.moveToFirst() && !cursor.isNull(0)) {
            cursor.getFloat(0)
        } else {
            cursor.close()
            return Pair(-1L, 0f) // Espèce non trouvée
        }
        cursor.close()
        
        // Calculer le prix total
        val prixTotal = quantite * prixUnitaire
        
        val values = ContentValues().apply {
            put("Client", client)
            put("Quantite", quantite)
            put("ID_espece", idEspece)
            put("Prix_unitaire", prixUnitaire)
            put("Prix_total", prixTotal)
            put("Date_vente", dateVente)
        }
        val result = writableDatabase.insert("VENTE", null, values)
        return Pair(result, prixTotal)
    }

    fun updatePoisson(
        id: Int,
        quantite: Int,
        dateIntroduction: String?,
        poidsMoyen: Float?,
        mortalite: Int,
        idEspece: Int,
        idBassin: Int
    ): Int {
        val values = ContentValues().apply {
            put("Quantite", quantite)
            put("Date_introduction", dateIntroduction)
            put("Poids_moyen", poidsMoyen)
            put("Mortalite", mortalite)
            put("ID_espece", idEspece)
            put("ID_bassin", idBassin)
        }
        return writableDatabase.update("POISSON", values, "ID_poisson = ?", arrayOf(id.toString()))
    }

    fun deletePoisson(id: Int): Int {
        return writableDatabase.delete("POISSON", "ID_poisson = ?", arrayOf(id.toString()))
    }

    // Méthodes CRUD supplémentaires pour Qualité Eau
    fun updateQualiteEau(id: Int, temperature: Float, ph: Float, oxygene: Float, dateMesure: String, idBassin: Int): Int {
        val values = ContentValues().apply {
            put("Temperature", temperature)
            put("Ph", ph)
            put("Oxygene", oxygene)
            put("Date_mesure", dateMesure)
            put("ID_bassin", idBassin)
        }
        return writableDatabase.update("QUALITE_EAU", values, "ID_qualite = ?", arrayOf(id.toString()))
    }

    fun deleteQualiteEau(id: Int): Int {
        return writableDatabase.delete("QUALITE_EAU", "ID_qualite = ?", arrayOf(id.toString()))
    }

    // Méthodes CRUD supplémentaires pour Récolte
    fun updateRecolte(id: Int, dateRecolte: String, quantite: Int, poidsTotal: Float, idBassin: Int): Int {
        val values = ContentValues().apply {
            put("Date_recolte", dateRecolte)
            put("Quantite", quantite)
            put("Poids_total", poidsTotal)
            put("ID_bassin", idBassin)
        }
        return writableDatabase.update("RECOLTE", values, "ID_recolte = ?", arrayOf(id.toString()))
    }

    fun deleteRecolte(id: Int): Int {
        return writableDatabase.delete("RECOLTE", "ID_recolte = ?", arrayOf(id.toString()))
    }

    // Méthodes CRUD supplémentaires pour Vente
    fun updateVente(id: Int, client: String, quantite: Int, idEspece: Int, dateVente: String): Pair<Int, Float> {
        // Obtenir le prix unitaire de l'espèce
        val cursor = readableDatabase.rawQuery(
            "SELECT Prix_unitaire FROM ESPECE WHERE ID_espece = ?", 
            arrayOf(idEspece.toString())
        )
        
        val prixUnitaire = if (cursor.moveToFirst() && !cursor.isNull(0)) {
            cursor.getFloat(0)
        } else {
            cursor.close()
            return Pair(-1, 0f) // Espèce non trouvée
        }
        cursor.close()
        
        // Calculer le prix total
        val prixTotal = quantite * prixUnitaire
        
        val values = ContentValues().apply {
            put("Client", client)
            put("Quantite", quantite)
            put("ID_espece", idEspece)
            put("Prix_unitaire", prixUnitaire)
            put("Prix_total", prixTotal)
            put("Date_vente", dateVente)
        }
        val result = writableDatabase.update("VENTE", values, "ID_vente = ?", arrayOf(id.toString()))
        return Pair(result, prixTotal)
    }

    fun deleteVente(id: Int): Int {
        return writableDatabase.delete("VENTE", "ID_vente = ?", arrayOf(id.toString()))
    }

    // Méthodes CRUD supplémentaires pour Nourrissage
    fun deleteNourrissage(id: Int): Int {
        return writableDatabase.delete("NOURRISSAGE", "ID_nourrissage = ?", arrayOf(id.toString()))
    }

    // Méthodes de statistiques pour HomeFragment
    fun getTotalBassins(): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM BASSIN", null)
        val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        return count
    }

    fun getTotalPoissons(): Int {
        val cursor = readableDatabase.rawQuery("SELECT SUM(Quantite) FROM POISSON", null)
        val total = if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getInt(0) else 0
        cursor.close()
        return total
    }

    fun getStockTotal(): Double {
        // Calcul correct: Quantite * Poids_moyen (en g) / 1000 = kg
        val cursor = readableDatabase.rawQuery("SELECT SUM((Quantite - Mortalite) * Poids_moyen / 1000) FROM POISSON", null)
        val total = if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getDouble(0) else 0.0
        cursor.close()
        return total
    }

    fun getTotalMortalite(): Int {
        val cursor = readableDatabase.rawQuery("SELECT SUM(Mortalite) FROM POISSON", null)
        val total = if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getInt(0) else 0
        cursor.close()
        return total
    }

    fun getTotalRecoltes(): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM RECOLTE", null)
        val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        return count
    }

    fun getTotalVentes(): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM VENTE", null)
        val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        return count
    }

    fun getTotalRevenue(): Float {
        val cursor = readableDatabase.rawQuery("SELECT SUM(Prix_total) FROM VENTE", null)
        val total = if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getFloat(0) else 0f
        cursor.close()
        return total
    }

    fun getTotalPoissonsInBassin(bassinId: Int): Int {
        val cursor = readableDatabase.rawQuery(
            "SELECT SUM(Quantite) FROM POISSON WHERE ID_bassin = ?", 
            arrayOf(bassinId.toString())
        )
        val total = if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getInt(0) else 0
        cursor.close()
        return total
    }

    fun getStockTotalInBassin(bassinId: Int): Double {
        val cursor = readableDatabase.rawQuery(
            "SELECT SUM((Quantite - Mortalite) * Poids_moyen / 1000) FROM POISSON WHERE ID_bassin = ?",
            arrayOf(bassinId.toString())
        )
        val total = if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getDouble(0) else 0.0
        cursor.close()
        return total
    }

    fun getStockTotalByEspece(especeId: Int): Double {
        val cursor = readableDatabase.rawQuery(
            "SELECT SUM((Quantite - Mortalite) * Poids_moyen / 1000) FROM POISSON WHERE ID_espece = ?",
            arrayOf(especeId.toString())
        )
        val total = if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getDouble(0) else 0.0
        cursor.close()
        return total
    }

    fun getPoidsMoyenGlobal(): Double {
        val cursor = readableDatabase.rawQuery("""
            SELECT AVG(Poids_moyen) FROM POISSON 
            WHERE Poids_moyen IS NOT NULL AND Poids_moyen > 0
        """.trimIndent(), null)
        val poidsMoyen = if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getDouble(0) else 0.0
        cursor.close()
        return poidsMoyen
    }

    fun exportAllDataToCSV(): String? {
        return try {
            val csvContent = StringBuilder()
            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            csvContent.appendLine("EXPORTATION DES DONNEES - PISCICULTURE APP")
            csvContent.appendLine("Date d'export: $timestamp")
            csvContent.appendLine()
            
            // Export Bassins
            csvContent.appendLine("=== BASSINS ===")
            csvContent.appendLine("ID,Nom,Capacité,Type,Localisation,Etat")
            getAllBassins().forEach { bassin ->
                csvContent.appendLine("${bassin.id},\"${bassin.nomBassin}\",${bassin.capacite},\"${bassin.typeBassin ?: ""}\",\"${bassin.localisation ?: ""}\",\"${bassin.etat}\"")
            }
            csvContent.appendLine()
            
            // Export Espèces
            csvContent.appendLine("=== ESPECES ===")
            csvContent.appendLine("ID,Nom,Prix unitaire")
            getAllEspeces().forEach { espece ->
                csvContent.appendLine("${espece.id},\"${espece.nomEspece}\",${espece.prixUnitaire}")
            }
            csvContent.appendLine()
            
            // Export Poissons
            csvContent.appendLine("=== POISSONS ===")
            csvContent.appendLine("ID,Espece,Quantite,Poids moyen,Mortalite,Date introduction,ID Bassin")
            getAllPoissons().forEach { poisson ->
                csvContent.appendLine("${poisson.id},\"${poisson.nomEspece}\",${poisson.quantite},${poisson.poidsMoyen},${poisson.mortalite},${poisson.dateIntroduction},${poisson.idBassin}")
            }
            csvContent.appendLine()
            
            // Export Récoltes
            csvContent.appendLine("=== RECOLTES ===")
            csvContent.appendLine("ID,Date,Quantite,Poids total,ID Bassin")
            getAllRecoltes().forEach { recolte ->
                csvContent.appendLine("${recolte.id},${recolte.dateRecolte},${recolte.quantite},${recolte.poidsTotal},${recolte.idBassin}")
            }
            csvContent.appendLine()
            
            // Export Ventes
            csvContent.appendLine("=== VENTES ===")
            csvContent.appendLine("ID,Client,Date,Quantite,Prix unitaire,Prix total,ID Espece")
            getAllVentes().forEach { vente ->
                csvContent.appendLine("${vente.id},\"${vente.client}\",${vente.dateVente},${vente.quantite},${vente.prixUnitaire},${vente.prixTotal},${vente.idEspece}")
            }
            csvContent.appendLine()
            
            // Export Nourrissage
            csvContent.appendLine("=== NOURISSAGE ===")
            csvContent.appendLine("ID,Date,Quantite,ID Bassin,ID Aliment")
            getAllNourrissages().forEach { nourrissage ->
                csvContent.appendLine("${nourrissage.id},${nourrissage.dateNourrissage},${nourrissage.quantite},${nourrissage.idBassin},${nourrissage.idAliment}")
            }
            csvContent.appendLine()
            
            // Export Qualité Eau
            csvContent.appendLine("=== QUALITE EAU ===")
            csvContent.appendLine("ID,Temperature,PH,Oxygène,Date mesure,ID Bassin")
            getAllQualiteEau().forEach { qualite ->
                csvContent.appendLine("${qualite.id},${qualite.temperature},${qualite.ph},${qualite.oxygene},${qualite.dateMesure},${qualite.idBassin}")
            }
            csvContent.appendLine()
            
            // Export Alimentation
            csvContent.appendLine("=== ALIMENTATION ===")
            csvContent.appendLine("ID,Nom aliment,Type aliment,Stock")
            getAllAlimentations().forEach { alimentation ->
                csvContent.appendLine("${alimentation.id},\"${alimentation.nomAliment}\",\"${alimentation.typeAliment}\",${alimentation.stock}")
            }
            
            csvContent.toString()
        } catch (e: Exception) {
            null
        }
    }
    
    fun exportDataToJSON(): String? {
        return try {
            val jsonContent = StringBuilder()
            jsonContent.appendLine("{")
            
            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            jsonContent.appendLine("  \"export_date\": \"$timestamp\",")
            jsonContent.appendLine("  \"data\": {")
            
            // Export Bassins
            jsonContent.appendLine("    \"bassins\": [")
            getAllBassins().forEachIndexed { index, bassin ->
                jsonContent.appendLine("      {")
                jsonContent.appendLine("        \"id\": ${bassin.id},")
                jsonContent.appendLine("        \"nom\": \"${bassin.nomBassin}\",")
                jsonContent.appendLine("        \"capacite\": ${bassin.capacite},")
                jsonContent.appendLine("        \"type\": \"${bassin.typeBassin ?: ""}\",")
                jsonContent.appendLine("        \"localisation\": \"${bassin.localisation ?: ""}\",")
                jsonContent.appendLine("        \"etat\": \"${bassin.etat}\"")
                jsonContent.appendLine("      }${if (index < getAllBassins().size - 1) "," else ""}")
            }
            jsonContent.appendLine("    ],")
            
            // Export Poissons
            jsonContent.appendLine("    \"poissons\": [")
            getAllPoissons().forEachIndexed { index, poisson ->
                jsonContent.appendLine("      {")
                jsonContent.appendLine("        \"id\": ${poisson.id},")
                jsonContent.appendLine("        \"nom_espece\": \"${poisson.nomEspece}\",")
                jsonContent.appendLine("        \"quantite\": ${poisson.quantite},")
                jsonContent.appendLine("        \"poids_moyen\": ${poisson.poidsMoyen},")
                jsonContent.appendLine("        \"mortalite\": ${poisson.mortalite},")
                jsonContent.appendLine("        \"date_introduction\": \"${poisson.dateIntroduction}\",")
                jsonContent.appendLine("        \"id_bassin\": ${poisson.idBassin}")
                jsonContent.appendLine("      }${if (index < getAllPoissons().size - 1) "," else ""}")
            }
            jsonContent.appendLine("    ],")
            
            // Export Récoltes
            jsonContent.appendLine("    \"recoltes\": [")
            getAllRecoltes().forEachIndexed { index, recolte ->
                jsonContent.appendLine("      {")
                jsonContent.appendLine("        \"id\": ${recolte.id},")
                jsonContent.appendLine("        \"date_recolte\": \"${recolte.dateRecolte}\",")
                jsonContent.appendLine("        \"quantite\": ${recolte.quantite},")
                jsonContent.appendLine("        \"poids_total\": ${recolte.poidsTotal},")
                jsonContent.appendLine("        \"id_bassin\": ${recolte.idBassin}")
                jsonContent.appendLine("      }${if (index < getAllRecoltes().size - 1) "," else ""}")
            }
            jsonContent.appendLine("    ],")
            
            // Export Ventes
            jsonContent.appendLine("    \"ventes\": [")
            getAllVentes().forEachIndexed { index, vente ->
                jsonContent.appendLine("      {")
                jsonContent.appendLine("        \"id\": ${vente.id},")
                jsonContent.appendLine("        \"client\": \"${vente.client}\",")
                jsonContent.appendLine("        \"date_vente\": \"${vente.dateVente}\",")
                jsonContent.appendLine("        \"quantite\": ${vente.quantite},")
                jsonContent.appendLine("        \"prix_unitaire\": ${vente.prixUnitaire},")
                jsonContent.appendLine("        \"prix_total\": ${vente.prixTotal},")
                jsonContent.appendLine("        \"id_espece\": ${vente.idEspece}")
                jsonContent.appendLine("      }${if (index < getAllVentes().size - 1) "," else ""}")
            }
            jsonContent.appendLine("    ]")
            
            jsonContent.appendLine("  }")
            jsonContent.appendLine("}")
            
            jsonContent.toString()
        } catch (e: Exception) {
            null
        }
    }
    
    fun getExportStats(): Map<String, Any> {
        return mapOf(
            "total_bassins" to getAllBassins().size,
            "total_especes" to getAllEspeces().size,
            "total_poissons" to getAllPoissons().size,
            "total_recoltes" to getAllRecoltes().size,
            "total_ventes" to getAllVentes().size,
            "total_nourrissages" to getAllNourrissages().size,
            "total_qualite_eau" to getAllQualiteEau().size,
            "total_alimentations" to getAllAlimentations().size,
            "stock_total_kg" to getStockTotal(),
            "poids_moyen_global" to getPoidsMoyenGlobal(),
            "total_revenus" to getTotalRevenus()
        )
    }
    
    fun transferPoisson(poissonId: Int, newBassinId: Int, quantite: Int): Boolean {
        return try {
            // Obtenir les informations du poisson à transférer
            val cursor = readableDatabase.rawQuery(
                "SELECT Quantite, Poids_moyen, Mortalite, Date_introduction, ID_espece FROM POISSON WHERE ID_poisson = ?",
                arrayOf(poissonId.toString())
            )
            
            if (cursor.moveToFirst()) {
                val quantiteActuelle = cursor.getInt(0)
                val poidsMoyen = cursor.getFloat(1)
                val mortalite = cursor.getInt(2)
                val dateIntroduction = cursor.getString(3)
                val idEspece = cursor.getInt(4)
                cursor.close()
                
                // Vérifier si la quantité à transférer est valide
                if (quantite <= 0 || quantite > quantiteActuelle) {
                    return false
                }
                
                // Vérifier la capacité du nouveau bassin
                val cursorBassin = readableDatabase.rawQuery(
                    "SELECT Capacite FROM BASSIN WHERE ID_bassin = ?",
                    arrayOf(newBassinId.toString())
                )
                
                if (cursorBassin.moveToFirst()) {
                    val capacite = cursorBassin.getInt(0)
                    cursorBassin.close()
                    
                    val stockActuel = getTotalPoissonsInBassin(newBassinId)
                    if (stockActuel + quantite > capacite) {
                        return false // Capacité insuffisante
                    }
                } else {
                    cursorBassin.close()
                    return false // Bassin non trouvé
                }
                
                // Vérifier s'il existe déjà un lot de la même espèce dans le bassin de destination
                val cursorExisting = readableDatabase.rawQuery(
                    "SELECT ID_poisson, Quantite, Poids_moyen FROM POISSON WHERE ID_bassin = ? AND ID_espece = ?",
                    arrayOf(newBassinId.toString(), idEspece.toString())
                )
                
                if (cursorExisting.moveToFirst()) {
                    // Il existe déjà un lot de la même espèce, on le combine
                    val existingPoissonId = cursorExisting.getInt(0)
                    val existingQuantite = cursorExisting.getInt(1)
                    val existingPoidsMoyen = cursorExisting.getFloat(2)
                    cursorExisting.close()
                    
                    // Calculer le nouveau poids moyen (moyenne pondérée)
                    val nouvelleQuantite = existingQuantite + quantite
                    val nouveauPoidsMoyen = ((existingPoidsMoyen * existingQuantite) + (poidsMoyen * quantite)) / nouvelleQuantite
                    
                    // Mettre à jour le lot existant
                    val valuesUpdate = ContentValues().apply {
                        put("Quantite", nouvelleQuantite)
                        put("Poids_moyen", nouveauPoidsMoyen)
                    }
                    val rowsUpdated = writableDatabase.update("POISSON", valuesUpdate, "ID_poisson = ?", arrayOf(existingPoissonId.toString()))
                    
                    if (rowsUpdated > 0) {
                        // Mettre à jour la quantité du poisson original
                        val nouvelleQuantiteSource = quantiteActuelle - quantite
                        val valuesUpdateSource = ContentValues().apply {
                            put("Quantite", nouvelleQuantiteSource)
                        }
                        val rowsUpdatedSource = writableDatabase.update("POISSON", valuesUpdateSource, "ID_poisson = ?", arrayOf(poissonId.toString()))
                        
                        return rowsUpdatedSource > 0
                    }
                } else {
                    cursorExisting.close()
                    
                    // Aucun lot existant, on en crée un nouveau
                    val nouvelleQuantiteSource = quantiteActuelle - quantite
                    val valuesUpdateSource = ContentValues().apply {
                        put("Quantite", nouvelleQuantiteSource)
                    }
                    val rowsUpdatedSource = writableDatabase.update("POISSON", valuesUpdateSource, "ID_poisson = ?", arrayOf(poissonId.toString()))
                    
                    if (rowsUpdatedSource > 0) {
                        // Créer un nouveau lot dans le bassin de destination
                        val valuesInsert = ContentValues().apply {
                            put("Quantite", quantite)
                            put("Poids_moyen", poidsMoyen)
                            put("Mortalite", 0) // Pas de mortalité pour le nouveau lot
                            put("Date_introduction", dateIntroduction)
                            put("ID_bassin", newBassinId)
                            put("ID_espece", idEspece)
                        }
                        val newId = writableDatabase.insert("POISSON", null, valuesInsert)
                        
                        return newId != -1L
                    }
                }
            } else {
                cursor.close()
            }
            
            false
        } catch (e: Exception) {
            false
        }
    }
    
    fun getBassinCapaciteRestante(bassinId: Int): Int {
        return try {
            val cursor = readableDatabase.rawQuery(
                "SELECT Capacite FROM BASSIN WHERE ID_bassin = ?",
                arrayOf(bassinId.toString())
            )
            
            val capacite = if (cursor.moveToFirst()) {
                val cap = cursor.getInt(0)
                cursor.close()
                cap
            } else {
                cursor.close()
                0
            }
            
            capacite - getTotalPoissonsInBassin(bassinId)
        } catch (e: Exception) {
            0
        }
    }


}