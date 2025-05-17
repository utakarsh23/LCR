package Utkarsh.net.LeetCodeRevs.Entity;

import lombok.Data;

import java.util.List;

@Data
public class UserQuestionData {
    private String title;
    private String link;
    private List<String> tags;
    private double weight;
    private boolean wasRecentlyUsed; // optional, for cooldowns
}