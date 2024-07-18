package com.mojang.tower;

import java.awt.Graphics2D;

/**
 * Represents a farm plot entity in the game.
 */
public class FarmPlot extends Entity
{
    public static final int GROW_SPEED = 200;

    private int age;
    private int stamina;
    private int yield;

    public FarmPlot(double x, double y, int initialAge) {
        super(x, y, 0);
        this.age = initialAge;
        this.stamina = initialAge;
        this.yield = initialAge;
    }


    @Override
    public void tick()
    {
        if (age < 7 * GROW_SPEED)
        {
            age++;
            stamina++;
            yield++;
        }
    }

    public void render(Graphics2D g, double alpha)
    {
        int x = (int) (xr - 4);
        int y = -(int) (yr / 2 + 5);

        g.drawImage(bitmaps.farmPlots[7 - age / GROW_SPEED], x, y, null);
    }

    public void cut()
    {
        alive = false;
    }

    public boolean gatherResource(int resourceId)
    {
        stamina -= 64;
        if (stamina <= 0)
        {
            alive = false;
            return true;
        }
        return false;
    }

    public int getAge()
    {
        return age/GROW_SPEED;
    }
    
    public boolean givesResource(int resourceId)
    {
        return getAge()>6 && resourceId==Resources.FOOD;
    }
    
}
