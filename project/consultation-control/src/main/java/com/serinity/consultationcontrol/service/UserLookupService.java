package com.serinity.consultationcontrol.service;

import com.serinity.consultationcontrol.model.User;
import com.serinity.consultationcontrol.model.UserRole;
import com.serinity.consultationcontrol.util.Mydatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UserLookupService {
    private final Connection cnx = Mydatabase.getInstance().getConnection();

    private static final String USER_SELECT = """
            SELECT u.id,
                   COALESCE(NULLIF(TRIM(CONCAT(COALESCE(p.firstName, ''), ' ', COALESCE(p.lastName, ''))), ''), p.username, u.email) AS full_name,
                   u.email,
                   p.phone,
                   CASE WHEN u.role='THERAPIST' THEN 'DOCTOR' ELSE u.role END AS role,
                   NULL AS speciality,
                   u.created_at,
                   TRIM(CONCAT_WS(', ', NULLIF(p.state, ''), NULLIF(p.country, ''))) AS address
            FROM users u
            LEFT JOIN profiles p ON p.user_id = u.id
            """;

    public List<User> getDoctors() {
        return getByRole(UserRole.DOCTOR);
    }

    public List<User> getPatients() {
        return getByRole(UserRole.PATIENT);
    }

    public User findById(String id){
        String sql = USER_SELECT + " WHERE u.id=?";
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setString(1, id);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return mapUser(rs);
                }
            }
        }catch(SQLException e){ e.printStackTrace(); }
        return null;
    }

    private List<User> getByRole(UserRole role){
        List<User> list = new ArrayList<>();
        String sql = USER_SELECT + " WHERE u.role=? ORDER BY full_name";
        try (PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setString(1, role == UserRole.DOCTOR ? "THERAPIST" : role.name());
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    list.add(mapUser(rs));
                }
            }
        } catch (SQLException e){ e.printStackTrace(); }
        return list;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getString("id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setRole(UserRole.valueOf(rs.getString("role")));
        u.setSpeciality(rs.getString("speciality"));
        u.setAddress(rs.getString("address"));
        Timestamp ts = rs.getTimestamp("created_at");
        if(ts != null) u.setCreatedAt(ts.toLocalDateTime());
        return u;
    }
}
