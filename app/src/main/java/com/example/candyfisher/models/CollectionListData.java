package com.example.candyfisher.models;

public class CollectionListData {
    private int collected;
    private int imageId;
    private String description;
    private final int realIndex;

    public CollectionListData(String description, int imageId, int collected, int realIndex) {
        this.description = description;
        this.imageId = imageId;
        this.collected = collected;
        this.realIndex = realIndex;
    }

    public void incrementCollected() {
        collected += 1;
    }

    public void decrementCollected() {
        collected -= 1;
    }

    public int getNumCollected() {
        return collected;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getImageId() {
        return imageId;
    }

    public int getRealIndex(){return realIndex;}

}
