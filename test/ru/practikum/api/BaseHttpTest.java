package ru.practikum.api;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.practikum.manager.InMemoryTaskManager;
import ru.practikum.manager.TaskManager;

import java.io.IOException;


public class BaseHttpTest {
    protected TaskManager manager;
    protected HttpTaskServer taskServer;
    protected Gson gson;

    @BeforeEach
    public void launch() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = taskServer.getGson();
        taskServer.start();
    }

    @AfterEach
    public void end() {
        if (taskServer != null) {
            taskServer.stop();
        }
    }

    protected String getBaseUrl() {
        return "http://localhost:8080";
    }
}