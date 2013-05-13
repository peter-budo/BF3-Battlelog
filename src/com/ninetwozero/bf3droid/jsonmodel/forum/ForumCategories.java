package com.ninetwozero.bf3droid.jsonmodel.forum;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ForumCategories {
    @SerializedName("positionId")
    private int positionId;
    @SerializedName("title")
    private String title;
    @SerializedName("forums")
    List<Forums> forums;
    @SerializedName("id")
    private long id;
}
