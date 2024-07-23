package com.mojang.tower.data;

import com.mojang.tower.model.HouseType;

public class Resources {
    public static final int WOOD = 0;
    public static final int ROCK = 1;
    public static final int FOOD = 2;

    private int wood = 100;
    private int rock = 100;
    private int food = 100;

    public void add(int resourceId, int count) {
        switch (resourceId) {
            case WOOD:
                wood += count;
                break;
            case ROCK:
                rock += count;
                break;
            case FOOD:
                food += count;
                break;
        }
    }

    public void charge(HouseType type) {
        wood -= type.getWood();
        rock -= type.getRock();
        food -= type.getFood();
    }

    public boolean canAfford(HouseType type) {
        if (wood < type.getWood()) return false;
        if (rock < type.getRock()) return false;
        if (food < type.getFood()) return false;
        return true;
    }

    public int getWood() {
        return wood;
    }

    public int getRock() {
        return rock;
    }

    public int getFood() {
        return food;
    }

    public void decreaseWood(int value) {
        this.wood -= value;
    }

    public void decreaseFood(int value) {
        this.food -= value;
    }

    public void increaseWood(int value) {
        this.wood += value;
    }

    public void increaseRock(int value) {
        this.rock += value;
    }
}
