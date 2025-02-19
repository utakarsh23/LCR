package Utkarsh.net.LeetCodeRevs.Repository;

import Utkarsh.net.LeetCodeRevs.Entity.Questions;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends MongoRepository<Questions, ObjectId> {

    Questions findByQuestionLink(String questionLink);

    @Aggregation(pipeline = { "{ $sample: { size: 1 } }" })
    Optional<Questions> findRandomQuestion();
}
