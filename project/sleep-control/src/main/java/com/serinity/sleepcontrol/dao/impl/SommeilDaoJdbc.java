package com.serinity.sleepcontrol.dao.impl;

import com.serinity.sleepcontrol.dao.SommeilDao;
import com.serinity.sleepcontrol.dao.ReveDao;
import com.serinity.sleepcontrol.model.Sommeil;
import com.serinity.sleepcontrol.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SommeilDaoJdbc implements SommeilDao {

    private Connection getConn() {
        return MyDataBase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Sommeil sommeil) throws SQLException {
        String sql = "INSERT INTO sommeil (date_nuit, heure_coucher, heure_reveil, qualite, " +
                "commentaire, duree_sommeil, interruptions, humeur_reveil, environnement, " +
                "temperature, bruit_niveau) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDate(1, Date.valueOf(sommeil.getDateNuit()));
            stmt.setTime(2, Time.valueOf(sommeil.getHeureCoucher()));
            stmt.setTime(3, Time.valueOf(sommeil.getHeureReveil()));
            stmt.setString(4, sommeil.getQualite());
            stmt.setString(5, sommeil.getCommentaire());
            // dureeSommeil déjà calculée dans le modèle (setHeureCoucher/Reveil)
            stmt.setDouble(6, sommeil.getDureeSommeil());
            stmt.setInt(7, sommeil.getInterruptions());
            stmt.setString(8, sommeil.getHumeurReveil());
            stmt.setString(9, sommeil.getEnvironnement());
            stmt.setDouble(10, sommeil.getTemperature());
            stmt.setString(11, sommeil.getNiveauBruit());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    sommeil.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Sommeil> listerTous() throws SQLException {
        List<Sommeil> sommeils = new ArrayList<>();
        String sql = "SELECT * FROM sommeil ORDER BY date_nuit DESC";
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Sommeil sommeil = mapResultSetToSommeil(rs);
                sommeil.setNbReves(compterRevesDB(sommeil.getId()));
                sommeils.add(sommeil);
            }
        }
        return sommeils;
    }

    @Override
    public Sommeil trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM sommeil WHERE id = ?";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Sommeil sommeil = mapResultSetToSommeil(rs);
                    ReveDao reveDao = new ReveDaoJdbc();
                    sommeil.setReves(reveDao.trouverParSommeilId(id));
                    return sommeil;
                }
            }
        }
        return null;
    }

    @Override
    public void modifier(Sommeil sommeil) throws SQLException {
        String sql = "UPDATE sommeil SET date_nuit=?, heure_coucher=?, heure_reveil=?, " +
                "qualite=?, commentaire=?, duree_sommeil=?, interruptions=?, " +
                "humeur_reveil=?, environnement=?, temperature=?, bruit_niveau=? WHERE id=?";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(sommeil.getDateNuit()));
            stmt.setTime(2, Time.valueOf(sommeil.getHeureCoucher()));
            stmt.setTime(3, Time.valueOf(sommeil.getHeureReveil()));
            stmt.setString(4, sommeil.getQualite());
            stmt.setString(5, sommeil.getCommentaire());
            stmt.setDouble(6, sommeil.getDureeSommeil());
            stmt.setInt(7, sommeil.getInterruptions());
            stmt.setString(8, sommeil.getHumeurReveil());
            stmt.setString(9, sommeil.getEnvironnement());
            stmt.setDouble(10, sommeil.getTemperature());
            stmt.setString(11, sommeil.getNiveauBruit());
            stmt.setInt(12, sommeil.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM sommeil WHERE id = ?";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Sommeil> rechercher(String critere) throws SQLException {
        List<Sommeil> sommeils = new ArrayList<>();
        String sql = "SELECT * FROM sommeil WHERE qualite LIKE ? OR commentaire LIKE ? " +
                "OR humeur_reveil LIKE ? OR environnement LIKE ? ORDER BY date_nuit DESC";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
            String pattern = "%" + critere + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sommeil sommeil = mapResultSetToSommeil(rs);
                    sommeil.setNbReves(compterRevesDB(sommeil.getId()));
                    sommeils.add(sommeil);
                }
            }
        }
        return sommeils;
    }

    @Override
    public List<Sommeil> filtrerParQualite(String qualite) throws SQLException {
        List<Sommeil> sommeils = new ArrayList<>();
        String sql = "SELECT * FROM sommeil WHERE qualite = ? ORDER BY date_nuit DESC";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
            stmt.setString(1, qualite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sommeil sommeil = mapResultSetToSommeil(rs);
                    sommeil.setNbReves(compterRevesDB(sommeil.getId()));
                    sommeils.add(sommeil);
                }
            }
        }
        return sommeils;
    }

    @Override
    public List<Sommeil> filtrerParPeriode(LocalDate debut, LocalDate fin) throws SQLException {
        List<Sommeil> sommeils = new ArrayList<>();
        String sql = "SELECT * FROM sommeil WHERE date_nuit BETWEEN ? AND ? ORDER BY date_nuit DESC";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(debut));
            stmt.setDate(2, Date.valueOf(fin));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sommeil sommeil = mapResultSetToSommeil(rs);
                    sommeil.setNbReves(compterRevesDB(sommeil.getId()));
                    sommeils.add(sommeil);
                }
            }
        }
        return sommeils;
    }

    @Override
    public double calculerDureeMoyenne() throws SQLException {
        String sql = "SELECT AVG(duree_sommeil) as moyenne FROM sommeil";
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("moyenne");
            }
        }
        return 0;
    }

    @Override
    public List<Object[]> statistiquesParQualite() throws SQLException {
        List<Object[]> stats = new ArrayList<>();
        String sql = "SELECT qualite, COUNT(*) as nombre, AVG(duree_sommeil) as duree_moy " +
                "FROM sommeil GROUP BY qualite";
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.add(new Object[]{
                        rs.getString("qualite"),
                        rs.getInt("nombre"),
                        rs.getDouble("duree_moy")
                });
            }
        }
        return stats;
    }

    private int compterRevesDB(int sommeilId) {
        String sql = "SELECT COUNT(*) FROM reves WHERE sommeil_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, sommeilId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Sommeil mapResultSetToSommeil(ResultSet rs) throws SQLException {
        Sommeil sommeil = new Sommeil();
        sommeil.setId(rs.getInt("id"));
        sommeil.setDateNuit(rs.getDate("date_nuit").toLocalDate());
        sommeil.setHeureCoucher(rs.getTime("heure_coucher").toLocalTime());
        sommeil.setHeureReveil(rs.getTime("heure_reveil").toLocalTime());
        sommeil.setQualite(rs.getString("qualite"));
        sommeil.setCommentaire(rs.getString("commentaire"));
        sommeil.setInterruptions(rs.getInt("interruptions"));
        sommeil.setHumeurReveil(rs.getString("humeur_reveil"));
        sommeil.setEnvironnement(rs.getString("environnement"));
        sommeil.setTemperature(rs.getDouble("temperature"));
        sommeil.setNiveauBruit(rs.getString("bruit_niveau"));
        return sommeil;
    }

    @Override
    public Connection getConnection() {
        return getConn();
    }
}
