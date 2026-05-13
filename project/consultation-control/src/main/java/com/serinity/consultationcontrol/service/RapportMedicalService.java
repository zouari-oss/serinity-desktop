package com.serinity.consultationcontrol.service;

import com.serinity.consultationcontrol.model.RapportMedical;
import com.serinity.consultationcontrol.util.DateTimeUtil;
import com.serinity.consultationcontrol.util.Mydatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RapportMedicalService {
    private final Connection cnx = Mydatabase.getInstance().getConnection();

    public List<RapportMedical> findAll(){
        List<RapportMedical> list = new ArrayList<>();
        String sql = """
            SELECT rm.*,
                   COALESCE(NULLIF(TRIM(CONCAT(COALESCE(p.firstName, ''), ' ', COALESCE(p.lastName, ''))), ''), p.username, u.email) AS patient_name
            FROM rapport rm
            JOIN users u ON u.id = rm.patient_id
            LEFT JOIN profiles p ON p.user_id = u.id
            ORDER BY rm.date_creation DESC
        """;
        try(Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql)){
            while(rs.next()){
                list.add(map(rs));
            }
        } catch (SQLException e){ e.printStackTrace(); }
        return list;
    }

    public RapportMedical findById(int id){
        String sql = """
            SELECT rm.*,
                   COALESCE(NULLIF(TRIM(CONCAT(COALESCE(p.firstName, ''), ' ', COALESCE(p.lastName, ''))), ''), p.username, u.email) AS patient_name
            FROM rapport rm
            JOIN users u ON u.id = rm.patient_id
            LEFT JOIN profiles p ON p.user_id = u.id
            WHERE rm.id=?
        """;
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, id);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()) return map(rs);
            }
        } catch (SQLException e){ e.printStackTrace(); }
        return null;
    }

    public void insert(RapportMedical r){
        String sql = "INSERT INTO rapport(patient_id, date_creation, resume_general) VALUES(?,?,?)";
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setString(1, r.getPatientId());
            ps.setDate(2, DateTimeUtil.toSqlDate(r.getDateCreation()));
            ps.setString(3, r.getResumeGeneral());
            ps.executeUpdate();
        } catch (SQLException e){ e.printStackTrace(); }
    }

    public void update(RapportMedical r){
        String sql = "UPDATE rapport SET patient_id=?, date_creation=?, resume_general=? WHERE id=?";
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setString(1, r.getPatientId());
            ps.setDate(2, DateTimeUtil.toSqlDate(r.getDateCreation()));
            ps.setString(3, r.getResumeGeneral());
            ps.setInt(4, r.getId());
            ps.executeUpdate();
        } catch (SQLException e){ e.printStackTrace(); }
    }

    public void delete(int id){
        try(PreparedStatement ps = cnx.prepareStatement("DELETE FROM rapport WHERE id=?")){
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e){ e.printStackTrace(); }
    }

    private RapportMedical map(ResultSet rs) throws SQLException{
        RapportMedical r = new RapportMedical();
        r.setId(rs.getInt("id"));
        r.setPatientId(rs.getString("patient_id"));
        r.setDateCreation(DateTimeUtil.toLocalDate(rs.getDate("date_creation")));
        r.setResumeGeneral(rs.getString("resume_general"));
        r.setPatientName(rs.getString("patient_name"));
        return r;
    }
}
