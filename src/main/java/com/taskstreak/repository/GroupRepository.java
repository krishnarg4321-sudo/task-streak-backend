package com.taskstreak.repository;

import com.taskstreak.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface GroupRepository extends MongoRepository<Group, String> {
    List<Group> findByMemberIdsContaining(String userId);
    List<Group> findByOwnerId(String ownerId);
}
