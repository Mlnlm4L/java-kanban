package ru.practikum.manager;

import ru.practikum.exception.ManagerLoadException;
import ru.practikum.exception.ManagerSaveException;
import ru.practikum.model.*;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final String HEADER = "id,type,name,status,description,duration,startTime,endTime,epic\n";

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    protected void save() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write(HEADER);

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
            throw new ManagerLoadException("Файл: " + file.getName() + " не существует");
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
                Task task = fileBackedTaskManager.fromString(line);
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

            fileBackedTaskManager.nextId = maxId + 1;

        } catch (IOException e) {
            throw new ManagerLoadException("Ошибка чтения данных из файла: " + file.getName(), e);
        }
        return fileBackedTaskManager;
    }

    protected String toString(Task task) {
        String durationStr = task.getDuration() != null ?
                String.valueOf(task.getDuration().toMinutes()) : "";

        String startTimeStr = task.getStartTime() != null ?
                task.getStartTime().toString() : "";

        String endTimeStr = task.getEndTime() != null ?
                task.getEndTime().toString() : "";

        if (task instanceof Subtask subtask) {
            return String.format("%d,%s,%s,%s,%s,%s,%s,%s,%d",
                    subtask.getId(),
                    subtask.getType(),
                    subtask.getTitle(),
                    subtask.getStatus(),
                    subtask.getDescription(),
                    durationStr,
                    startTimeStr,
                    endTimeStr,
                    subtask.getEpicId()
            );
        } else {
            return String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s",
                    task.getId(),
                    task.getType(),
                    task.getTitle(),
                    task.getStatus(),
                    task.getDescription(),
                    durationStr,
                    startTimeStr,
                    endTimeStr,
                    "");
        }
    }

    protected Task fromString(String value) {
        String[] fields = value.split(",", -1);

        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String title = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];

        Duration duration = null;
        if (!fields[5].isEmpty()) {
            duration = Duration.ofMinutes(Long.parseLong(fields[5]));
        }

        LocalDateTime startTime = null;
        if (!fields[6].isEmpty()) {
            startTime = LocalDateTime.parse(fields[6]);
        }

        LocalDateTime endTime = null;
        if (!fields[7].isEmpty()) {
            endTime = LocalDateTime.parse(fields[7]);
        }

        switch (type) {
            case TASK:
                Task task = new Task(title, description, status, duration, startTime);
                task.setId(id);
                sortedTasks.add(task);
                return task;

            case EPIC:
                Epic epic = new Epic(title, description);
                epic.setId(id);
                epic.setStatus(status);
                epic.setDuration(duration);
                epic.setStartTime(startTime);
                epic.setEndTime(endTime);
                sortedTasks.add(epic);
                return epic;

            case SUBTASK:
                int epicId = Integer.parseInt(fields[8]);
                Subtask subtask = new Subtask(title, description, status, epicId, duration, startTime);
                subtask.setId(id);
                sortedTasks.add(subtask);
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
}