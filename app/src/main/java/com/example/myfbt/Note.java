package com.example.myfbt;

import androidx.appcompat.app.AlertDialog;

public class Note {
    private String title;
    private String description;

    public Note(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return "Title: " + title + "\nContent: " + description;
    }


}