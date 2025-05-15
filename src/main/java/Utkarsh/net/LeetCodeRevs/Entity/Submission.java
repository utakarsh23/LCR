package Utkarsh.net.LeetCodeRevs.Entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document(collection = "QuestionList")
public class Submission {

    private String title;

    // Getter and setter
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}