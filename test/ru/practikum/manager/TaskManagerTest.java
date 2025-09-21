package ru.practikum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practikum.exception.TaskTimeConflictException;
import ru.practikum.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected Epic epic;
    protected Subtask subtask1;
    protected Subtask subtask2;
    protected Subtask subtask3;
    protected Task task;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
        taskManager.deleteAllTasks();
        taskManager.deleteAllSubtasks();
        taskManager.deleteAllEpics();
        epic = new Epic("Тестовый Эпик", "Описание эпика");
        taskManager.createEpic(epic);
        task = new Task("Задача", "Описание задачи", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2025, 2, 2, 10, 0));

        subtask1 = new Subtask("Подзадача 1", "Описание 1", Status.NEW, epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2025, 1, 1, 10, 0));

        subtask2 = new Subtask("Подзадача 2", "Описание 2", Status.NEW, epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2025, 1, 1, 12, 0));

        subtask3 = new Subtask("Подзадача 3", "Описание 3", Status.NEW, epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2025, 1, 1, 14, 0));
    }

    @Test
    void testCreateAndGetTask() {
        Task createdTask = taskManager.createTask(task);
        Task retrievedTask = taskManager.getTaskById(createdTask.getId());
        assertNotNull(retrievedTask);
        assertEquals(createdTask.getTitle(), retrievedTask.getTitle());
        assertEquals(createdTask.getDescription(), retrievedTask.getDescription());
        assertEquals(createdTask.getStatus(), retrievedTask.getStatus());
    }

    @Test
    void testCreateAndGetEpic() {
        Epic createdEpic = taskManager.createEpic(epic);
        Epic retrievedEpic = taskManager.getEpicById(createdEpic.getId());
        assertNotNull(retrievedEpic);
        assertEquals(createdEpic.getTitle(), retrievedEpic.getTitle());
        assertEquals(createdEpic.getDescription(), retrievedEpic.getDescription());
    }

    @Test
    void testCreateAndGetSubtask() {
        Subtask createdSubtask = taskManager.createSubtask(subtask1);
        Subtask retrievedSubtask = taskManager.getSubtaskById(createdSubtask.getId());
        assertNotNull(retrievedSubtask);
        assertEquals(createdSubtask.getTitle(), retrievedSubtask.getTitle());
        assertEquals(epic.getId(), retrievedSubtask.getEpicId());
    }

    @Test
    void testGetAllTasks() {
        taskManager.createTask(task);
        List<Task> tasks = taskManager.getAllTasks();
        assertEquals(1, tasks.size());
    }

    @Test
    void testGetAllEpics() {
        List<Epic> epics = taskManager.getAllEpics();
        assertEquals(1, epics.size());
    }

    @Test
    void testGetAllSubtasks() {
        taskManager.createSubtask(subtask1);
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        assertEquals(1, subtasks.size());
    }

    @Test
    void testUpdateTask() {
        Task createdTask = taskManager.createTask(task);
        createdTask.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(createdTask);
        Task updatedTask = taskManager.getTaskById(createdTask.getId());
        assertEquals(Status.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    void testUpdateSubtask() {
        Subtask createdSubtask = taskManager.createSubtask(subtask1);
        createdSubtask.setStatus(Status.DONE);
        taskManager.updateSubtask(createdSubtask);
        Subtask updatedSubtask = taskManager.getSubtaskById(createdSubtask.getId());
        assertEquals(Status.DONE, updatedSubtask.getStatus());
    }

    @Test
    void testUpdateEpic() {
        Epic createdEpic = taskManager.createEpic(epic);
        createdEpic.setTitle("Обновленный Эпик");
        taskManager.updateEpic(createdEpic);
        Epic updatedEpic = taskManager.getEpicById(createdEpic.getId());
        assertEquals("Обновленный Эпик", updatedEpic.getTitle());
    }

    @Test
    void testDeleteTask() {
        Task createdTask = taskManager.createTask(task);
        taskManager.deleteTaskById(createdTask.getId());
        assertNull(taskManager.getTaskById(createdTask.getId()));
    }

    @Test
    void testDeleteEpic() {
        Epic createdEpic = taskManager.createEpic(epic);
        taskManager.deleteEpicById(createdEpic.getId());
        assertNull(taskManager.getEpicById(createdEpic.getId()));
    }

    @Test
    void testDeleteSubtask() {
        Subtask createdSubtask = taskManager.createSubtask(subtask1);
        taskManager.deleteSubtaskById(createdSubtask.getId());
        assertNull(taskManager.getSubtaskById(createdSubtask.getId()));
    }

    @Test
    void testDeleteAllTasks() {
        taskManager.createTask(task);
        taskManager.deleteAllTasks();
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void testDeleteAllEpics() {
        taskManager.createEpic(epic);
        taskManager.deleteAllEpics();
        assertTrue(taskManager.getAllEpics().isEmpty());
    }

    @Test
    void testDeleteAllSubtasks() {
        taskManager.createSubtask(subtask1);
        taskManager.deleteAllSubtasks();
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void testSubtaskHasLinkedEpic() {
        Subtask createdSubtask = taskManager.createSubtask(subtask1);
        Epic epicFromManager = taskManager.getEpicById(epic.getId());
        assertTrue(epicFromManager.getSubtaskIds().contains(createdSubtask.getId()));
    }

    @Test
    void testEpicStatusMixedSubtasks() {
        Subtask createdSubtask1 = taskManager.createSubtask(subtask1);
        Subtask createdSubtask2 = taskManager.createSubtask(subtask2);
        createdSubtask1.setStatus(Status.NEW);
        createdSubtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(createdSubtask1);
        taskManager.updateSubtask(createdSubtask2);
        Epic epicFromManager = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, epicFromManager.getStatus(),
                "Эпик должен иметь статус IN_PROGRESS, когда подзадачи имеют разные статусы");
    }

    @Test
    void testSubtaskLinkedToCorrectEpic() {
        Subtask createdSubtask = taskManager.createSubtask(subtask1);
        Epic epicFromManager = taskManager.getEpicById(epic.getId());
        assertEquals(epic.getId(), createdSubtask.getEpicId(),
                "Подзадача должна ссылаться на свой эпик");
        assertTrue(epicFromManager.getSubtaskIds().contains(createdSubtask.getId()),
                "Эпик должен содержать ID своей подзадачи");
    }

    @Test
    void testTimeOverlapDetection() {
        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание", Status.NEW,
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 10, 0)));
        Task overlappingTask = new Task("Задача 2", "Описание", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 0));
        assertThrows(TaskTimeConflictException.class, () -> taskManager.createTask(overlappingTask));
    }

    @Test
    void testNoTimeOverlap() {
        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0)));
        Task nonOverlappingTask = new Task("Задача 2", "Описание", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 30));
        assertDoesNotThrow(() -> taskManager.createTask(nonOverlappingTask));
    }

    @Test
    void testTasksWithoutTimeNoOverlap() {
        Task taskWithTime = taskManager.createTask(new Task("С временем", "Описание", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0)));
        Task taskWithoutTime = new Task("Без времени", "Описание", Status.NEW,
                null, null);
        assertDoesNotThrow(() -> taskManager.createTask(taskWithoutTime));
    }
}