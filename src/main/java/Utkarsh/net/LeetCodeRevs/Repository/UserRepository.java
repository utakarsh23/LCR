package Utkarsh.net.LeetCodeRevs.Repository;

import Utkarsh.net.LeetCodeRevs.Entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

public interface UserRepository extends MongoRepository<User, ObjectId> {

    User findUserByEmail(String email);

    boolean existsByEmail(String email);
}
