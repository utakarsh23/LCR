package Utkarsh.net.LeetCodeRevs.DTO;

import java.util.List;

public class LeetCodeProblem {
    private String questionId;
    private String title;
    private String content;
    private String difficulty;
    private String exampleTestcases;
    private List<String> topicTags;
    private String url;

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getExampleTestcases() { return exampleTestcases; }
    public void setExampleTestcases(String exampleTestcases) { this.exampleTestcases = exampleTestcases; }

    public List<String> getTopicTags() { return topicTags; }
    public void setTopicTags(List<String> topicTags) { this.topicTags = topicTags; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    @Override
    public String toString() {
        return "Problem: " + title + "\n" +
                "Difficulty: " + difficulty + "\n" +
                "Example Testcases: " + exampleTestcases + "\n" +
                "Topics: " + String.join(", ", topicTags) + "\n" +
                "URL: " + url;  // Include the URL in the string representation
    }
}