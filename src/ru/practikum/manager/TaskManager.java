package ru.practikum.manager;

import ru.practikum.model.Epic;
import ru.practikum.model.Subtask;
import ru.practikum.model.Task;

import java.util.List;

public interface TaskManager {
    List<Task> getAllTasks();
    void deleteAllTasks();
    Task getTaskById(int id);
    Task createTask(Task task);
    void updateTask(Task task);
    void deleteTaskById(int id);
    List<Epic> getAllEpics();
    void deleteAllEpics();
    Epic getEpicById(int id);
    Epic createEpic(Epic epic);
    void updateEpic(Epic epic);
    void deleteEpicById(int id);
    List<Subtask> getAllSubtasks();
    void deleteAllSubtasks();
    Subtask getSubtaskById(int id);
    Subtask createSubtask(Subtask subtask);
    void updateSubtask(Subtask subtask);
    void deleteSubtaskById(int id);
    List<Subtask> getSubtasksByEpicId(int epicId);
    List<Task> getHistory();
}