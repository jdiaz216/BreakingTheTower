package com.mojang.tower.model;

import com.mojang.tower.sound.Sound;
import com.mojang.tower.sound.Sounds;
import com.mojang.tower.data.Bitmaps;

import java.awt.Graphics2D;

public class Peon extends Entity {
    private static final int[] animSteps = {0, 1, 0, 2};
    private static final int[] animDirs = {2, 0, 3, 1};
    private static final int TYPE_WARRIOR = 1;
    private static final int TYPE_REGULAR = 0;
    private double rot = 0;
    private double moveTick = 0;
    private int type;
    private int wanderTime = 0;
    protected Job job;

    protected double xTarget, yTarget;

    private int hp = 100;
    private int maxHp = 100;
    private int xp = 0;
    private int nextLevel = 1;
    private int level = 0;

    public Peon(double x, double y, int type) {
        super(x, y, 1);
        this.type = type;
        rot = random.nextDouble() * Math.PI * 2;
        moveTick = random.nextInt(4 * 3);
    }

    public void init(Island island, Bitmaps bitmaps) {
        super.init(island, bitmaps);
        island.increasePopulation();
    }

    public void fight(Monster monster) {
        if (job == null && (type == TYPE_WARRIOR || random.nextInt(10) == 0)) {
            setJob(new Job.Hunt(monster));
        }
        if (type == TYPE_REGULAR) {
            monster.fight(this);
            if ((hp -= 4) <= 0) die();
        } else {
            monster.fight(this);
            if (--hp <= 0) die();
        }
    }

    public void die() {
        Sounds.play(new Sound.Death());
        island.decreasePopulation();
        if (type == TYPE_WARRIOR) {
            island.decreaseWarriorPopulation();
        }
        alive = false;
    }

    public void setJob(Job job) {
        this.job = job;
        if (job != null) job.init(island, this);
    }

    public void tick() {
        if (job != null) {
            job.tick();
        }

        if (type == TYPE_WARRIOR || job == null) {
            for (int i = 0; i < 15 && (job == null || job instanceof Job.Goto); i++) {
                TargetFilter monsterFilter = new TargetFilter() {
                    public boolean accepts(Entity e) {
                        return e.isAlive() && (e instanceof Monster);
                    }
                };
                Entity e = type == TYPE_REGULAR ? getRandomTarget(30, 15, monsterFilter) : getRandomTarget(70, 80, monsterFilter);
                if (e instanceof Monster) {
                    setJob(new Job.Hunt((Monster) e));
                }
            }
        }

        increaseHp();

        double speed = 1;
        if (wanderTime == 0 && job != null && job.hasTarget()) {
            double xd = job.getxTarget() - x;
            double yd = job.getyTarget() - y;
            double rd = job.getTargetDistance() + r;
            if (xd * xd + yd * yd < rd * rd) {
                job.arrived();
                speed = 0;
            }
            rot = Math.atan2(yd, xd);
        } else {
            rot += (random.nextDouble() - 0.5) * random.nextDouble() * 2;
        }

        if (wanderTime > 0) wanderTime--;

        speed += level * 0.1;

        double xt = x + Math.cos(rot) * 0.4 * speed;
        double yt = y + Math.sin(rot) * 0.4 * speed;
        if (island.isFree(xt, yt, r, this)) {
            x = xt;
            y = yt;
        } else {
            if (job != null) {
                Entity collided = island.getEntityAt(xt, yt, r, null, this);
                if (collided != null) {
                    job.collide(collided);
                } else {
                    job.cantReach();
                }
            }
            rot = (random.nextDouble()) * Math.PI * 2;
            wanderTime = random.nextInt(30) + 3;
        }

        moveTick += speed;
        super.tick();
    }

    private void increaseHp() {
        if (hp < maxHp && random.nextInt(5) == 0) {
            hp++;
        }
    }

    public void render(Graphics2D g, double alpha) {
        int rotationStep = Monster.getRotationStep();
        int animationStep = Monster.getAnimationStep();

        int x = (int) (xr - 4);
        int y = -(int) (yr / 2 + 8);

        int carrying = -1;
        if (job != null) carrying = job.getCarried();

        if (carrying >= 0) {
            g.drawImage(bitmaps.getPeons()[2][animDirs[rotationStep & 3] * 3 + animationStep], x, y, null);
            g.drawImage(bitmaps.getCarriedResources()[carrying], x, y - 3, null);
        } else {
            g.drawImage(bitmaps.getPeons()[type][animDirs[rotationStep & 3] * 3 + animationStep], x, y, null);
        }

        Monster.drawHealthBar(g, x, y, hp, maxHp);
    }

    public void setType(int i) {
        this.type = i;
        hp = maxHp = type == 0 ? 20 : 100;
    }

    public void addXp() {
        xp++;
        if (xp == nextLevel) {
            nextLevel = nextLevel * 2 + 1;
            island.addEntity(new InfoPuff(x, y, 0));
            hp += 10;
            maxHp += 10;
            level++;
            Sounds.play(new Sound.Ding());
        }
    }

    public double getRot() {
        return rot;
    }

    public int getType() {
        return type;
    }

    public void increaseRot(double value) {
        this.rot += value;
    }

    @Override
    public boolean isTargetable() {
        return true;
    }
}