package Utkarsh.net.LeetCodeRevs.DTO;

import Utkarsh.net.LeetCodeRevs.Entity.Submission;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "LCResponse")
public class LeetCodeResponse {
    private int count;
    private List<Submission> submission;
    private String link;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    // Getters and setters
    public List<Submission> getSubmission() {
        return submission;
    }

    public void setSubmission(List<Submission> submission) {
        this.submission = submission;
    }
}