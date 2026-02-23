package esprit.tn.oussema_javafx.services;

import esprit.tn.oussema_javafx.models.User;
import esprit.tn.oussema_javafx.models.UserRole;
import esprit.tn.oussema_javafx.utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserLookupService {
    private final Connection cnx = Mydatabase.getInstance().getConnection();

    public List<User> getDoctors() {
        return getByRole(UserRole.DOCTOR);
    }

    public List<User> getPatients() {
        return getByRole(UserRole.PATIENT);
    }

    public User findById(int id){
        String sql = "SELECT id, full_name, email, phone, role, speciality, created_at FROM user WHERE id=?";
        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1, id);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setFullName(rs.getString("full_name"));
                    u.setEmail(rs.getString("email"));
                    u.setPhone(rs.getString("phone"));
                    u.setRole(UserRole.valueOf(rs.getString("role")));
                    u.setSpeciality(rs.getString("speciality"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if(ts != null) u.setCreatedAt(ts.toLocalDateTime());
                    return u;
                }
            }
        }catch(SQLException e){ e.printStackTrace(); }
        return null;
    }

    private List<User> getByRole(UserRole role){
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, full_name, email, phone, role, speciality, created_at FROM user WHERE role=? ORDER BY full_name";
        try (PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setString(1, role.name());
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setFullName(rs.getString("full_name"));
                    u.setEmail(rs.getString("email"));
                    u.setPhone(rs.getString("phone"));
                    u.setRole(UserRole.valueOf(rs.getString("role")));
                    u.setSpeciality(rs.getString("speciality"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if(ts != null) u.setCreatedAt(ts.toLocalDateTime());
                    list.add(u);
                }
            }
        } catch (SQLException e){ e.printStackTrace(); }
        return list;
    }
}
