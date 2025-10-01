package ru.practikum.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, Status status, int epicId,
                   Duration duration, LocalDateTime startTime) {
        super(title, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask() {
        super("", "", Status.NEW);
        this.epicId = 0;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", epicId=" + epicId +
                '}';
    }
}