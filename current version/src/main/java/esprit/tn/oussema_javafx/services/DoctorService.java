package esprit.tn.oussema_javafx.services;

import esprit.tn.oussema_javafx.models.User;
import esprit.tn.oussema_javafx.models.UserRole;
import esprit.tn.oussema_javafx.utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorService {

    private final Connection cnx = Mydatabase.getInstance().getConnection();

    public List<User> findAllDoctors(){
        List<User> list = new ArrayList<>();

        String sql = """

                SELECT id, full_name, email, phone, address, speciality
                                       FROM user
            WHERE role='DOCTOR'
            ORDER BY full_name
        """;

        try(Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql)){

            while(rs.next()){
                User d = new User();
                d.setId(rs.getInt("id"));
                d.setFullName(rs.getString("full_name"));
                d.setEmail(rs.getString("email"));
                d.setPhone(rs.getString("phone"));
                d.setSpeciality(rs.getString("speciality"));
                d.setRole(UserRole.DOCTOR);
                d.setAddress(rs.getString("address"));


                list.add(d);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return list;
    }

    public User findById(int id){
        String sql = "SELECT * FROM user WHERE id=? AND role='DOCTOR'";

        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setInt(1,id);

            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                User d = new User();
                d.setId(rs.getInt("id"));
                d.setFullName(rs.getString("full_name"));
                d.setEmail(rs.getString("email"));
                d.setPhone(rs.getString("phone"));
                d.setSpeciality(rs.getString("speciality"));
                d.setRole(UserRole.DOCTOR);
                d.setAddress(rs.getString("address"));

                return d;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
