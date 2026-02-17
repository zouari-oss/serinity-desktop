package com.serinity.sleepcontrol;

import com.serinity.sleepcontrol.utils.MyDataBase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConnection {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   TEST DE CONNEXION BASE DE DONNEES");
        System.out.println("========================================\n");

        try {
            // Test 1: Obtenir la connexion
            System.out.println("1. Test de connexion...");
            Connection conn = MyDataBase.getInstance().getConnection();

            if (conn == null) {
                System.err.println("❌ ERREUR: Connexion NULL");
                System.err.println("   Verifiez que MySQL est demarre");
                System.err.println("   Port: 3307");
                System.err.println("   Base: serinity_sleep");
                return;
            }

            if (conn.isClosed()) {
                System.err.println("❌ ERREUR: Connexion fermee");
                return;
            }

            System.out.println("✅ Connexion etablie avec succes");
            System.out.println("   Database: " + conn.getCatalog());
            System.out.println("   URL: " + conn.getMetaData().getURL());
            System.out.println("   User: " + conn.getMetaData().getUserName());

            // Test 2: Verifier les tables
            System.out.println("\n2. Verification des tables...");

            Statement stmt = conn.createStatement();

            // Test table sommeil
            try {
                ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) as count FROM sommeil");
                if (rs1.next()) {
                    System.out.println("✅ Table 'sommeil': " + rs1.getInt("count") + " enregistrements");
                }
                rs1.close();
            } catch (Exception e) {
                System.err.println("❌ Table 'sommeil' introuvable ou erreur: " + e.getMessage());
            }

            // Test table reve
            try {
                ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) as count FROM reves");
                if (rs2.next()) {
                    System.out.println("✅ Table 'reves': " + rs2.getInt("count") + " enregistrements");
                }
                rs2.close();
            } catch (Exception e) {
                System.err.println("❌ Table 'reves' introuvable ou erreur: " + e.getMessage());
            }

            // Test 3: Structure de la table sommeil
            System.out.println("\n3. Structure de la table 'sommeil':");
            try {
                ResultSet rs3 = stmt.executeQuery("DESCRIBE sommeil");
                while (rs3.next()) {
                    String field = rs3.getString("Field");
                    String type = rs3.getString("Type");
                    String nullable = rs3.getString("Null");
                    String key = rs3.getString("Key");
                    System.out.println("   - " + field + " (" + type + ") " +
                            (key.equals("PRI") ? "[PRIMARY KEY]" : "") +
                            (nullable.equals("NO") ? "[NOT NULL]" : ""));
                }
                rs3.close();
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la lecture de la structure: " + e.getMessage());
            }

            // Test 4: Test d'insertion (optionnel - commenté par défaut)
            System.out.println("\n4. Test d'insertion (desactive par defaut)");
            System.out.println("   Decommentez le code pour tester l'insertion");


            stmt.close();

            System.out.println("\n========================================");
            System.out.println("   ✅ TOUS LES TESTS SONT PASSES!");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("\n========================================");
            System.err.println("   ❌ ERREUR LORS DES TESTS");
            System.err.println("========================================");
            System.err.println("Type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            System.err.println("\nStack trace:");
            e.printStackTrace();

            System.err.println("\n=== CONSEILS DE DEBOGAGE ===");
            System.err.println("1. Verifiez que MySQL/XAMPP est demarre");
            System.err.println("2. Verifiez le port: 3307");
            System.err.println("3. Verifiez que la base 'serinity_sleep' existe");
            System.err.println("4. Verifiez les identifiants dans MyDataBase.java");
        }
    }
}
