package services;

import esprit.tn.oussema_javafx.models.RapportMedical;
import esprit.tn.oussema_javafx.services.RapportMedicalService;
import esprit.tn.oussema_javafx.utils.Mydatabase;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RapportMedicalServiceTest {

    private static RapportMedicalService service;
    private static Connection cnx;

    private static int patientId;
    private static int rapportId;

    /* ================= SETUP ================= */

    @BeforeAll
    static void init() throws Exception {

        service = new RapportMedicalService();
        cnx = Mydatabase.getInstance().getConnection();

        System.out.println("=== STARTING RAPPORT MEDICAL TESTS ===");

        /* ---------- CREATE PATIENT ---------- */
        PreparedStatement ps = cnx.prepareStatement(
                "INSERT INTO user(full_name,email,phone,role,address,created_at) VALUES(?,?,?,?,?,NOW())",
                Statement.RETURN_GENERATED_KEYS);

        ps.setString(1,"Rapport Test Patient");
        ps.setString(2,"rapport@test.com");
        ps.setString(3,"99999999");
        ps.setString(4,"PATIENT");
        ps.setString(5,"Test Address");

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        patientId = rs.getInt(1);
    }

    /* ================= INSERT ================= */

    @Test
    @Order(1)
    void testInsertRapport(){

        RapportMedical r = new RapportMedical();
        r.setPatientId(patientId);
        r.setDateCreation(LocalDate.now());
        r.setResumeGeneral("Rapport médical JUnit");

        service.insert(r);

        List<RapportMedical> list = service.findAll();

        RapportMedical inserted = list.stream()
                .filter(rm -> "Rapport médical JUnit".equals(rm.getResumeGeneral()))
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(inserted);
        rapportId = inserted.getId();
    }

    /* ================= FIND BY ID ================= */

    @Test
    @Order(2)
    void testFindById(){

        RapportMedical r = service.findById(rapportId);

        Assertions.assertNotNull(r);
        Assertions.assertEquals("Rapport médical JUnit", r.getResumeGeneral());
        Assertions.assertEquals(patientId, r.getPatientId());
    }

    /* ================= FIND ALL ================= */

    @Test
    @Order(3)
    void testFindAll(){

        List<RapportMedical> list = service.findAll();

        boolean exists = list.stream()
                .anyMatch(r -> r.getId() == rapportId);

        Assertions.assertTrue(exists);
    }

    /* ================= UPDATE ================= */

    @Test
    @Order(4)
    void testUpdateRapport(){

        RapportMedical r = service.findById(rapportId);

        r.setResumeGeneral("UPDATED RAPPORT");
        service.update(r);

        RapportMedical updated = service.findById(rapportId);

        Assertions.assertEquals("UPDATED RAPPORT", updated.getResumeGeneral());
    }

    /* ================= DELETE ================= */

    @Test
    @Order(5)
    void testDeleteRapport(){

        service.delete(rapportId);

        RapportMedical deleted = service.findById(rapportId);

        Assertions.assertNull(deleted);
    }

    /* ================= CLEANUP ================= */

    @AfterAll
    static void cleanup() throws Exception {

        cnx.prepareStatement("DELETE FROM rapports WHERE id="+rapportId).executeUpdate();
        cnx.prepareStatement("DELETE FROM user WHERE id="+patientId).executeUpdate();

        System.out.println("=== RAPPORT MEDICAL TESTS FINISHED ===");
    }
}
