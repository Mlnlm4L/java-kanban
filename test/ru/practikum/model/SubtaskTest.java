package ru.practikum.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Epic epic = new Epic("Эпик", "Описание");
        epic.setId(1);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", Status.NEW, epic.getId(),
                Duration.ofHours(2), LocalDateTime.of(2025, 4, 5, 6, 7));
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", Status.DONE, epic.getId(),
                Duration.ofHours(2), LocalDateTime.of(2025, 4, 5, 6, 7));
        subtask2.setId(2);
        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны быть равны");
    }
}