package esprit.tn.oussema_javafx.services;

import esprit.tn.oussema_javafx.models.RapportMedical;
import esprit.tn.oussema_javafx.utils.DateTimeUtil;
import esprit.tn.oussema_javafx.utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RapportMedicalService {
    private final Connection cnx = Mydatabase.getInstance().getConnection();

    public List<RapportMedical> findAll(){
        List<RapportMedical> list = new ArrayList<>();
        String sql = """
            SELECT rm.*, u.full_name AS patient_name
            FROM rapports rm
            JOIN user u ON u.id = rm.patient_id
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
            SELECT rm.*, u.full_name AS patient_name
            FROM rapports rm
            JOIN user u ON u.id = rm.patient_id
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
        String sql = "INSERT INTO rapports(patient_id, date_creation, resume_general) VALUES(?,?,?)";
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, r.getPatientId());
            ps.setDate(2, DateTimeUtil.toSqlDate(r.getDateCreation()));
            ps.setString(3, r.getResumeGeneral());
            ps.executeUpdate();
        } catch (SQLException e){ e.printStackTrace(); }
    }

    public void update(RapportMedical r){
        String sql = "UPDATE rapports SET patient_id=?, date_creation=?, resume_general=? WHERE id=?";
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, r.getPatientId());
            ps.setDate(2, DateTimeUtil.toSqlDate(r.getDateCreation()));
            ps.setString(3, r.getResumeGeneral());
            ps.setInt(4, r.getId());
            ps.executeUpdate();
        } catch (SQLException e){ e.printStackTrace(); }
    }

    public void delete(int id){
        try(PreparedStatement ps = cnx.prepareStatement("DELETE FROM rapports WHERE id=?")){
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e){ e.printStackTrace(); }
    }

    private RapportMedical map(ResultSet rs) throws SQLException{
        RapportMedical r = new RapportMedical();
        r.setId(rs.getInt("id"));
        r.setPatientId(rs.getInt("patient_id"));
        r.setDateCreation(DateTimeUtil.toLocalDate(rs.getDate("date_creation")));
        r.setResumeGeneral(rs.getString("resume_general"));
        r.setPatientName(rs.getString("patient_name"));
        return r;
    }
}
