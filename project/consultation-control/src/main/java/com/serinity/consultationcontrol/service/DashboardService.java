package com.serinity.consultationcontrol.service;

import com.serinity.consultationcontrol.util.Mydatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardService {

    private final Connection cnx = Mydatabase.getInstance().getConnection();

    public int totalPatients(String doctorId){
        String sql = """
            SELECT COUNT(DISTINCT patient_id)
            FROM rendez_vous
            WHERE doctor_id = ?
        """;

        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setString(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getInt(1);
        }catch(Exception e){ e.printStackTrace(); }
        return 0;
    }

    public int todayAppointments(String doctorId){
        String sql = """
            SELECT COUNT(*)
            FROM rendez_vous
            WHERE doctor_id = ?
            AND DATE(date_time) = CURDATE()
        """;

        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setString(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getInt(1);
        }catch(Exception e){ e.printStackTrace(); }
        return 0;
    }

    public int pendingAppointments(String doctorId){
        String sql = """
            SELECT COUNT(*)
            FROM rendez_vous
            WHERE doctor_id = ?
            AND status = 'EN_ATTENTE'
        """;

        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setString(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getInt(1);
        }catch(Exception e){ e.printStackTrace(); }
        return 0;
    }

    public int totalConsultations(String doctorId){
        String sql = """
            SELECT COUNT(*)
            FROM consultation
            WHERE doctor_id = ?
        """;

        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setString(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getInt(1);
        }catch(Exception e){ e.printStackTrace(); }
        return 0;
    }

    public double acceptanceRate(String doctorId){
        String sql = """
            SELECT 
                SUM(CASE WHEN status='APPROUVE' THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(*), 0)
            FROM rendez_vous
            WHERE doctor_id = ?
        """;

        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setString(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getDouble(1);
        }catch(Exception e){ e.printStackTrace(); }
        return 0;
    }

    public ResultSet rdvLast7Days(String doctorId) throws Exception{
        String sql = """
        SELECT DATE(date_time) as d, COUNT(*) as total
        FROM rendez_vous
        WHERE doctor_id=?
        AND date_time >= CURDATE() - INTERVAL 7 DAY
        GROUP BY DATE(date_time)
        ORDER BY d
    """;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, doctorId);
        return ps.executeQuery();
    }

    public ResultSet consultationsLast7Days(String doctorId) throws Exception{
        String sql = """
        SELECT DATE(date_consultation) as d, COUNT(*) as total
        FROM consultation
        WHERE doctor_id=?
        AND date_consultation >= CURDATE() - INTERVAL 7 DAY
        GROUP BY DATE(date_consultation)
    """;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, doctorId);
        return ps.executeQuery();
    }

    public ResultSet statusDistribution(String doctorId) throws Exception{
        String sql = """
        SELECT status, COUNT(*) total
        FROM rendez_vous
        WHERE doctor_id=?
        GROUP BY status
    """;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, doctorId);
        return ps.executeQuery();
    }

    public ResultSet monthlyRdv(String doctorId) throws Exception{
        String sql = """
        SELECT MONTH(date_time) m, COUNT(*) total
        FROM rendez_vous
        WHERE doctor_id=?
        GROUP BY MONTH(date_time)
    """;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, doctorId);
        return ps.executeQuery();
    }

    public ResultSet topPatients(String doctorId) throws Exception{
        String sql = """
        SELECT COALESCE(NULLIF(TRIM(CONCAT(COALESCE(p.firstName, ''), ' ', COALESCE(p.lastName, ''))), ''), p.username, u.email) AS full_name, COUNT(*) visits
        FROM rendez_vous r
        JOIN users u ON u.id = r.patient_id
        LEFT JOIN profiles p ON p.user_id = u.id
        WHERE r.doctor_id=?
        GROUP BY full_name
        ORDER BY visits DESC
        LIMIT 5
    """;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, doctorId);
        return ps.executeQuery();
    }
}
