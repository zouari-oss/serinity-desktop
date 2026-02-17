package com.serinity.sleepcontrol.dao.impl;

import com.serinity.sleepcontrol.dao.ReveDao;
import com.serinity.sleepcontrol.model.Reve;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReveDaoJdbc implements ReveDao {

    private Connection connection;

    public ReveDaoJdbc(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void ajouter(Reve reve) throws SQLException {
        String sql = "INSERT INTO reves (sommeil_id, titre, description, humeur, type_reve, " +
                "intensite, couleur, emotions, symboles, recurrent) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, reve.getSommeilId());
            stmt.setString(2, reve.getTitre());
            stmt.setString(3, reve.getDescription());
            stmt.setString(4, reve.getHumeur());
            stmt.setString(5, reve.getTypeReve());
            stmt.setInt(6, reve.getIntensite());
            stmt.setBoolean(7, reve.isCouleur());
            stmt.setString(8, reve.getEmotions());
            stmt.setString(9, reve.getSymboles());
            stmt.setBoolean(10, reve.isRecurrent());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    reve.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Reve> listerTous() throws SQLException {
        List<Reve> reves = new ArrayList<>();
        String sql = "SELECT * FROM reves ORDER BY id DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reves.add(mapResultSetToReve(rs));
            }
        }
        return reves;
    }

    @Override
    public Reve trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM reves WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReve(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Reve> trouverParSommeilId(int sommeilId) throws SQLException {
        List<Reve> reves = new ArrayList<>();
        String sql = "SELECT * FROM reves WHERE sommeil_id = ? ORDER BY id DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sommeilId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reves.add(mapResultSetToReve(rs));
                }
            }
        }
        return reves;
    }

    @Override
    public void modifier(Reve reve) throws SQLException {
        String sql = "UPDATE reves SET sommeil_id=?, titre=?, description=?, humeur=?, " +
                "type_reve=?, intensite=?, couleur=?, emotions=?, symboles=?, recurrent=? " +
                "WHERE id=?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reve.getSommeilId());
            stmt.setString(2, reve.getTitre());
            stmt.setString(3, reve.getDescription());
            stmt.setString(4, reve.getHumeur());
            stmt.setString(5, reve.getTypeReve());
            stmt.setInt(6, reve.getIntensite());
            stmt.setBoolean(7, reve.isCouleur());
            stmt.setString(8, reve.getEmotions());
            stmt.setString(9, reve.getSymboles());
            stmt.setBoolean(10, reve.isRecurrent());
            stmt.setInt(11, reve.getId());

            stmt.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM reves WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Reve> rechercher(String critere) throws SQLException {
        List<Reve> reves = new ArrayList<>();
        String sql = "SELECT * FROM reves WHERE titre LIKE ? OR description LIKE ? " +
                "OR emotions LIKE ? OR symboles LIKE ? ORDER BY id DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String pattern = "%" + critere + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reves.add(mapResultSetToReve(rs));
                }
            }
        }
        return reves;
    }

    @Override
    public List<Reve> filtrerParType(String type) throws SQLException {
        List<Reve> reves = new ArrayList<>();
        String sql = "SELECT * FROM reves WHERE type_reve = ? ORDER BY id DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, type);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reves.add(mapResultSetToReve(rs));
                }
            }
        }
        return reves;
    }

    @Override
    public List<Object[]> statistiquesParType() throws SQLException {
        List<Object[]> stats = new ArrayList<>();
        String sql = "SELECT type_reve, COUNT(*) as nombre, AVG(intensite) as intensite_moy " +
                "FROM reves GROUP BY type_reve";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.add(new Object[]{
                        rs.getString("type_reve"),
                        rs.getInt("nombre"),
                        rs.getDouble("intensite_moy")
                });
            }
        }
        return stats;
    }

    @Override
    public double calculerIntensiteMoyenne() throws SQLException {
        String sql = "SELECT AVG(intensite) as moyenne FROM reves";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("moyenne");
            }
        }
        return 0;
    }

    // Méthode privée pour mapper ResultSet
    private Reve mapResultSetToReve(ResultSet rs) throws SQLException {
        Reve reve = new Reve();
        reve.setId(rs.getInt("id"));
        reve.setSommeilId(rs.getInt("sommeil_id"));
        reve.setTitre(rs.getString("titre"));
        reve.setDescription(rs.getString("description"));
        reve.setHumeur(rs.getString("humeur"));
        reve.setTypeReve(rs.getString("type_reve"));
        reve.setIntensite(rs.getInt("intensite"));
        reve.setCouleur(rs.getBoolean("couleur"));
        reve.setEmotions(rs.getString("emotions"));
        reve.setSymboles(rs.getString("symboles"));
        reve.setRecurrent(rs.getBoolean("recurrent"));
        return reve;
    }
}
