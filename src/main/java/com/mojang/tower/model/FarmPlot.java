package com.mojang.tower.model;

import com.mojang.tower.data.Resources;

import java.awt.Graphics2D;

/**
 * Represents a farm plot entity in the game.
 */
public class FarmPlot extends Entity {
    public static final int GROW_SPEED = 200;
    public static final int MAX_AGE = 7 * GROW_SPEED;
    public static final int HARVEST_STAMINA_COST = 64;
    public static final int AGE_DIVISOR = 4;
    public static final int Y_POSITION_ADJUSTMENT = 5;

    private int age;
    private int stamina;
    private int submit;

    public FarmPlot(double x, double y, int initialAge) {
        super(x, y, 0);
        this.age = initialAge;
        this.stamina = initialAge;
        this.submit = initialAge;
    }


    @Override
    public void tick() {
        if (age < MAX_AGE) {
            age++;
            stamina++;
            submit++;
        }
    }


    @Override
    public void render(Graphics2D g, double alpha) {
        int xPos = (int) (xr - AGE_DIVISOR);
        int yPos = -(int) (yr / 2 + Y_POSITION_ADJUSTMENT);

        int imageIndex = Math.max(0, 7 - age / GROW_SPEED);
        g.drawImage(bitmaps.getFarmPlots()[imageIndex], xPos, yPos, null);
    }

    public void cut() {
        alive = false;
    }


    @Override
    public boolean gatherResource(int resourceId) {
        stamina -= HARVEST_STAMINA_COST;
        if (stamina <= 0) {
            alive = false;
            return true;
        }
        return false;
    }

    public int getAge() {
        return age / GROW_SPEED;
    }

    @Override
    public boolean givesResource(int resourceId) {
        return getAge() > 6 && resourceId == Resources.RESOURCE_FOOD_ID;
    }

}
