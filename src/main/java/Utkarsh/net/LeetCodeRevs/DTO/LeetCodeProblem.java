package Utkarsh.net.LeetCodeRevs.DTO;

import java.util.List;

public class LeetCodeProblem {
    private String questionId;
    private String title;
    private String content;
    private String difficulty;
    private int likes;
    private int dislikes;
    private String exampleTestcases;
    private List<String> topicTags;

    // Getters and Setters
    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getDislikes() { return dislikes; }
    public void setDislikes(int dislikes) { this.dislikes = dislikes; }

    public String getExampleTestcases() { return exampleTestcases; }
    public void setExampleTestcases(String exampleTestcases) { this.exampleTestcases = exampleTestcases; }

    public List<String> getTopicTags() { return topicTags; }
    public void setTopicTags(List<String> topicTags) { this.topicTags = topicTags; }

    @Override
    public String toString() {
        return "Problem: " + title + "\n" +
               "Difficulty: " + difficulty + "\n" +
               "Likes: " + likes + ", Dislikes: " + dislikes + "\n" +
               "Example Testcases: " + exampleTestcases + "\n" +
               "Topics: " + String.join(", ", topicTags);
    }
}