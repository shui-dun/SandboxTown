package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Player {

  private String username;
  private long money;
  private long exp;
  private long level;
  private long hunger;
  private long hp;
  private long attack;
  private long defense;
  private long speed;
  private long X;
  private long Y;


  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }


  public long getMoney() {
    return money;
  }

  public void setMoney(long money) {
    this.money = money;
  }


  public long getExp() {
    return exp;
  }

  public void setExp(long exp) {
    this.exp = exp;
  }


  public long getLevel() {
    return level;
  }

  public void setLevel(long level) {
    this.level = level;
  }


  public long getHunger() {
    return hunger;
  }

  public void setHunger(long hunger) {
    this.hunger = hunger;
  }


  public long getHp() {
    return hp;
  }

  public void setHp(long hp) {
    this.hp = hp;
  }


  public long getAttack() {
    return attack;
  }

  public void setAttack(long attack) {
    this.attack = attack;
  }


  public long getDefense() {
    return defense;
  }

  public void setDefense(long defense) {
    this.defense = defense;
  }


  public long getSpeed() {
    return speed;
  }

  public void setSpeed(long speed) {
    this.speed = speed;
  }


  public long getX() {
    return X;
  }

  public void setX(long X) {
    this.X = X;
  }


  public long getY() {
    return Y;
  }

  public void setY(long Y) {
    this.Y = Y;
  }

  @Override
  public String toString() {
    return "Player{" +
            "username='" + username + '\'' +
            ", money=" + money +
            ", exp=" + exp +
            ", level=" + level +
            ", hunger=" + hunger +
            ", hp=" + hp +
            ", attack=" + attack +
            ", defense=" + defense +
            ", speed=" + speed +
            ", X=" + X +
            ", Y=" + Y +
            '}';
  }
}
