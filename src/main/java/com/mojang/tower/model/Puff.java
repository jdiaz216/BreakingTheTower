package com.mojang.tower.model;

import java.awt.*;

public class Puff extends Entity {
    public void updatePos(double sin, double cos, double alpha) {
        super.updatePos(sin, cos, alpha);
        yr -= 2;
    }

    private double xa, ya, za;
    private double z = 0;
    private int life;
    private int lifeTime;

    public Puff(double x, double y) {
        super(x, y, -1);
        z = 7;
        za = 0.3;
        life = 0;
        lifeTime = 80 + random.nextInt(60);
    }

    public void tick() {
        xa *= 0.99;
        ya *= 0.99;
        za *= 0.99;
        xa += 0.002;
        x += xa;
        y += ya;
        z += za;

        if (life++ == lifeTime) alive = false;
    }

    public void render(Graphics2D g, double alpha) {
        int x = (int) (xr - 4);
        int y = -(int) (yr / 2 + 4 + (z + za * alpha));

        int age = life * 6 / lifeTime;
        if (age <= 4)
            g.drawImage(bitmaps.getSmoke()[age], x, y, null);
    }
}