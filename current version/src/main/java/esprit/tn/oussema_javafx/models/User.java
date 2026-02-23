package esprit.tn.oussema_javafx.models;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String fullName;
    private String email;
    private String phone;
    private UserRole role;
    private String speciality; // seulement si DOCTOR
    private LocalDateTime createdAt;
    private String address;
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public User() {}

    public User(int id, String fullName, String email, String phone, UserRole role, String speciality) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.speciality = speciality;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getSpeciality() { return speciality; }
    public void setSpeciality(String speciality) { this.speciality = speciality; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override public String toString() {
        return fullName + " (" + role + ")";
    }
}
