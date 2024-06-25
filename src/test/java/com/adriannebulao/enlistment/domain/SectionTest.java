package com.adriannebulao.enlistment.domain;

import org.junit.jupiter.api.*;

import static com.adriannebulao.enlistment.domain.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class SectionTest {

    @Test
    void newSection_same_room_diff_sked() {
        Room room = new Room("X", 10);
        new Section("A", DEFAULT_SUBJECT, MTH830to10, room);
        assertDoesNotThrow(() -> new Section("B", DEFAULT_SUBJECT, TF10to1130, room));
    }

    @Test
    void newSection_same_room_same_sked() {
        Room room = new Room("X", 10);
        new Section("A", DEFAULT_SUBJECT, MTH830to10, room);
        assertThrows(ScheduleConflictException.class, () -> new Section("B", DEFAULT_SUBJECT, MTH830to10, room));
    }

}
