package com.serinity.sleepcontrol.dao;

import com.serinity.sleepcontrol.model.Sommeil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface SommeilDao {

    void ajouter(Sommeil sommeil) throws SQLException;

    List<Sommeil> listerTous() throws SQLException;

    Sommeil trouverParId(int id) throws SQLException;

    void modifier(Sommeil sommeil) throws SQLException;

    void supprimer(int id) throws SQLException;

    List<Sommeil> rechercher(String critere) throws SQLException;

    List<Sommeil> filtrerParQualite(String qualite) throws SQLException;

    List<Sommeil> filtrerParPeriode(LocalDate debut, LocalDate fin) throws SQLException;

    double calculerDureeMoyenne() throws SQLException;

    List<Object[]> statistiquesParQualite() throws SQLException;
    Connection getConnection();

}
