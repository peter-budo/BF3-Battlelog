package com.ninetwozero.bf3droid.jsonmodel.forum;

import com.google.gson.annotations.SerializedName;

public class Forums {
    @SerializedName("numberOfPosts")
    private int numberOfPosts;
    @SerializedName("positionId")
    private int positionId;
    @SerializedName("description")
    private String description;
    @SerializedName("categoryId")
    private long categoryId;
    @SerializedName("numberOfThreads")
    private int numberOfThreads;
    @SerializedName("id")
    private long id;

    public int getNumberOfPosts() {
        return numberOfPosts;
    }

    public int getPositionId() {
        return positionId;
    }

    public String getDescription() {
        return description;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public long getId() {
        return id;
    }
}
