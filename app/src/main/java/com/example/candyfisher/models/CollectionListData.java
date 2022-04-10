package com.example.candyfisher.models;

public class CollectionListData {
    private boolean collected;
    private int imageId;
    private String description;

    public CollectionListData(String description, int imageId, boolean collected){
        this.description = description;
        this.imageId = imageId;
        this.collected = collected;
    }

    public void swapCollected(){
        collected = !collected;
    }

    public boolean getCollected(){
        return collected;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public String getDescription(){
        return description;
    }

    public void setImageId(int imageId){
        this.imageId = imageId;
    }

    public int getImageId(){
        return imageId;
    }

}
