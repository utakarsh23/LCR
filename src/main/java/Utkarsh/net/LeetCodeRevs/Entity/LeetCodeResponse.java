package Utkarsh.net.LeetCodeRevs.Entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "LCResponse")
public class LeetCodeResponse {
    private int count;
    private List<Submission> submission;

    // Getters and setters
    public List<Submission> getSubmission() {
        return submission;
    }

    public void setSubmission(List<Submission> submission) {
        this.submission = submission;
    }
}