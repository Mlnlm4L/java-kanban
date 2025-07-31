package ru.practikum.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Epic epic = new Epic("Эпик", "Описание");
        epic.setId(1);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", Status.NEW, epic.getId());
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", Status.DONE, epic.getId());
        subtask2.setId(2);
        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны быть равны");
    }
}