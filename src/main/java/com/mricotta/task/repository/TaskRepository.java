package com.mricotta.task.repository;

import com.mricotta.task.entity.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {

    List<Task> findAllByCompleted(boolean completed);
}
