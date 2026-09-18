package com.mricotta.task.service;

import com.mricotta.task.dto.TaskRequest;
import com.mricotta.task.dto.TaskResponse;
import com.mricotta.task.exception.TaskNotFoundException;
import com.mricotta.task.mapper.TaskMapper;
import com.mricotta.task.repository.TaskRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Override
    @CacheEvict(cacheNames = "taskLists", allEntries = true)
    public TaskResponse createTask(TaskRequest request) {
        var saved = taskRepository.save(taskMapper.toEntity(request));
        return taskMapper.toDto(saved);
    }

    @Override
    @Cacheable(cacheNames = "tasks", key = "#id")
    public TaskResponse getTask(String id) {
        return taskRepository.findById(id)
                .map(taskMapper::toDto)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Override
    @Cacheable(cacheNames = "taskLists", key = "'all'")
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @Override
    @Caching(
            put = @CachePut(cacheNames = "tasks", key = "#id"),
            evict = @CacheEvict(cacheNames = "taskLists", allEntries = true))
    public TaskResponse updateTask(String id, TaskRequest request) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        taskMapper.updateEntity(request, task);
        return taskMapper.toDto(taskRepository.save(task));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "tasks", key = "#id"),
            @CacheEvict(cacheNames = "taskLists", allEntries = true)})
    public void deleteTask(String id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    @Override
    @Cacheable(cacheNames = "taskLists", key = "'status:' + #status")
    public List<TaskResponse> getTaskByStatus(Boolean status) {
        return taskRepository.findAllByCompleted(status).stream()
                .map(taskMapper::toDto)
                .toList();
    }
}
