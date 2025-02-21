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

    Questions findByQuestionLink(String questionLink);

    @Aggregation(pipeline = { "{ $sample: { size: 1 } }" })
    Optional<Questions> findRandomQuestion();


    @Query("{ 'questionName': { $nin: ?0 } }")
    List<Questions> findUnsolvedQuestions(List<String> solvedQuestionNames);

    List<Questions> findQuestionsByUser(User user);

    List<Questions> searchQuestionsByQuestionName(String questionName);

    List<Questions> findQuestionsByQuestionName(String questionName);
}
