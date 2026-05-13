package com.serinity.consultationcontrol.service;

import com.serinity.consultationcontrol.model.RdvStatus;
import com.serinity.consultationcontrol.model.RendezVous;
import com.serinity.consultationcontrol.model.User;
import com.serinity.consultationcontrol.util.DateTimeUtil;
import com.serinity.consultationcontrol.util.Mydatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RendezVousService {
    private final Connection cnx = Mydatabase.getInstance().getConnection();

    private static final String NAME_EXPR = "COALESCE(NULLIF(TRIM(CONCAT(COALESCE(%s.firstName, ''), ' ', COALESCE(%s.lastName, ''))), ''), %s.username, %s.email)";

    private static final String BASE_SELECT = """
            SELECT r.*,
                   %s AS doctor_name,
                   %s AS patient_name
            FROM rendez_vous r
            JOIN users du ON du.id = r.doctor_id
            LEFT JOIN profiles dp ON dp.user_id = du.id
            JOIN users pu ON pu.id = r.patient_id
            LEFT JOIN profiles pp ON pp.user_id = pu.id
            """.formatted(
            NAME_EXPR.formatted("dp", "dp", "dp", "du"),
            NAME_EXPR.formatted("pp", "pp", "pp", "pu"));

    public List<RendezVous> findAll(){
        List<RendezVous> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY r.date_time DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)){
            while(rs.next()){
                list.add(map(rs));
            }
        } catch (SQLException e){ e.printStackTrace(); }
        return list;
    }

    public List<RendezVous> findAllByPatient(String patientId){
        List<RendezVous> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE r.patient_id=? ORDER BY r.date_time DESC";
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setString(1, patientId);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    list.add(map(rs));
                }
            }
        }catch(SQLException e){ e.printStackTrace(); }
        return list;
    }

    public RendezVous findById(int id){
        String sql = BASE_SELECT + " WHERE r.id=?";
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, id);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()) return map(rs);
            }
        } catch (SQLException e){ e.printStackTrace(); }
        return null;
    }

    public boolean insert(RendezVous r){
        String sql = """
        INSERT INTO rendez_vous(patient_id, doctor_id, motif, description, date_time, status, created_at)
        VALUES(?,?,?,?,?,?,?)
    """;

        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setString(1, r.getPatientId());
            ps.setString(2, r.getDoctorId());
            ps.setString(3, r.getMotif());
            ps.setString(4, r.getDescription());
            ps.setTimestamp(5, DateTimeUtil.toTimestamp(r.getDateTime()));
            ps.setString(6, "EN_ATTENTE");
            ps.setTimestamp(7, DateTimeUtil.toTimestamp(LocalDateTime.now()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e){
            throw new IllegalStateException("Failed to save rendez-vous: " + e.getMessage(), e);
        }
    }

    public void update(RendezVous r){
        String sql = """
            UPDATE rendez_vous
            SET patient_id=?, doctor_id=?, motif=?, date_time=?, status=?, proposed_date_time=?, doctor_note=?
            WHERE id=?
        """;
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setString(1, r.getPatientId());
            ps.setString(2, r.getDoctorId());
            ps.setString(3, r.getMotif());
            ps.setTimestamp(4, DateTimeUtil.toTimestamp(r.getDateTime()));
            ps.setString(5, r.getStatus().name());
            ps.setTimestamp(6, DateTimeUtil.toTimestamp(r.getProposedDateTime()));
            ps.setString(7, r.getDoctorNote());
            ps.setInt(8, r.getId());
            ps.executeUpdate();
        } catch (SQLException e){ e.printStackTrace(); }
    }

    public void delete(int id){
        try(PreparedStatement ps = cnx.prepareStatement("DELETE FROM rendez_vous WHERE id=?")){
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e){ e.printStackTrace(); }
    }

    private RendezVous map(ResultSet rs) throws SQLException{
        RendezVous r = new RendezVous();
        r.setId(rs.getInt("id"));
        r.setPatientId(rs.getString("patient_id"));
        r.setDoctorId(rs.getString("doctor_id"));
        r.setMotif(rs.getString("motif"));
        r.setDescription(rs.getString("description"));
        r.setDateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("date_time")));
        r.setStatus(RdvStatus.fromDatabase(rs.getString("status")));
        r.setProposedDateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("proposed_date_time")));
        r.setDoctorNote(rs.getString("doctor_note"));
        r.setDoctorName(rs.getString("doctor_name"));
        r.setPatientName(rs.getString("patient_name"));
        return r;
    }

    public List<RendezVous> findAllByDoctor(String doctorId){
        List<RendezVous> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE r.doctor_id=? ORDER BY r.date_time DESC";
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setString(1, doctorId);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    list.add(map(rs));
                }
            }
        }catch(SQLException e){ e.printStackTrace(); }
        return list;
    }

    public List<User> findPatientsByDoctor(String doctorId){
        List<User> list = new ArrayList<>();

        String sql = """
        SELECT DISTINCT u.id,
               COALESCE(NULLIF(TRIM(CONCAT(COALESCE(p.firstName, ''), ' ', COALESCE(p.lastName, ''))), ''), p.username, u.email) AS full_name,
               u.email,
               p.phone,
               TRIM(CONCAT_WS(', ', NULLIF(p.state, ''), NULLIF(p.country, ''))) AS address
        FROM rendez_vous r
        JOIN users u ON u.id = r.patient_id
        LEFT JOIN profiles p ON p.user_id = u.id
        WHERE r.doctor_id = ?
        ORDER BY full_name
    """;

        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setString(1, doctorId);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    User u = new User();
                    u.setId(rs.getString("id"));
                    u.setFullName(rs.getString("full_name"));
                    u.setEmail(rs.getString("email"));
                    u.setPhone(rs.getString("phone"));
                    u.setAddress(rs.getString("address"));
                    list.add(u);
                }
            }
        }catch(SQLException e){ e.printStackTrace(); }

        return list;
    }
}
