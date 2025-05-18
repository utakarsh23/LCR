package Utkarsh.net.LeetCodeRevs.Entity;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class UserQuestionData {
    private String title;
    private String link;
    private List<String> tags;
    private double weight;
    private String lastUpdatedTimestamp;
    private boolean coolDown;
    private LocalDate lastAssigned;
    Map<String, String> testCases;

    public Map<String, String> getTestCases() {
        return testCases;
    }

    public void setTestCases(Map<String, String> testCases) {
        this.testCases = testCases;
    }

    public LocalDate getLastAssigned() {
        return lastAssigned;
    }

    public void setLastAssigned(LocalDate lastAssigned) {
        this.lastAssigned = lastAssigned;
    }

    public boolean isCoolDown() {
        return coolDown;
    }

    public void setCoolDown(boolean coolDown) {
        this.coolDown = coolDown;
    }

    public String getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(String lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}