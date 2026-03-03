package services;

import esprit.tn.oussema_javafx.models.RdvStatus;
import esprit.tn.oussema_javafx.models.RendezVous;
import esprit.tn.oussema_javafx.services.RendezVousService;
import esprit.tn.oussema_javafx.utils.Mydatabase;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RendezVousServiceTest {

    private static RendezVousService service;
    private static Connection cnx;

    private static int patientId;
    private static int doctorId;
    private static int rdvId;

    /* ================= SETUP ================= */

    @BeforeAll
    static void init() throws Exception {

        service = new RendezVousService();
        cnx = Mydatabase.getInstance().getConnection();

        System.out.println("=== STARTING RDV TESTS ===");

        /* ---------- CREATE PATIENT ---------- */
        PreparedStatement ps1 = cnx.prepareStatement(
                "INSERT INTO user(full_name,email,phone,role,address,created_at) VALUES(?,?,?,?,?,NOW())",
                Statement.RETURN_GENERATED_KEYS);

        ps1.setString(1,"JUnit Patient");
        ps1.setString(2,"patient@junit.com");
        ps1.setString(3,"11111111");
        ps1.setString(4,"PATIENT");
        ps1.setString(5,"Test Address");

        ps1.executeUpdate();
        ResultSet rs1 = ps1.getGeneratedKeys();
        rs1.next();
        patientId = rs1.getInt(1);

        /* ---------- CREATE DOCTOR ---------- */
        PreparedStatement ps2 = cnx.prepareStatement(
                "INSERT INTO user(full_name,email,phone,role,speciality,address,created_at) VALUES(?,?,?,?,?,?,NOW())",
                Statement.RETURN_GENERATED_KEYS);

        ps2.setString(1,"JUnit Doctor");
        ps2.setString(2,"doctor@junit.com");
        ps2.setString(3,"22222222");
        ps2.setString(4,"DOCTOR");
        ps2.setString(5,"Cardiologue");
        ps2.setString(6,"Clinic Street");

        ps2.executeUpdate();
        ResultSet rs2 = ps2.getGeneratedKeys();
        rs2.next();
        doctorId = rs2.getInt(1);
    }

    /* ================= INSERT ================= */

    @Test
    @Order(1)
    void testInsertRdv(){

        RendezVous r = new RendezVous();
        r.setPatientId(patientId);
        r.setDoctorId(doctorId);
        r.setMotif("Douleur poitrine");
        r.setDescription("Test JUnit RDV");
        r.setDateTime(LocalDateTime.now().plusDays(1));

        service.insert(r);

        List<RendezVous> list = service.findAllByPatient(patientId);

        RendezVous inserted = list.stream()
                .filter(x -> "Douleur poitrine".equals(x.getMotif()))
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(inserted);

        rdvId = inserted.getId();
    }

    /* ================= FIND BY ID ================= */

    @Test
    @Order(2)
    void testFindById(){

        RendezVous r = service.findById(rdvId);

        Assertions.assertNotNull(r);
        Assertions.assertEquals(patientId, r.getPatientId());
        Assertions.assertEquals(doctorId, r.getDoctorId());
        Assertions.assertEquals("Douleur poitrine", r.getMotif());
        Assertions.assertEquals(RdvStatus.EN_ATTENTE, r.getStatus());
    }

    /* ================= FIND BY PATIENT ================= */

    @Test
    @Order(3)
    void testFindAllByPatient(){

        List<RendezVous> list = service.findAllByPatient(patientId);

        boolean exists = list.stream()
                .anyMatch(r -> r.getId() == rdvId);

        Assertions.assertTrue(exists);
    }

    /* ================= UPDATE (Doctor validation) ================= */

    @Test
    @Order(4)
    void testUpdateStatus(){

        RendezVous r = service.findById(rdvId);

        r.setStatus(RdvStatus.APPROUVE);
        r.setDoctorNote("RDV confirmé par le médecin");

        service.update(r);

        RendezVous updated = service.findById(rdvId);

        Assertions.assertEquals(RdvStatus.APPROUVE, updated.getStatus());
        Assertions.assertEquals("RDV confirmé par le médecin", updated.getDoctorNote());
    }

    /* ================= DELETE ================= */

    @Test
    @Order(5)
    void testDeleteRdv(){

        service.delete(rdvId);

        RendezVous deleted = service.findById(rdvId);

        Assertions.assertNull(deleted);
    }

    /* ================= CLEANUP ================= */

    @AfterAll
    static void cleanup() throws Exception {

        cnx.prepareStatement("DELETE FROM rendez_vous WHERE id="+rdvId).executeUpdate();
        cnx.prepareStatement("DELETE FROM user WHERE id="+patientId).executeUpdate();
        cnx.prepareStatement("DELETE FROM user WHERE id="+doctorId).executeUpdate();

        System.out.println("=== RDV TESTS FINISHED ===");
    }
}
