package com.serinity.consultationcontrol.service;

import com.serinity.consultationcontrol.model.Consultation;
import com.serinity.consultationcontrol.util.DateTimeUtil;
import com.serinity.consultationcontrol.util.Mydatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ConsultationService {
    private final Connection cnx = Mydatabase.getInstance().getConnection();

    private static final String NAME_EXPR = "COALESCE(NULLIF(TRIM(CONCAT(COALESCE(%s.firstName, ''), ' ', COALESCE(%s.lastName, ''))), ''), %s.username, %s.email)";
    private static final String BASE_SELECT = """
            SELECT c.*,
                   %s AS doctor_name,
                   %s AS patient_name
            FROM consultation c
            JOIN users du ON du.id = c.doctor_id
            LEFT JOIN profiles dp ON dp.user_id = du.id
            JOIN rapport rm ON rm.id = c.rapport_id
            JOIN users pu ON pu.id = rm.patient_id
            LEFT JOIN profiles pp ON pp.user_id = pu.id
            """.formatted(
            NAME_EXPR.formatted("dp", "dp", "dp", "du"),
            NAME_EXPR.formatted("pp", "pp", "pp", "pu"));

    public List<Consultation> findAll(){
        List<Consultation> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY c.date_consultation DESC";
        try(Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql)){
            while(rs.next()){
                list.add(map(rs));
            }
        } catch (SQLException e){ e.printStackTrace(); }
        return list;
    }

    public boolean existsByRdv(int rdvId){
        String sql = "SELECT COUNT(*) FROM consultation WHERE rendez_vous_id = ?";

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
        String sql = BASE_SELECT + " WHERE c.id=?";
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
            INSERT INTO consultation(rapport_id, rendez_vous_id, doctor_id, date_consultation, diagnostic, prescription, notes)
            VALUES(?,?,?,?,?,?,?)
        """;
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, c.getRapportId());
            if(c.getRendezVousId() == 0) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, c.getRendezVousId());
            ps.setString(3, c.getDoctorId());
            ps.setTimestamp(4, DateTimeUtil.toTimestamp(c.getDateConsultation()));
            ps.setString(5, c.getDiagnostic());
            ps.setString(6, c.getPrescription());
            ps.setString(7, c.getNotes());
            ps.executeUpdate();
        } catch (SQLException e){ e.printStackTrace(); }
    }

    public void update(Consultation c){
        String sql = """
            UPDATE consultation
            SET rapport_id=?, rendez_vous_id=?, doctor_id=?, date_consultation=?, diagnostic=?, prescription=?, notes=?
            WHERE id=?
        """;
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, c.getRapportId());
            if(c.getRendezVousId() == 0) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, c.getRendezVousId());
            ps.setString(3, c.getDoctorId());
            ps.setTimestamp(4, DateTimeUtil.toTimestamp(c.getDateConsultation()));
            ps.setString(5, c.getDiagnostic());
            ps.setString(6, c.getPrescription());
            ps.setString(7, c.getNotes());
            ps.setInt(8, c.getId());
            ps.executeUpdate();
        } catch (SQLException e){ e.printStackTrace(); }
    }

    public void delete(int id){
        try(PreparedStatement ps = cnx.prepareStatement("DELETE FROM consultation WHERE id=?")){
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e){ e.printStackTrace(); }
    }

    private Consultation map(ResultSet rs) throws SQLException{
        Consultation c = new Consultation();
        c.setId(rs.getInt("id"));
        c.setRapportId(rs.getInt("rapport_id"));
        c.setRendezVousId(rs.getInt("rendez_vous_id"));
        c.setDoctorId(rs.getString("doctor_id"));
        c.setDateConsultation(DateTimeUtil.toLocalDateTime(rs.getTimestamp("date_consultation")));
        c.setDiagnostic(rs.getString("diagnostic"));
        c.setPrescription(rs.getString("prescription"));
        c.setNotes(rs.getString("notes"));
        c.setDoctorName(rs.getString("doctor_name"));
        c.setPatientName(rs.getString("patient_name"));
        return c;
    }
}
