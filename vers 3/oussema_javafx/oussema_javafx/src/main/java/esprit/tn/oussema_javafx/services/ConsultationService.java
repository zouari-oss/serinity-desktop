package esprit.tn.oussema_javafx.services;

import esprit.tn.oussema_javafx.models.Consultation;
import esprit.tn.oussema_javafx.utils.DateTimeUtil;
import esprit.tn.oussema_javafx.utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsultationService {
    private final Connection cnx = Mydatabase.getInstance().getConnection();

    public List<Consultation> findAll(){
        List<Consultation> list = new ArrayList<>();
        String sql = """
            SELECT c.*,
                   d.full_name AS doctor_name,
                   p.full_name AS patient_name
            FROM consultations c
            JOIN user d ON d.id = c.doctor_id
            JOIN rapports rm ON rm.id = c.rapport_id
            JOIN user p ON p.id = rm.patient_id
            ORDER BY c.date_consultation DESC
        """;
        try(Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql)){
            while(rs.next()){
                list.add(map(rs));
            }
        } catch (SQLException e){ e.printStackTrace(); }
        return list;
    }
    public boolean existsByRdv(int rdvId){
        String sql = "SELECT COUNT(*) FROM consultations WHERE rendez_vous_id = ?";

        try(PreparedStatement ps = cnx.prepareStatement(sql)){

            ps.setInt(1, rdvId);

            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1) > 0;
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public Consultation findById(int id){
        String sql = """
            SELECT c.*,
                   d.full_name AS doctor_name,
                   p.full_name AS patient_name
            FROM consultations c
            JOIN user d ON d.id = c.doctor_id
            JOIN rapports rm ON rm.id = c.rapport_id
            JOIN user p ON p.id = rm.patient_id
            WHERE c.id=?
        """;
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, id);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()) return map(rs);
            }
        } catch (SQLException e){ e.printStackTrace(); }
        return null;
    }

    public void insert(Consultation c){
        String sql = """
            INSERT INTO consultations(rapport_id, rendez_vous_id, doctor_id, date_consultation, diagnostic, prescription, notes)
            VALUES(?,?,?,?,?,?,?)
        """;
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, c.getRapportId());
            if(c.getRendezVousId() == 0) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, c.getRendezVousId());
            ps.setInt(3, c.getDoctorId());
            ps.setTimestamp(4, DateTimeUtil.toTimestamp(c.getDateConsultation()));
            ps.setString(5, c.getDiagnostic());
            ps.setString(6, c.getPrescription());
            ps.setString(7, c.getNotes());
            ps.executeUpdate();
        } catch (SQLException e){ e.printStackTrace(); }
    }

    public void update(Consultation c){
        String sql = """
            UPDATE consultations
            SET rapport_id=?, rendez_vous_id=?, doctor_id=?, date_consultation=?, diagnostic=?, prescription=?, notes=?
            WHERE id=?
        """;
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, c.getRapportId());
            if(c.getRendezVousId() == 0) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, c.getRendezVousId());
            ps.setInt(3, c.getDoctorId());
            ps.setTimestamp(4, DateTimeUtil.toTimestamp(c.getDateConsultation()));
            ps.setString(5, c.getDiagnostic());
            ps.setString(6, c.getPrescription());
            ps.setString(7, c.getNotes());
            ps.setInt(8, c.getId());
            ps.executeUpdate();
        } catch (SQLException e){ e.printStackTrace(); }
    }

    public void delete(int id){
        try(PreparedStatement ps = cnx.prepareStatement("DELETE FROM consultations WHERE id=?")){
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e){ e.printStackTrace(); }
    }

    private Consultation map(ResultSet rs) throws SQLException{
        Consultation c = new Consultation();
        c.setId(rs.getInt("id"));
        c.setRapportId(rs.getInt("rapport_id"));
        c.setRendezVousId(rs.getInt("rendez_vous_id"));
        c.setDoctorId(rs.getInt("doctor_id"));
        c.setDateConsultation(DateTimeUtil.toLocalDateTime(rs.getTimestamp("date_consultation")));
        c.setDiagnostic(rs.getString("diagnostic"));
        c.setPrescription(rs.getString("prescription"));
        c.setNotes(rs.getString("notes"));
        c.setDoctorName(rs.getString("doctor_name"));
        c.setPatientName(rs.getString("patient_name"));
        return c;
    }
}
