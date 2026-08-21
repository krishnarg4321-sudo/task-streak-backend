package com.taskstreak.repository;

import com.taskstreak.model.Friendship;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends MongoRepository<Friendship, String> {
    List<Friendship> findByUserId(String userId);
    List<Friendship> findByFriendId(String friendId);
    List<Friendship> findByUserIdOrFriendId(String userId, String friendId);
    Optional<Friendship> findByUserIdAndFriendId(String userId, String friendId);
    List<Friendship> findByFriendIdAndStatus(String friendId, Friendship.Status status);
}
