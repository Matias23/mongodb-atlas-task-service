package com.mricotta.task.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.mricotta.task.config.CacheConfig;
import com.mricotta.task.dto.TaskResponse;
import com.mricotta.task.entity.Task;
import com.mricotta.task.mapper.TaskMapper;
import com.mricotta.task.repository.TaskRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {
        TaskServiceImpl.class,
        CacheConfig.class,
        TaskCacheTest.TestCacheConfig.class})
class TaskCacheTest {

    private static final String TASK_ID = "66e8a1f2c3b4d5e6f7a8b9c0";

    @TestConfiguration
    static class TestCacheConfig {
        @Bean
        CacheManager cacheManager() {
            var manager = new CaffeineCacheManager("tasks", "taskLists");
            manager.setCaffeine(Caffeine.newBuilder().maximumSize(100));
            return manager;
        }
    }

    @Autowired
    private TaskService taskService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private TaskRepository taskRepository;

    @MockitoBean
    private TaskMapper taskMapper;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    @Test
    void getTask_secondCall_isServedFromCache() {
        var task = Task.builder().id(TASK_ID).title("Write docs").completed(false).build();
        var dto = new TaskResponse(TASK_ID, "Write docs", null, false);
        given(taskRepository.findById(TASK_ID)).willReturn(Optional.of(task));
        given(taskMapper.toDto(task)).willReturn(dto);

        var first = taskService.getTask(TASK_ID);
        var second = taskService.getTask(TASK_ID);

        assertThat(first).isEqualTo(second);
        then(taskRepository).should(times(1)).findById(TASK_ID);
    }

    @Test
    void deleteTask_evictsTheCachedTask() {
        var task = Task.builder().id(TASK_ID).title("Write docs").completed(false).build();
        given(taskRepository.findById(TASK_ID)).willReturn(Optional.of(task));
        given(taskMapper.toDto(task)).willReturn(new TaskResponse(TASK_ID, "Write docs", null, false));
        given(taskRepository.existsById(TASK_ID)).willReturn(true);

        taskService.getTask(TASK_ID);
        taskService.deleteTask(TASK_ID);
        taskService.getTask(TASK_ID);

        then(taskRepository).should(times(2)).findById(TASK_ID);
    }

    @Test
    void deleteTask_evictsTheCachedLists() {
        given(taskRepository.findAll()).willReturn(List.of());
        given(taskRepository.existsById(TASK_ID)).willReturn(true);

        taskService.getAllTasks();
        taskService.deleteTask(TASK_ID);
        taskService.getAllTasks();

        then(taskRepository).should(times(2)).findAll();
    }
}
