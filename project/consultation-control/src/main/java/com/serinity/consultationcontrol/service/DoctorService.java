package com.serinity.consultationcontrol.service;

import com.serinity.consultationcontrol.model.User;
import com.serinity.consultationcontrol.model.UserRole;
import com.serinity.consultationcontrol.util.Mydatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DoctorService {

    private final Connection cnx = Mydatabase.getInstance().getConnection();

    private static final String DOCTOR_SELECT = """
            SELECT u.id,
                   COALESCE(NULLIF(TRIM(CONCAT(COALESCE(p.firstName, ''), ' ', COALESCE(p.lastName, ''))), ''), p.username, u.email) AS full_name,
                   u.email,
                   p.phone,
                   TRIM(CONCAT_WS(', ', NULLIF(p.state, ''), NULLIF(p.country, ''))) AS address,
                   NULL AS speciality
            FROM users u
            LEFT JOIN profiles p ON p.user_id = u.id
            WHERE u.role='THERAPIST'
            """;

    public List<User> findAllDoctors(){
        List<User> list = new ArrayList<>();
        String sql = DOCTOR_SELECT + " ORDER BY full_name";

        try(Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql)){
            while(rs.next()){
                list.add(mapDoctor(rs));
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return list;
    }

    public User findById(String id){
        String sql = DOCTOR_SELECT + " AND u.id=?";

        try(PreparedStatement ps = cnx.prepareStatement(sql)){
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return mapDoctor(rs);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private User mapDoctor(ResultSet rs) throws Exception {
        User d = new User();
        d.setId(rs.getString("id"));
        d.setFullName(rs.getString("full_name"));
        d.setEmail(rs.getString("email"));
        d.setPhone(rs.getString("phone"));
        d.setSpeciality(rs.getString("speciality"));
        d.setRole(UserRole.DOCTOR);
        d.setAddress(rs.getString("address"));
        return d;
    }
}
