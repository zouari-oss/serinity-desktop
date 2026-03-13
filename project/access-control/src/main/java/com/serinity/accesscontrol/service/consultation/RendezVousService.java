package com.serinity.accesscontrol.service.consultation;

import com.serinity.accesscontrol.model.consultation.RdvStatus;
import com.serinity.accesscontrol.model.consultation.RendezVous;
import com.serinity.accesscontrol.model.consultation.User;
import com.serinity.accesscontrol.util.consultation.DateTimeUtil;
import com.serinity.accesscontrol.util.consultation.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RendezVousService {
    private final Connection cnx = Mydatabase.getInstance().getConnection();

    public List<RendezVous> findAll(){
        List<RendezVous> list = new ArrayList<>();
        String sql = """
            SELECT r.*,
                   dp.full_name AS doctor_name,
                   pp.full_name AS patient_name
            FROM rendez_vous r
            JOIN user dp ON dp.id = r.doctor_id
            JOIN user pp ON pp.id = r.patient_id
            ORDER BY r.date_time DESC
        """;
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)){
            while(rs.next()){
                list.add(map(rs));
            }
        } catch (SQLException e){ e.printStackTrace(); }
        return list;
    }

    // ✅ IMPORTANT: Mes réservations (patient)
    public List<RendezVous> findAllByPatient(int patientId){
        List<RendezVous> list = new ArrayList<>();
        String sql = """
            SELECT r.*,
                   dp.full_name AS doctor_name,
                   pp.full_name AS patient_name
            FROM rendez_vous r
            JOIN user dp ON dp.id = r.doctor_id
            JOIN user pp ON pp.id = r.patient_id
            WHERE r.patient_id=?
            ORDER BY r.date_time DESC
        """;
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, patientId);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    list.add(map(rs));
                }
            }
        }catch(SQLException e){ e.printStackTrace(); }
        return list;
    }

    public RendezVous findById(int id){
        String sql = """
            SELECT r.*,
                   dp.full_name AS doctor_name,
                   pp.full_name AS patient_name
            FROM rendez_vous r
            JOIN user dp ON dp.id = r.doctor_id
            JOIN user pp ON pp.id = r.patient_id
            WHERE r.id=?
        """;
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, id);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()) return map(rs);
            }
        } catch (SQLException e){ e.printStackTrace(); }
        return null;
    }

    public void insert(RendezVous r){
        String sql = """
        INSERT INTO rendez_vous(patient_id, doctor_id, motif, description, date_time, status)
        VALUES(?,?,?,?,?,?)
    """;

        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, r.getPatientId());
            ps.setInt(2, r.getDoctorId());
            ps.setString(3, r.getMotif());
            ps.setString(4, r.getDescription());
            ps.setTimestamp(5, DateTimeUtil.toTimestamp(r.getDateTime()));
            ps.setString(6, "EN_ATTENTE");

            ps.executeUpdate();
        } catch (SQLException e){ e.printStackTrace(); }
    }

    public void update(RendezVous r){
        String sql = """
            UPDATE rendez_vous
            SET patient_id=?, doctor_id=?, motif=?, date_time=?, status=?, proposed_date_time=?, doctor_note=?
            WHERE id=?
        """;
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, r.getPatientId());
            ps.setInt(2, r.getDoctorId());
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
        r.setPatientId(rs.getInt("patient_id"));
        r.setDoctorId(rs.getInt("doctor_id"));
        r.setMotif(rs.getString("motif"));
        r.setDescription(rs.getString("description"));
        r.setDateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("date_time")));
        r.setStatus(RdvStatus.valueOf(rs.getString("status")));
        r.setProposedDateTime(DateTimeUtil.toLocalDateTime(rs.getTimestamp("proposed_date_time")));
        r.setDoctorNote(rs.getString("doctor_note"));
        r.setDoctorName(rs.getString("doctor_name"));
        r.setPatientName(rs.getString("patient_name"));
        return r;
    }




    // RDV du médecin connecté
    public List<RendezVous> findAllByDoctor(int doctorId){
        List<RendezVous> list = new ArrayList<>();
        String sql = """
        SELECT r.*,
               dp.full_name AS doctor_name,
               pp.full_name AS patient_name
        FROM rendez_vous r
        JOIN user dp ON dp.id = r.doctor_id
        JOIN user pp ON pp.id = r.patient_id
        WHERE r.doctor_id=?
        ORDER BY r.date_time DESC
    """;
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, doctorId);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    list.add(map(rs));
                }
            }
        }catch(SQLException e){ e.printStackTrace(); }
        return list;
    }













    public List<User> findPatientsByDoctor(int doctorId){
        List<User> list = new ArrayList<>();

        String sql = """
        SELECT DISTINCT u.id, u.full_name, u.email, u.phone, u.address
        FROM rendez_vous r
        JOIN user u ON u.id = r.patient_id
        WHERE r.doctor_id = ?
        ORDER BY u.full_name
    """;

        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, doctorId);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    User u = new User();
                    u.setId(rs.getInt("id"));
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
