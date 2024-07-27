package com.mojang.tower.model;

import com.mojang.tower.sound.Sound;
import com.mojang.tower.sound.Sounds;
import com.mojang.tower.data.Bitmaps;

import java.awt.Color;
import java.awt.Graphics2D;

public class Monster extends Entity {
    private static final int[] ANIM_STEPS = {0, 1, 0, 2};
    private static final int[] ANIM_DIRS = {2, 0, 3, 1};


    private static final double MOVEMENT_SPEED = 0.3;

    private static final int HP_REGEN_INTERVAL = 16;

    private static final int RANDOM_TARGET_CHANCE = 100;

    private static final int WANDER_TIME_MAX = 30;
    private static double rotation = 0;
    private static double moveTick = 0;
    private int wanderTime = 0;

    protected Entity target;
    private int hp = 100;
    private final int maxHp = 100;

    public Monster(double x, double y) {
        super(x, y, 2);
        rotation = random.nextDouble() * Math.PI * 2;
        moveTick = random.nextInt(4 * 3);
    }

    public void init(Island island, Bitmaps bitmaps) {
        super.init(island, bitmaps);
        island.increaseMonsterPopulation();
    }

    public void die() {
        Sounds.play(new Sound.MonsterDeath());
        island.decreaseMonsterPopulation();
        alive = false;
    }

    public void tick() {
        regenerateHealth();
        updateTarget();
        moveMonster();
        super.tick();
    }

    private void regenerateHealth() {
        if (hp < maxHp && random.nextInt(HP_REGEN_INTERVAL) == 0) {
            hp = Math.min(hp + 1, maxHp); // Ensure hp does not exceed maxHp
        }
    }

    private void updateTarget() {
        if (target == null || random.nextInt(RANDOM_TARGET_CHANCE) == 0) {
            Entity e = getRandomTarget(60, 30, new TargetFilter() {
                public boolean accepts(Entity e) {
                    return e.isAlive() && e.isTargetable(); // polymorphism
                }
            });
            if (e != null && e.isTargetable()) { // polymorphism
                target = e;
            }
        }
        if (target != null && !target.isAlive()) {
            target = null;
        }
    }

    private void moveMonster() {
        double speed = 1;
        if (wanderTime == 0 && target != null) {
            double xd = target.x - x;
            double yd = target.y - y;
            double rd = target.r + r + 2;
            if (xd * xd + yd * yd < rd * rd) {
                speed = 0;
                target.fight(this);
            }
            rotation = Math.atan2(yd, xd);
        } else {
            rotation += (random.nextDouble() - 0.5) * random.nextDouble();
        }

        if (wanderTime > 0) wanderTime--;

        double xt = x + Math.cos(rotation) * MOVEMENT_SPEED * speed;
        double yt = y + Math.sin(rotation) * MOVEMENT_SPEED * speed;
        if (island.isFree(xt, yt, r, this)) {
            x = xt;
            y = yt;
        } else {
            handleObstacle();
        }

        moveTick += speed;
    }

    private void handleObstacle() {
        rotation += random.nextInt(2) * 2 - 1 * Math.PI / 2 + (random.nextDouble() - 0.5);
        wanderTime = random.nextInt(WANDER_TIME_MAX);
    }

    public void render(Graphics2D g, double alpha) {
        int rotationStep = getRotationStep();
        int animationStep = getAnimationStep();

        int x = (int) (xr - 4);
        int y = -(int) (yr / 2 + 8);
        g.drawImage(bitmaps.getPeons()[3][ANIM_DIRS[rotationStep & 3] * 3 + animationStep], x, y, null);

        drawHealthBar(g, x, y, hp, maxHp);
    }

    protected static int getRotationStep() {
        return (int) Math.floor((rotation - island.getRot()) * 4 / (Math.PI * 2) + 0.5);
    }

    protected static int getAnimationStep() {
        return ANIM_STEPS[(int) (moveTick / 4) & 3];
    }

    protected static void drawHealthBar(Graphics2D g, int x, int y, int hp, int maxHp) {
        if (hp < maxHp) {
            g.setColor(Color.BLACK);
            g.fillRect(x + 2, y - 2, 4, 1);
            g.setColor(Color.RED);
            g.fillRect(x + 2, y - 2, hp * 4 / maxHp, 1);
        }
    }

    public void fight(Peon peon) {
        if (hp <= 0) return;
        if (random.nextInt(5) == 0) target = peon;
        hp = Math.max(hp - 1, 0); // Ensure hp does not go below zero
        if (hp == 0) {
            die();
            peon.addXp();
        }
    }
}