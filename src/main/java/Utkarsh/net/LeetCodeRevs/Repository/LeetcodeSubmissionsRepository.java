package Utkarsh.net.LeetCodeRevs.Repository;

import Utkarsh.net.LeetCodeRevs.Entity.LeetCodeSubmissions;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LeetcodeSubmissionsRepository extends MongoRepository<LeetCodeSubmissions, ObjectId> {
}
