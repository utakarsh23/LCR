package Utkarsh.net.LeetCodeRevs.Repository;

import Utkarsh.net.LeetCodeRevs.Entity.Questions;
import Utkarsh.net.LeetCodeRevs.Entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends MongoRepository<Questions, ObjectId> {

    @Aggregation(pipeline = { "{ $sample: { size: 1 } }" })
    Optional<Questions> findRandomQuestion();

    List<Questions> findQuestionsByUser(User user);

    List<Questions> getQuestionsById(ObjectId id);
}
