package com.serinity.sleepcontrol.dao;

import com.serinity.sleepcontrol.model.Reve;
import java.sql.SQLException;
import java.util.List;

public interface ReveDao {

    // CRUD Operations
    void ajouter(Reve reve) throws SQLException;

    List<Reve> listerTous() throws SQLException;

    Reve trouverParId(int id) throws SQLException;

    List<Reve> trouverParSommeilId(int sommeilId) throws SQLException;

    void modifier(Reve reve) throws SQLException;

    void supprimer(int id) throws SQLException;

    // Recherche et filtres
    List<Reve> rechercher(String critere) throws SQLException;

    List<Reve> filtrerParType(String type) throws SQLException;

    // Statistiques
    List<Object[]> statistiquesParType() throws SQLException;

    double calculerIntensiteMoyenne() throws SQLException;
}
