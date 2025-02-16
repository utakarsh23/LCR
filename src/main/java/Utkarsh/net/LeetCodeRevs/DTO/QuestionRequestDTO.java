package Utkarsh.net.LeetCodeRevs.DTO;

import lombok.Data;

import java.util.List;

@Data
public class QuestionRequestDTO {
    public String getQuestionLink() {
        return questionLink;
    }

    public void setQuestionLink(String questionLink) {
        this.questionLink = questionLink;
    }

    public List<String> getSolution() {
        return solution;
    }

    public void setSolution(List<String> solution) {
        this.solution = solution;
    }

    private String questionLink;
    private List<String> solution;

}
