package com.mricotta.task.service;

import com.mricotta.task.dto.TaskRequest;
import com.mricotta.task.dto.TaskResponse;
import java.util.List;

public interface TaskService {

    TaskResponse createTask(TaskRequest request);

    TaskResponse getTask(String id);

    List<TaskResponse> getAllTasks();

    TaskResponse updateTask(String id, TaskRequest request);

    void deleteTask(String id);

    List<TaskResponse> getTaskByStatus(Boolean status);
}
