package com.mricotta.task.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.mricotta.task.dto.TaskRequest;
import com.mricotta.task.entity.Task;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class TaskMapperTest {

    private static final String TASK_ID = "66e8a1f2c3b4d5e6f7a8b9c0";

    private final TaskMapper mapper = Mappers.getMapper(TaskMapper.class);

    @Test
    void toEntity_mapsRequestFieldsAndLeavesIdEmpty() {
        var request = new TaskRequest("Write docs", "README for the service", true);

        var task = mapper.toEntity(request);

        assertThat(task)
                .extracting(Task::getTitle, Task::getDescription, Task::isCompleted)
                .containsExactly("Write docs", "README for the service", true);
        assertThat(task.getId()).isNull();
    }

    @Test
    void toDto_mapsAllFields() {
        var task = new Task(TASK_ID, "Write docs", "README for the service", true);

        var dto = mapper.toDto(task);

        assertThat(dto.id()).isEqualTo(TASK_ID);
        assertThat(dto.title()).isEqualTo("Write docs");
        assertThat(dto.description()).isEqualTo("README for the service");
        assertThat(dto.completed()).isTrue();
    }

    @Test
    void updateEntity_overwritesFieldsButKeepsId() {
        var task = new Task(TASK_ID, "Old", "Old description", false);
        var request = new TaskRequest("New", "New description", true);

        mapper.updateEntity(request, task);

        assertThat(task)
                .extracting(Task::getId, Task::getTitle, Task::getDescription, Task::isCompleted)
                .containsExactly(TASK_ID, "New", "New description", true);
    }
}
