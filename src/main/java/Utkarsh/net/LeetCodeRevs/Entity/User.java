package Utkarsh.net.LeetCodeRevs.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mongodb.lang.Nullable;
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

    @Nullable
    private String leetCodeUserName;

    private List<String> submissions;

    public List<String> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<String> submissions) {
        this.submissions = submissions;
    }

    @DBRef
    private List<Questions> questions;

    private LeetCodeSubmissions leetCodeSubmissions;


    private String dailyQuestion;

    /// used only for ref so we could get Question from user profile for ans check for the solution with gemini
    private ObjectId dailyQuesID;

    public void setLeetCodeUserName(String leetCodeUserName) {
        this.leetCodeUserName = leetCodeUserName;
    }

    public String getLeetCodeUserName() {
        return leetCodeUserName;
    }

    public LeetCodeSubmissions getLeetCodeSubmissions() {
        return leetCodeSubmissions;
    }

    public void setLeetCodeSubmissions(LeetCodeSubmissions leetCodeSubmissions) {
        this.leetCodeSubmissions = leetCodeSubmissions;
    }

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
