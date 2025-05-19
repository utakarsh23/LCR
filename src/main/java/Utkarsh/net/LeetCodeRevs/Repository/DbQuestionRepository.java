package Utkarsh.net.LeetCodeRevs.Repository;

import Utkarsh.net.LeetCodeRevs.Entity.DbQuestions;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DbQuestionRepository extends MongoRepository<DbQuestions, ObjectId> {

    boolean findBy(String name);

    DbQuestions findByName(String name);
}