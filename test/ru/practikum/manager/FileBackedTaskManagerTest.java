package ru.practikum.manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practikum.model.Epic;
import ru.practikum.model.Status;
import ru.practikum.model.Subtask;
import ru.practikum.model.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class FileBackedTaskManagerTest {
    File file;

    @BeforeEach
    void create() throws IOException {
        file = File.createTempFile("test_tasks", ".csv");
    }

    @AfterEach
    void delete() {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    @Test
    void testSavingAndLoadingEmptyFile() {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.save();

        assertTrue(file.exists(), "Файл должен существовать");
        assertTrue(file.length() > 0, "Файл должен содержать данные");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(manager.getAllTasks().isEmpty(), "Должен быть пустым");
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubtasks().isEmpty());

        assertTrue(loadedManager.getAllTasks().isEmpty(), "Должен быть пустым");
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void testSaveMultipleTasks() throws IOException {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = manager.createTask(new Task("Задача 1", "Описание задачи", Status.NEW));
        Epic epic = manager.createEpic(new Epic("Эпик 1", "Описание эпика"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи",
                Status.NEW, epic.getId()));

        assertTrue(file.exists(), "Файл должен существовать");
        assertTrue(file.length() > 0, "Файл должен содержать данные");

        String string = Files.readString(file.toPath());
        assertTrue(string.contains("id,type,name,status,description,epic"), "Должен содержать заголовок");
        assertTrue(string.contains("TASK"), "Должен содержать задачу");
        assertTrue(string.contains("EPIC"), "Должен содержать эпик");
        assertTrue(string.contains("SUBTASK"), "Должен содержать подзадачу");
        assertTrue(string.contains("Задача 1"), "Должен содержать название задачи");
        assertTrue(string.contains("Эпик 1"), "Должен содержать название эпика");
    }

    @Test
    void testUploadingMultipleTasks() {

        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.createTask(new Task("Задача 1", "Описание 1", Status.NEW));
        manager.createTask(new Task("Задача 2", "Описание 2", Status.DONE));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(2, loadedManager.getAllTasks().size());
    }
}