package com.mricotta.task.mapper;

import com.mricotta.task.dto.TaskRequest;
import com.mricotta.task.dto.TaskResponse;
import com.mricotta.task.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    Task toEntity(TaskRequest request);

    TaskResponse toDto(Task task);

    @Mapping(target = "id", ignore = true)
    void updateEntity(TaskRequest request, @MappingTarget Task task);
}
