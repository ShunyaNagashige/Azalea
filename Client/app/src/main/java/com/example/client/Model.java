package com.example.client;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Model {

    @SerializedName("lang")
    @Expose
    public String lang;
    @SerializedName("message")
    @Expose
    public String message;

}
