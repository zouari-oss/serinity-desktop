package services;

import esprit.tn.oussema_javafx.models.Consultation;
import esprit.tn.oussema_javafx.services.ConsultationService;
import esprit.tn.oussema_javafx.utils.Mydatabase;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConsultationServiceTest {

    private static ConsultationService service;
    private static Connection cnx;

    private static int patientId;
    private static int doctorId;
    private static int rapportId;
    private static int rdvId;
    private static int consultationId;

    /* ================= SETUP ================= */

    @BeforeAll
    static void init() throws Exception {

        service = new ConsultationService();
        cnx = Mydatabase.getInstance().getConnection();

        System.out.println("=== STARTING  CONSULTATION TESTS ===");

        /* ---------- CREATE PATIENT ---------- */
        PreparedStatement psPatient = cnx.prepareStatement(
                "INSERT INTO user(full_name,email,phone,role,address,created_at) VALUES(?,?,?,?,?,NOW())",
                Statement.RETURN_GENERATED_KEYS);

        psPatient.setString(1,"JUnit Patient");
        psPatient.setString(2,"patient@test.com");
        psPatient.setString(3,"00000000");
        psPatient.setString(4,"PATIENT");
        psPatient.setString(5,"Test Address");

        psPatient.executeUpdate();

        ResultSet rs = psPatient.getGeneratedKeys();
        rs.next();
        patientId = rs.getInt(1);


        /* ---------- CREATE DOCTOR ---------- */
        PreparedStatement psDoctor = cnx.prepareStatement(
                "INSERT INTO user(full_name,email,phone,role,speciality,address,created_at) VALUES(?,?,?,?,?,?,NOW())",
                Statement.RETURN_GENERATED_KEYS);

        psDoctor.setString(1,"JUnit Doctor");
        psDoctor.setString(2,"doctor@test.com");
        psDoctor.setString(3,"11111111");
        psDoctor.setString(4,"DOCTOR");
        psDoctor.setString(5,"Cardiologie");
        psDoctor.setString(6,"Clinic Address");

        psDoctor.executeUpdate();

        rs = psDoctor.getGeneratedKeys();
        rs.next();
        doctorId = rs.getInt(1);


        /* ---------- CREATE RAPPORT ---------- */
        PreparedStatement psRapport = cnx.prepareStatement(
                "INSERT INTO rapports(patient_id,date_creation,resume_general) VALUES(?,CURDATE(),'JUnit Rapport')",
                Statement.RETURN_GENERATED_KEYS);

        psRapport.setInt(1, patientId);
        psRapport.executeUpdate();

        rs = psRapport.getGeneratedKeys();
        rs.next();
        rapportId = rs.getInt(1);


        /* ---------- CREATE RDV ---------- */
        PreparedStatement psRdv = cnx.prepareStatement(
                "INSERT INTO rendez_vous(patient_id,doctor_id,motif,date_time,status) VALUES(?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);

        psRdv.setInt(1, patientId);
        psRdv.setInt(2, doctorId);
        psRdv.setString(3,"JUnit RDV");
        psRdv.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
        psRdv.setString(5,"APPROUVE");

        psRdv.executeUpdate();

        rs = psRdv.getGeneratedKeys();
        rs.next();
        rdvId = rs.getInt(1);
    }

    /* ================= INSERT ================= */

    @Test
    @Order(1)
    void testInsertConsultation(){

        Consultation c = new Consultation();
        c.setRapportId(rapportId);
        c.setRendezVousId(rdvId);
        c.setDoctorId(doctorId);
        c.setDateConsultation(LocalDateTime.now());
        c.setDiagnostic("Test Diagnostic");
        c.setPrescription("Test Prescription");
        c.setNotes("JUnit Notes");

        service.insert(c);

        List<Consultation> list = service.findAll();

        Consultation inserted = list.stream()
                .filter(cs -> "Test Diagnostic".equals(cs.getDiagnostic()))
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(inserted);
        consultationId = inserted.getId();
    }

    /* ================= FIND BY ID ================= */

    @Test
    @Order(2)
    void testFindById(){

        Consultation c = service.findById(consultationId);

        Assertions.assertNotNull(c);
        Assertions.assertEquals("Test Diagnostic", c.getDiagnostic());
    }

    /* ================= EXISTS BY RDV ================= */

    @Test
    @Order(3)
    void testExistsByRdv(){

        boolean exists = service.existsByRdv(rdvId);
        Assertions.assertTrue(exists);
    }

    /* ================= UPDATE ================= */

    @Test
    @Order(4)
    void testUpdateConsultation(){

        Consultation c = service.findById(consultationId);

        c.setDiagnostic("UPDATED DIAGNOSTIC");
        service.update(c);

        Consultation updated = service.findById(consultationId);

        Assertions.assertEquals("UPDATED DIAGNOSTIC", updated.getDiagnostic());
    }

    /* ================= DELETE ================= */

    @Test
    @Order(5)
    void testDeleteConsultation(){

        service.delete(consultationId);

        Consultation deleted = service.findById(consultationId);

        Assertions.assertNull(deleted);
    }

    /* ================= CLEANUP ================= */

    @AfterAll
    static void cleanup() throws Exception {

        // supprimer données créées par test
        cnx.prepareStatement("DELETE FROM consultations WHERE rendez_vous_id="+rdvId).executeUpdate();
        cnx.prepareStatement("DELETE FROM rendez_vous WHERE id="+rdvId).executeUpdate();
        cnx.prepareStatement("DELETE FROM rapports WHERE id="+rapportId).executeUpdate();
        cnx.prepareStatement("DELETE FROM user WHERE id="+doctorId).executeUpdate();
        cnx.prepareStatement("DELETE FROM user WHERE id="+patientId).executeUpdate();

        System.out.println("===  TESTS FINISHED SUCCESSFULLY ===");
    }
}
