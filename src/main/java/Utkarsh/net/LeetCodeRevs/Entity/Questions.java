package Utkarsh.net.LeetCodeRevs.Entity;

import Utkarsh.net.LeetCodeRevs.DTO.LeetCodeProblem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "questions")
public class Questions {

    @Id
    private ObjectId id;
    private String questionLink;
    private String questionName; // Auto-generated name
    private LeetCodeProblem questionData;
    private List<String> solutions; // Store multiple solutions

    @DBRef
    @JsonIgnore
    private User user;


    @CreatedDate
    private LocalDateTime createdDate;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getQuestionLink() {
        return questionLink;
    }

    public void setQuestionLink(String questionLink) {
        this.questionLink = questionLink;
    }

    public String getQuestionName() {
        return questionName;
    }

    public void setQuestionName(String questionName) {
        this.questionName = questionName;
    }

    public List<String> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<String> solutions) {
        this.solutions = solutions;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LeetCodeProblem getQuestionData() {
        return questionData;
    }

    public void setQuestionData(LeetCodeProblem questionData) {
        this.questionData = questionData;
    }


    @Override
    public String toString() {
        return "questionData=" + questionData;
    }
}