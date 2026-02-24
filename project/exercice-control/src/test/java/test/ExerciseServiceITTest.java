package test;

import com.serinity.exercicecontrol.model.Exercise;
import com.serinity.exercicecontrol.service.ExerciseService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExerciseServiceITTest {

    private static ExerciseService service;
    private static int createdId;

    @BeforeAll
    static void setup() {
        service = new ExerciseService();
    }

    // create a valid Exercise
    private Exercise buildValidExercise(String title) {
        Exercise ex = new Exercise();
        ex.setTitle(title);
        ex.setType("respiration");
        ex.setLevel(1);
        ex.setDurationMinutes(5);
        ex.setDescription("test description");
        return ex;
    }

    /**
     * TEST 1: Add (Create)
     * - verifies insert returns an id
     * - verifies it exists after insert
     */
    @Test
    @Order(1)
    void shouldAddExercise_andReturnId_andExistInDb() throws SQLException {
        Exercise ex = buildValidExercise("Test Exercise - Add");

        int id = service.addExercise(ex);
        assertTrue(id > 0, "Expected generated id > 0 after insert");
        createdId = id;

        Exercise fromDb = service.getExerciseById(createdId);
        assertNotNull(fromDb, "Exercise should exist in DB after insert");
        assertEquals("Test Exercise - Add", fromDb.getTitle());
        assertEquals("respiration", fromDb.getType());
        assertEquals(1, fromDb.getLevel());
        assertEquals(5, fromDb.getDurationMinutes());
    }

    /**
     * TEST 2: Read (Get all)
     * - verifies list is not empty
     * - verifies created exercise is present
     */
    @Test
    @Order(2)
    void shouldGetAllExercises_andContainCreatedExercise() throws SQLException {
        List<Exercise> list = service.getAllExercises();

        assertNotNull(list);
        assertFalse(list.isEmpty(), "Expected exercises list not empty");

        assertTrue(
                list.stream().anyMatch(e -> e.getId() == createdId),
                "Expected created exercise to be present in getAllExercises()"
        );
    }

    /**
     * TEST 3: Update
     * - modifies title + level + duration
     * - verifies DB reflects changes
     */
    @Test
    @Order(3)
    void shouldUpdateExercise_andPersistChanges() throws SQLException {
        Exercise updated = new Exercise();
        updated.setId(createdId);
        updated.setTitle("Test Exercise - Updated");
        updated.setType("respiration"); // keep valid
        updated.setLevel(2);
        updated.setDurationMinutes(10);
        updated.setDescription("updated description");

        service.updateExercise(updated);

        Exercise fromDb = service.getExerciseById(createdId);
        assertNotNull(fromDb);
        assertEquals("Test Exercise - Updated", fromDb.getTitle());
        assertEquals(2, fromDb.getLevel());
        assertEquals(10, fromDb.getDurationMinutes());
        assertEquals("updated description", fromDb.getDescription());
    }

    /**
     * TEST 4: Delete
     * - deletes created exercise
     * - verifies it no longer exists
     */
    @Test
    @Order(4)
    void shouldDeleteExercise_andRemoveFromDb() throws SQLException {
        service.deleteExercise(createdId);

        Exercise fromDb = service.getExerciseById(createdId);
        assertNull(fromDb, "Expected exercise to be null after delete");
    }

    /**
     * Safety cleanup if a test fails mid-run.
     */
    @AfterAll
    static void cleanup() {
        try {
            if (createdId > 0) {
                Exercise check = service.getExerciseById(createdId);
                if (check != null) {
                    service.deleteExercise(createdId);
                }
            }
        } catch (Exception ignored) {
        }
    }
}
