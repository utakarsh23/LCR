package Utkarsh.net.LeetCodeRevs.Repository;

import Utkarsh.net.LeetCodeRevs.Entity.Questions;
import Utkarsh.net.LeetCodeRevs.Entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuestionRepository extends MongoRepository<Questions, ObjectId> {

}
