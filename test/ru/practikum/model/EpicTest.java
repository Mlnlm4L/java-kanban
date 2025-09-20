package ru.practikum.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practikum.manager.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private InMemoryTaskManager taskManager;
    private Epic epic;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
        epic = new Epic("Эпик", "Описание эпика");
        taskManager.createEpic(epic);
    }

    @Test
    void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic("Эпик 1", "Описание 1");
        epic1.setId(1);
        Epic epic2 = new Epic("Эпик 2", "Описание 2");
        epic2.setId(1);
        assertEquals(epic1, epic2, "Эпики с одинаковым id должны быть равны");
    }

    @Test
    void testEpicStatusAllNew() {
        Subtask subtask1 = new Subtask("Эпик 1", "Описание 1", Status.NEW, epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2025, 1, 1, 10, 0));
        Subtask subtask2 = new Subtask("Эпик 2", "Описание 2", Status.NEW, epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2025, 1, 1, 12, 0));
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        assertEquals(Status.NEW, epic.getStatus());
    }

    @Test
    void testEpicStatusAllDone() {
        Subtask subtask1 = new Subtask("Эпик 1", "Описание 1", Status.DONE, epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2025, 1, 1, 10, 0));
        Subtask subtask2 = new Subtask("Эпик 2", "Описание 2", Status.DONE, epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2025, 1, 1, 12, 0));
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        assertEquals(Status.DONE, epic.getStatus());
    }

    @Test
    void testEpicStatusNewAndDone() {
        Subtask subtask1 = new Subtask("Эпик 1", "Описание 1", Status.NEW, epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2025, 1, 1, 10, 0));
        Subtask subtask2 = new Subtask("Эпик 2", "Описание 2", Status.DONE, epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2025, 1, 1, 12, 0));
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void testEpicStatusInProgress() {
        Subtask subtask1 = new Subtask("Эпик 1", "Описание 1", Status.IN_PROGRESS, epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2025, 1, 1, 10, 0));
        Subtask subtask2 = new Subtask("Эпик 2", "Описание 2", Status.IN_PROGRESS, epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2025, 1, 1, 12, 0));
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void testEpicStatusChange() {
        Subtask subtask = new Subtask("Эпик", "Описание", Status.NEW, epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2025, 1, 1, 10, 0));
        Subtask subtask1 = taskManager.createSubtask(subtask);
        assertEquals(Status.NEW, epic.getStatus());
        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);
        assertEquals(Status.DONE, epic.getStatus());
    }
}