package ru.practikum.manager;

import ru.practikum.model.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;


public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    void save() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write("id,type,name,status,description,epic\n");

            Map<Integer, Task> sortedTasks = new TreeMap<>();
            sortedTasks.putAll(tasks);
            sortedTasks.putAll(epics);
            sortedTasks.putAll(subtasks);

            for (Task task : sortedTasks.values()) {
                bufferedWriter.write(toString(task));
                bufferedWriter.newLine();
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);

        if (!file.exists()) {
            return fileBackedTaskManager;
        }

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.size() <= 1) return fileBackedTaskManager;

            int maxId = 0;

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    continue;
                }
                Task task = fromString(line);
                if (task.getId() > maxId) {
                    maxId = task.getId();
                }

                switch (task.getType()) {
                    case TASK:
                        fileBackedTaskManager.tasks.put(task.getId(), task);
                        break;
                    case EPIC:
                        Epic epic = (Epic) task;
                        fileBackedTaskManager.epics.put(epic.getId(), epic);
                        break;
                    case SUBTASK:
                        Subtask subtask = (Subtask) task;
                        fileBackedTaskManager.subtasks.put(subtask.getId(), subtask);
                        Epic subEpic = fileBackedTaskManager.epics.get(subtask.getEpicId());
                        if (subEpic != null) {
                            subEpic.addSubtaskId(subtask.getId());
                        }
                        break;
                }
            }

            for (Epic epic : fileBackedTaskManager.epics.values()) {
                fileBackedTaskManager.updateEpicStatus(epic.getId());
            }

            fileBackedTaskManager.setNextId(maxId + 1);

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения данных из файла: " + file.getName(), e);
        }
        return fileBackedTaskManager;
    }

    protected String toString(Task task) {
        if (task instanceof Subtask subtask) {
            return String.format("%d,%s,%s,%s,%s,%d",
                    subtask.getId(),
                    subtask.getType(),
                    subtask.getTitle(),
                    subtask.getStatus(),
                    subtask.getDescription(),
                    subtask.getEpicId());
        } else {
            return String.format("%d,%s,%s,%s,%s,",
                    task.getId(),
                    task.getType(),
                    task.getTitle(),
                    task.getStatus(),
                    task.getDescription());
        }
    }

    protected static Task fromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String title = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];

        switch (type) {
            case TASK:
                Task task = new Task(title, description, status);
                task.setId(id);
                return task;

            case EPIC:
                Epic epic = new Epic(title, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;

            case SUBTASK:
                int epicId = Integer.parseInt(fields[5]);
                Subtask subtask = new Subtask(title, description, status, epicId);
                subtask.setId(id);
                return subtask;

            default:
                throw new ManagerSaveException("Неизвестный тип задачи: " + type);
        }
    }


    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    public static void main(String[] args) {

        File file = new File("resources/tasks.csv");
        TaskManager manager = new FileBackedTaskManager(file);
        Task task = manager.createTask(new Task("Задача 1", "Описание задачи 1", Status.NEW));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание задачи 1", Status.NEW));
        Epic epic = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Epic epic2 = manager.createEpic(new Epic("Эпик 2", "Описание эпика 1"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи",
                Status.NEW, epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи",
                Status.NEW, epic2.getId()));
        FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(file);

        boolean tasksMatch = manager.getAllTasks().equals(manager2.getAllTasks());
        boolean epicsMatch = manager.getAllEpics().equals(manager2.getAllEpics());
        boolean subtasksMatch = manager.getAllSubtasks().equals(manager2.getAllSubtasks());

        System.out.println("\n");
        System.out.println("Задачи совпадают: " + tasksMatch);
        System.out.println("Эпики совпадают: " + epicsMatch);
        System.out.println("Подзадачи совпадают: " + subtasksMatch);
    }
}