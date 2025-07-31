package ru.practikum.manager;

import org.junit.jupiter.api.Test;
import ru.practikum.model.Epic;
import ru.practikum.model.Status;
import ru.practikum.model.Subtask;
import ru.practikum.model.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class ManagerTest {

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Задача 2", "Описание 2", Status.IN_PROGRESS);
        task2.setId(1);
        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
    }

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

    @Test
    void subtaskCannotReferenceItselfAsEpic() {
        TaskManager manager = Managers.getDefault();
        boolean found = false;
        Epic epic = new Epic("Эпик", "Описание");
        Epic savedEpic = manager.createEpic(epic);
        int epicId = savedEpic.getId();
        Subtask validSubtask = new Subtask("Подзадача", "Описание", Status.NEW, epicId);
        manager.createSubtask(validSubtask);
        Subtask invalidSubtask = new Subtask("Подзадача", "Описание", Status.NEW, 999);
        invalidSubtask.setId(999);
        assertNull(manager.createSubtask(invalidSubtask),
                "Подзадача, ссылающаяся на несуществующий эпик, не должна создаваться");
        for (Subtask subtask : manager.getAllSubtasks()) {
            if (subtask.getId() == 999) {
                found = true;
                break;
            }
        }
        assertFalse(found, "В менеджере не должно быть подзадачи с id=999");
    }

    @Test
    void managersShouldBeInitializedAndReadyToWork() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(historyManager, "HistoryManager должен быть проинициализирован");
        assertNotNull(taskManager, "TaskManager должен быть проинициализирован");
        assertTrue(historyManager instanceof InMemoryHistoryManager,
                "Должен возвращаться InMemoryHistoryManager");
        assertTrue(taskManager instanceof InMemoryTaskManager, "Должен возвращаться InMemoryTaskManager");
    }

    @Test
    void managerShouldAddAndFindDifferentTaskTypes() {
        TaskManager manager = new InMemoryTaskManager();
        Task task = manager.createTask(new Task("Задача", "Описание", Status.NEW));
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача", "Описание",
                Status.NEW, epic.getId()));
        assertNotNull(manager.getTaskById(task.getId()), "Должна находиться задача по id");
        assertNotNull(manager.getEpicById(epic.getId()), "Должен находиться эпик по id");
        assertNotNull(manager.getSubtaskById(subtask.getId()), "Должна находиться подзадача по id");
    }

    @Test
    void taskShouldNotChangeWhenAddedToManager() {
        TaskManager manager = new InMemoryTaskManager();
        Task task = new Task("Задача", "Описание", Status.NEW);
        task.setId(1);
        Task newTask = manager.createTask(task);
        assertEquals(task.getTitle(), newTask.getTitle(), "Название задачи не должно изменяться");
        assertEquals(task.getDescription(), newTask.getDescription(),
                "Описание задачи не должно изменяться");
        assertEquals(task.getStatus(), newTask.getStatus(), "Статус задачи не должен изменяться");
        assertEquals(task.getId(), newTask.getId(), "Id задачи не должен изменяться");
    }

    @Test
    void tasksWithAssignedAndGeneratedIdsShouldNotConflict() {
        TaskManager manager = new InMemoryTaskManager();
        Task task1 = new Task("Задача 1", "Описание", Status.NEW);
        manager.createTask(task1);
        task1.setId(10);
        Task task2 = manager.createTask(new Task("Задача 2",
                "Описание", Status.NEW));
        assertNotEquals(task1.getId(), task2.getId());
        assertEquals(10, task1.getId(), "Id должен сохраняться");
    }

    @Test
    void historyManagerShouldPreserveTaskState() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task("Задача", "Описание", Status.NEW);
        task.setId(1);
        historyManager.add(task);
        Task historyTask = historyManager.getHistory().getFirst();
        assertEquals(Status.NEW, historyTask.getStatus(), "История должна сохранять исходный статус");
        assertEquals("Задача", historyTask.getTitle(), "История должна сохранять исходное название");
        assertEquals("Описание", historyTask.getDescription(),
                "История должна сохранять исходное описание");
    }

    @Test
    void add() {
        Task task = new Task("Задача", "Описание", Status.NEW);
        HistoryManager historyManager = Managers.getDefaultHistory();
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");
    }
}