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
    private String title;
    private String link;
    private List<String> topicTags;


    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<String> getTopicTags() {
        return topicTags;
    }

    public void setTopicTags(List<String> topicTags) {
        this.topicTags = topicTags;
    }
}