package esprit.tn.oussema_javafx.services;

import esprit.tn.oussema_javafx.utils.Mydatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardService {

    private final Connection cnx = Mydatabase.getInstance().getConnection();

    // 1. Total patients
    public int totalPatients(int doctorId){
        String sql = """
            SELECT COUNT(DISTINCT patient_id)
            FROM rendez_vous
            WHERE doctor_id = ?
        """;

        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getInt(1);
        }catch(Exception e){ e.printStackTrace(); }
        return 0;
    }

    // 2. RDV aujourd'hui
    public int todayAppointments(int doctorId){
        String sql = """
            SELECT COUNT(*)
            FROM rendez_vous
            WHERE doctor_id = ?
            AND DATE(date_time) = CURDATE()
        """;

        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getInt(1);
        }catch(Exception e){ e.printStackTrace(); }
        return 0;
    }

    // 3. RDV en attente
    public int pendingAppointments(int doctorId){
        String sql = """
            SELECT COUNT(*)
            FROM rendez_vous
            WHERE doctor_id = ?
            AND status = 'EN_ATTENTE'
        """;

        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getInt(1);
        }catch(Exception e){ e.printStackTrace(); }
        return 0;
    }

    // 4. consultations réalisées
    public int totalConsultations(int doctorId){
        String sql = """
            SELECT COUNT(*)
            FROM consultations
            WHERE doctor_id = ?
        """;

        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getInt(1);
        }catch(Exception e){ e.printStackTrace(); }
        return 0;
    }

    // 5. taux d’acceptation
    public double acceptanceRate(int doctorId){
        String sql = """
            SELECT 
                SUM(CASE WHEN status='ACCEPTE' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)
            FROM rendez_vous
            WHERE doctor_id = ?
        """;

        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getDouble(1);
        }catch(Exception e){ e.printStackTrace(); }
        return 0;
    }
    public ResultSet rdvLast7Days(int doctorId) throws Exception{
        String sql = """
        SELECT DATE(date_time) as d, COUNT(*) as total
        FROM rendez_vous
        WHERE doctor_id=?
        AND date_time >= CURDATE() - INTERVAL 7 DAY
        GROUP BY DATE(date_time)
        ORDER BY d
    """;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, doctorId);
        return ps.executeQuery();
    }
    public ResultSet consultationsLast7Days(int doctorId) throws Exception{
        String sql = """
        SELECT DATE(date_consultation) as d, COUNT(*) as total
        FROM consultations
        WHERE doctor_id=?
        AND date_consultation >= CURDATE() - INTERVAL 7 DAY
        GROUP BY DATE(date_consultation)
    """;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, doctorId);
        return ps.executeQuery();
    }
    public ResultSet statusDistribution(int doctorId) throws Exception{
        String sql = """
        SELECT status, COUNT(*) total
        FROM rendez_vous
        WHERE doctor_id=?
        GROUP BY status
    """;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, doctorId);
        return ps.executeQuery();
    }
    public ResultSet monthlyRdv(int doctorId) throws Exception{
        String sql = """
        SELECT MONTH(date_time) m, COUNT(*) total
        FROM rendez_vous
        WHERE doctor_id=?
        GROUP BY MONTH(date_time)
    """;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, doctorId);
        return ps.executeQuery();
    }
    public ResultSet topPatients(int doctorId) throws Exception{
        String sql = """
        SELECT u.full_name, COUNT(*) visits
        FROM rendez_vous r
        JOIN user u ON u.id = r.patient_id
        WHERE r.doctor_id=?
        GROUP BY u.full_name
        ORDER BY visits DESC
        LIMIT 5
    """;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, doctorId);
        return ps.executeQuery();
    }
}