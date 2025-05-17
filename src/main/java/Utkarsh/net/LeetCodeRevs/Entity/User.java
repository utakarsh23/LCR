package Utkarsh.net.LeetCodeRevs.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mongodb.lang.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.checkerframework.common.aliasing.qual.Unique;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@Document(collection = "user")
public class User {
    @Id
    private ObjectId id;
    private String email;
    private String password;
    private String leetCodeUserName;
    private List<Questions> submissions;

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

    public List<Questions> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<Questions> submissions) {
        this.submissions = submissions;
    }
}