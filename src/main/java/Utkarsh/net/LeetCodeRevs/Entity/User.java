package Utkarsh.net.LeetCodeRevs.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "user")
@Data
public class User {
    @Id
    private ObjectId id;

    private String email;
//    @JsonIgnore
    private String password;
    private String leetCodeUserName;
    private String dailyAssignedQuestionLink;
    private String dailyAssignedTopicQuestionLink;
    private Map<String, Double> topicWeights = new HashMap<>();

    @Field("userQuestions")
    private Map<String, UserQuestionData> userQuestions;


    public String getDailyAssignedTopicQuestionLink() {
        return dailyAssignedTopicQuestionLink;
    }

    public void setDailyAssignedTopicQuestionLink(String dailyAssignedTopicQuestionLink) {
        this.dailyAssignedTopicQuestionLink = dailyAssignedTopicQuestionLink;
    }

    public Map<String, Double> getTopicWeights() {
        return topicWeights;
    }

    public void setTopicWeights(Map<String, Double> topicWeights) {
        this.topicWeights = topicWeights;
    }

    public String getDailyAssignedQuestionLink() {
        return dailyAssignedQuestionLink;
    }

    public void setDailyAssignedQuestionLink(String dailyAssignedQuestionLink) {
        this.dailyAssignedQuestionLink = dailyAssignedQuestionLink;
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

    public String getLeetCodeUserName() {
        return leetCodeUserName;
    }

    public void setLeetCodeUserName(String leetCodeUserName) {
        this.leetCodeUserName = leetCodeUserName;
    }

    public Map<String, UserQuestionData> getUserQuestions() {
        return userQuestions;
    }

    public void setUserQuestions(Map<String, UserQuestionData> userQuestions) {
        this.userQuestions = userQuestions;
    }

    @Override
    public String toString() {
        return "User{" +
                "       id=" + id + ",\n\n" +
                "       email='" + email + '\'' + "\n\n" +
                "       password='" + password + '\'' + ",\n\n" +
                "       leetCodeUserName='" + leetCodeUserName + '\'' + ",\n\n" +
                "       dailyAssignedQuestionLink='" + dailyAssignedQuestionLink + '\'' + ",\n\n" +
                "       dailyAssignedTopicQuestionLink='" + dailyAssignedTopicQuestionLink + '\'' + ",\n\n" +
                "       topicWeights=" + topicWeights + ",\n\n" +
                "       userQuestions=" + userQuestions + "\n\n" +
                '}';
    }
}