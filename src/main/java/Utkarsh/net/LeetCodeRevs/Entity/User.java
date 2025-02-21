package Utkarsh.net.LeetCodeRevs.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "user")
public class User {
    @Id
    private ObjectId id;
    private String email;
    private String password;

    @DBRef
    private List<Questions> questions;

    private String dailyQuestion;

    //used only for ref
    private ObjectId dailyQuesID;

    public ObjectId getDailyQuesID() {
        return dailyQuesID;
    }

    public void setDailyQuesID(ObjectId dailyQuesID) {
        this.dailyQuesID = dailyQuesID;
    }

    public String getDailyQuestion() {
        return dailyQuestion;
    }

    public void setDailyQuestion(String dailyQuestion) {
        this.dailyQuestion = dailyQuestion;
    }

    public List<Questions> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Questions> questions) {
        this.questions = questions;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
