package Utkarsh.net.LeetCodeRevs.Entity;

import lombok.Data;

//to store title(name) for the question, API structure for calling and saving the api
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