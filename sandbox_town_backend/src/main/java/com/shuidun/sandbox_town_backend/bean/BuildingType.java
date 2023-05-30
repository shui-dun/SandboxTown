package com.shuidun.sandbox_town_backend.bean;


public class BuildingType {

  private String id;
  private String description;
  private long basicPrice;
  private String imagePath;


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }


  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  public long getBasicPrice() {
    return basicPrice;
  }

  public void setBasicPrice(long basicPrice) {
    this.basicPrice = basicPrice;
  }


  public String getImagePath() {
    return imagePath;
  }

  public void setImagePath(String imagePath) {
    this.imagePath = imagePath;
  }

}
