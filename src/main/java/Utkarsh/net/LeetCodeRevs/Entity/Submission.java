package Utkarsh.net.LeetCodeRevs.Entity;

import lombok.Data;


@Data
public class Submission {

    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}