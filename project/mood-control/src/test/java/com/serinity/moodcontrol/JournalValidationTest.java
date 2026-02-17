package com.serinity.moodcontrol;

import com.serinity.moodcontrol.service.JournalValidation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JournalValidationTest {

    @Test
    @Order(1)
    void newEntry_allValid_returnsNull() {
        String err = JournalValidation.validateNewOrEdit(
                "My Title",
                "Answer one",
                "Answer two",
                "Answer three"
        );
        assertNull(err);
    }

    @Test
    @Order(2)
    void titleEmpty_returnsTitleEmptyKey() {
        String err = JournalValidation.validateNewOrEdit(
                "   ",
                "A1",
                "A2",
                "A3"
        );
        assertEquals("journal.field.title|journal.validation.empty", err);
    }

    @Test
    @Order(3)
    void answer1Empty_returnsA1EmptyKey() {
        String err = JournalValidation.validateNewOrEdit(
                "Title",
                "",
                "A2",
                "A3"
        );
        assertEquals("journal.field.a1|journal.validation.empty", err);
    }

    @Test
    @Order(4)
    void forbiddenCharacterInAnswer2_returnsA2ForbiddenKey() {
        String err = JournalValidation.validateNewOrEdit(
                "Title",
                "A1",
                "bad * char",
                "A3"
        );
        assertEquals("journal.field.a2|journal.validation.forbidden", err);
    }

    @Test
    @Order(5)
    void maxLengthExceeded_returnsMaxLenKey() {
        // Title max is 80 in your validator
        String longTitle = "a".repeat(81);

        String err = JournalValidation.validateNewOrEdit(
                longTitle,
                "A1",
                "A2",
                "A3"
        );
        assertEquals("journal.field.title|journal.validation.max_len", err);
    }
}
