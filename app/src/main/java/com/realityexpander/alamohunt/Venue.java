package com.realityexpander.alamohunt;

/**
 * Created by test2 on 4/27/18.
 */

public class Venue {
    private String name;
    private String Id;
    private String categoryName;


    public Venue(String name, String id, String categoryName){
        this.setName(name);
        this.setId(id);
        this.setCategoryName(categoryName);
    }

    public String getName() {
        return name;
    }
    public String getId() {
        return Id;
    }
    public String getCategoryName() {
        return categoryName;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setId(String id) {
        this.Id = id;
    }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}