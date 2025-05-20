package Utkarsh.net.LeetCodeRevs.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

//used for question searching from the datasheet
@Data
@NoArgsConstructor //says such that no arg-constructors is req for this class
@Document(collection = "questions")
public class DbQuestions {

    @Id
    private ObjectId id;

    private String name;

    private List<String> tags;

    private List<TestCase> testcases;


    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<TestCase> getTestcases() {
        return testcases;
    }

    public void setTestcases(List<TestCase> testcases) {
        this.testcases = testcases;
    }
}