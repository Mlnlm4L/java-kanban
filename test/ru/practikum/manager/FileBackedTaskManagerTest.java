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

import static org.junit.jupiter.api.Assertions.*;


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
        Task task = manager.createTask(new Task("Задача 1", "Описание 1", Status.NEW));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание 2", Status.DONE));
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание эпика"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача", "Описание подзадачи",
                Status.NEW, epic.getId()));
        Epic epic2 = manager.createEpic(new Epic("Эпик2", "Описание эпика2"));
        Subtask subtask2 = manager.createSubtask(new Subtask("Подзадача2", "Описание подзадачи2",
                Status.IN_PROGRESS, epic2.getId()));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(2, loadedManager.getAllTasks().size());
        assertEquals(2, loadedManager.getAllEpics().size());
        assertEquals(2, loadedManager.getAllSubtasks().size());

        Task loadedTask = loadedManager.getTaskById(task.getId());
        assertNotNull(loadedTask);
        assertEquals(task.getTitle(), loadedTask.getTitle());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(Status.NEW, loadedTask.getStatus());

        Task loadedTask2 = loadedManager.getTaskById(task2.getId());
        assertNotNull(loadedTask2);
        assertEquals(task2.getTitle(), loadedTask2.getTitle());
        assertEquals(task2.getDescription(), loadedTask2.getDescription());
        assertEquals(Status.DONE, loadedTask2.getStatus());

        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        assertNotNull(loadedEpic);
        assertEquals(epic.getTitle(), loadedEpic.getTitle());
        assertEquals(epic.getDescription(), loadedEpic.getDescription());
        assertTrue(loadedEpic.getSubtaskIds().contains(subtask.getId()));

        Epic loadedEpic2 = loadedManager.getEpicById(epic2.getId());
        assertNotNull(loadedEpic2);
        assertEquals(epic2.getTitle(), loadedEpic2.getTitle());
        assertEquals(epic2.getDescription(), loadedEpic2.getDescription());
        assertTrue(loadedEpic2.getSubtaskIds().contains(subtask2.getId()));

        Subtask loadedSubtask = loadedManager.getSubtaskById(subtask.getId());
        assertNotNull(loadedSubtask);
        assertEquals(subtask.getTitle(), loadedSubtask.getTitle());
        assertEquals(subtask.getDescription(), loadedSubtask.getDescription());
        assertEquals(Status.NEW, loadedSubtask.getStatus());
        assertEquals(epic.getId(), loadedSubtask.getEpicId());

        Subtask loadedSubtask2 = loadedManager.getSubtaskById(subtask2.getId());
        assertNotNull(loadedSubtask);
        assertEquals(subtask2.getTitle(), loadedSubtask2.getTitle());
        assertEquals(subtask2.getDescription(), loadedSubtask2.getDescription());
        assertEquals(Status.IN_PROGRESS, loadedSubtask2.getStatus());
        assertEquals(epic2.getId(), loadedSubtask2.getEpicId());
    }
}