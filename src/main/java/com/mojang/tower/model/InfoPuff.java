package com.mojang.tower.model;

import java.awt.*;

public class InfoPuff extends Entity {
    private static final double DECAY_RATE = 0.99;
    private static final int INITIAL_Z = 12;
    private static final double INITIAL_ZA = 0.3;
    private static final int MIN_LIFE_TIME = 80;
    private static final int LIFE_TIME_VARIATION = 60;

    private double xa;
    private double ya;
    private double za;
    private double z;
    private int life;
    private int lifeTime;
    private int image;

    public InfoPuff(double x, double y, int image) {
        super(x, y, -1);
        this.image = image;
        this.z = INITIAL_Z;
        this.za = INITIAL_ZA;
        this.life = 0;
        this.lifeTime = MIN_LIFE_TIME + random.nextInt(LIFE_TIME_VARIATION);
    }

    @Override
    public void updatePos(double sin, double cos, double alpha) {
        super.updatePos(sin, cos, alpha);
        this.yr -= 2;
    }

    @Override
    public void tick() {
        applyDecay();
        updatePosition();
        checkLife();
    }

    private void applyDecay() {
        xa *= DECAY_RATE;
        ya *= DECAY_RATE;
        za *= DECAY_RATE;
    }

    private void updatePosition() {
        x += xa;
        y += ya;
        z += za;
    }

    private void checkLife() {
        if (++life >= lifeTime) {
            alive = false;
        }
    }

    @Override
    public void render(Graphics2D g, double alpha) {
        int renderX = (int) (xr - 8);
        int renderY = -(int) (yr / 2 + 4 + (z + za * alpha));

        g.drawImage(bitmaps.getInfoPuffs()[image], renderX, renderY, null);
    }
}
