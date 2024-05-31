package com.example.homescreen;

import java.io.Serializable;

public class ListData implements Serializable {
    private String name;
    private String time;
    private int image;
    private int deleteIcon;

    public ListData(String name, String time, int image, int deleteIcon) {
        this.name = name;
        this.time = time;
        this.image = image;
        this.deleteIcon = deleteIcon;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public int getImage() {
        return image;
    }

    public int getDeleteIcon() {
        return deleteIcon;
    }
}
