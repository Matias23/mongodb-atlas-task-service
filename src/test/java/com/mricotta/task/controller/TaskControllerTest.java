package com.mricotta.task.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mricotta.task.dto.TaskRequest;
import com.mricotta.task.dto.TaskResponse;
import com.mricotta.task.exception.TaskNotFoundException;
import com.mricotta.task.service.TaskService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    private static final String TASK_ID = "66e8a1f2c3b4d5e6f7a8b9c0";

    private static final String VALID_BODY = """
            {"title":"Write docs","description":"README for the service","completed":false}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void createTask_returnsCreatedWithLocationAndBody() throws Exception {
        given(taskService.createTask(any(TaskRequest.class))).willReturn(response(false));

        mockMvc.perform(post("/v1/tasks").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/v1/tasks/" + TASK_ID)))
                .andExpect(jsonPath("$.id").value(TASK_ID))
                .andExpect(jsonPath("$.title").value("Write docs"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void createTask_withInvalidBody_returnsBadRequestWithFieldErrors() throws Exception {
        var invalidBody = """
                {"title":"","completed":null}
                """;

        mockMvc.perform(post("/v1/tasks").contentType(MediaType.APPLICATION_JSON).content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.completed").exists());

        verifyNoInteractions(taskService);
    }

    @Test
    void getAllTasks_returnsOkWithList() throws Exception {
        given(taskService.getAllTasks()).willReturn(List.of(response(false)));

        mockMvc.perform(get("/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(TASK_ID));
    }

    @Test
    void getTask_returnsOkWithBody() throws Exception {
        given(taskService.getTask(TASK_ID)).willReturn(response(false));

        mockMvc.perform(get("/v1/tasks/{id}", TASK_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TASK_ID));
    }

    @Test
    void getTask_whenMissing_returnsNotFound() throws Exception {
        given(taskService.getTask(TASK_ID)).willThrow(new TaskNotFoundException(TASK_ID));

        mockMvc.perform(get("/v1/tasks/{id}", TASK_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task with id %s not found".formatted(TASK_ID)))
                .andExpect(jsonPath("$.path").value("/v1/tasks/" + TASK_ID));
    }

    @Test
    void updateTask_returnsOkWithBody() throws Exception {
        given(taskService.updateTask(eq(TASK_ID), any(TaskRequest.class))).willReturn(response(true));

        mockMvc.perform(put("/v1/tasks/{id}", TASK_ID).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void updateTask_whenMissing_returnsNotFound() throws Exception {
        given(taskService.updateTask(eq(TASK_ID), any(TaskRequest.class)))
                .willThrow(new TaskNotFoundException(TASK_ID));

        mockMvc.perform(put("/v1/tasks/{id}", TASK_ID).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/v1/tasks/{id}", TASK_ID))
                .andExpect(status().isNoContent());

        then(taskService).should().deleteTask(TASK_ID);
    }

    @Test
    void deleteTask_whenMissing_returnsNotFound() throws Exception {
        willThrow(new TaskNotFoundException(TASK_ID)).given(taskService).deleteTask(TASK_ID);

        mockMvc.perform(delete("/v1/tasks/{id}", TASK_ID))
                .andExpect(status().isNotFound());
    }

    private static TaskResponse response(boolean completed) {
        return new TaskResponse(TASK_ID, "Write docs", "README for the service", completed);
    }
}
