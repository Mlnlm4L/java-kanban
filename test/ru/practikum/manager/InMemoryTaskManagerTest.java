package ru.practikum.manager;

import org.junit.jupiter.api.Test;
import ru.practikum.model.Epic;
import ru.practikum.model.Status;
import ru.practikum.model.Subtask;
import ru.practikum.model.Task;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
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
    void managerShouldAddAndFindDifferentTaskTypes() {
        TaskManager manager = Managers.getDefault();
        Task task = manager.createTask(new Task("Задача", "Описание", Status.NEW));
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача", "Описание",
                Status.NEW, epic.getId()));
        assertNotNull(manager.getTaskById(task.getId()), "Должна находиться задача по id");
        assertNotNull(manager.getEpicById(epic.getId()), "Должен находиться эпик по id");
        assertNotNull(manager.getSubtaskById(subtask.getId()), "Должна находиться подзадача по id");
    }

    @Test
    void tasksWithAssignedAndGeneratedIdsShouldNotConflict() {
        TaskManager manager = Managers.getDefault();
        Task task1 = new Task("Задача 1", "Описание", Status.NEW);
        manager.createTask(task1);
        task1.setId(10);
        Task task2 = manager.createTask(new Task("Задача 2",
                "Описание", Status.NEW));
        assertNotEquals(task1.getId(), task2.getId());
        assertEquals(10, task1.getId(), "Id должен сохраняться");
    }
}