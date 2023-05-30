package com.shuidun.sandbox_town_backend.bean;


public class Building {

  private String id;
  private String type;
  private String map;
  private long level;
  private String owner;
  private long originX;
  private long originY;
  private long displayWidth;
  private long displayHeight;


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }


  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


  public String getMap() {
    return map;
  }

  public void setMap(String map) {
    this.map = map;
  }


  public long getLevel() {
    return level;
  }

  public void setLevel(long level) {
    this.level = level;
  }


  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }


  public long getOriginX() {
    return originX;
  }

  public void setOriginX(long originX) {
    this.originX = originX;
  }


  public long getOriginY() {
    return originY;
  }

  public void setOriginY(long originY) {
    this.originY = originY;
  }


  public long getDisplayWidth() {
    return displayWidth;
  }

  public void setDisplayWidth(long displayWidth) {
    this.displayWidth = displayWidth;
  }


  public long getDisplayHeight() {
    return displayHeight;
  }

  public void setDisplayHeight(long displayHeight) {
    this.displayHeight = displayHeight;
  }

}
