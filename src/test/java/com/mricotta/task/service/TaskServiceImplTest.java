package com.mricotta.task.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

import com.mricotta.task.dto.TaskRequest;
import com.mricotta.task.dto.TaskResponse;
import com.mricotta.task.entity.Task;
import com.mricotta.task.exception.TaskNotFoundException;
import com.mricotta.task.mapper.TaskMapper;
import com.mricotta.task.repository.TaskRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    private static final String TASK_ID = "66e8a1f2c3b4d5e6f7a8b9c0";

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private final TaskRequest request = new TaskRequest("Write docs", "README for the service", false);

    @Test
    void createTask_savesMappedEntityAndReturnsDto() {
        var unsaved = task(null);
        var saved = task(TASK_ID);
        var expected = response();
        given(taskMapper.toEntity(request)).willReturn(unsaved);
        given(taskRepository.save(unsaved)).willReturn(saved);
        given(taskMapper.toDto(saved)).willReturn(expected);

        var result = taskService.createTask(request);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getTask_whenFound_returnsDto() {
        var saved = task(TASK_ID);
        var expected = response();
        given(taskRepository.findById(TASK_ID)).willReturn(Optional.of(saved));
        given(taskMapper.toDto(saved)).willReturn(expected);

        assertThat(taskService.getTask(TASK_ID)).isEqualTo(expected);
    }

    @Test
    void getTask_whenMissing_throwsNotFound() {
        given(taskRepository.findById(TASK_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTask(TASK_ID))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(TASK_ID);
    }

    @Test
    void getAllTasks_mapsEveryEntity() {
        var saved = task(TASK_ID);
        var expected = response();
        given(taskRepository.findAll()).willReturn(List.of(saved));
        given(taskMapper.toDto(saved)).willReturn(expected);

        assertThat(taskService.getAllTasks()).containsExactly(expected);
    }

    @Test
    void updateTask_whenFound_appliesChangesAndSaves() {
        var existing = task(TASK_ID);
        var expected = response();
        given(taskRepository.findById(TASK_ID)).willReturn(Optional.of(existing));
        given(taskRepository.save(existing)).willReturn(existing);
        given(taskMapper.toDto(existing)).willReturn(expected);

        var result = taskService.updateTask(TASK_ID, request);

        assertThat(result).isEqualTo(expected);
        then(taskMapper).should().updateEntity(request, existing);
    }

    @Test
    void updateTask_whenMissing_throwsNotFoundAndDoesNotSave() {
        given(taskRepository.findById(TASK_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(TASK_ID, request))
                .isInstanceOf(TaskNotFoundException.class);

        then(taskRepository).should(never()).save(any());
        verifyNoInteractions(taskMapper);
    }

    @Test
    void deleteTask_whenFound_deletesById() {
        given(taskRepository.existsById(TASK_ID)).willReturn(true);

        taskService.deleteTask(TASK_ID);

        then(taskRepository).should().deleteById(TASK_ID);
    }

    @Test
    void deleteTask_whenMissing_throwsNotFoundAndDoesNotDelete() {
        given(taskRepository.existsById(TASK_ID)).willReturn(false);

        assertThatThrownBy(() -> taskService.deleteTask(TASK_ID))
                .isInstanceOf(TaskNotFoundException.class);

        then(taskRepository).should(never()).deleteById(TASK_ID);
    }

    private static Task task(String id) {
        return Task.builder()
                .id(id)
                .title("Write docs")
                .description("README for the service")
                .completed(false)
                .build();
    }

    private static TaskResponse response() {
        return new TaskResponse(TASK_ID, "Write docs", "README for the service", false);
    }
}
