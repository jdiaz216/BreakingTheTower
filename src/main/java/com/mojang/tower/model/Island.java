package com.mojang.tower.model;

import com.mojang.tower.data.HouseType;
import com.mojang.tower.data.Resources;
import com.mojang.tower.sound.Sound;
import com.mojang.tower.sound.Sounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Island implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Island.class);

    private static final int IMAGE_SIZE = 256;
    private static final double SCALING_FACTOR = 1.5;
    private static final int ROCK_COUNT = 100;
    private static final int FOREST_COUNT = 200;
    private static final int TOWER_TRIAL_COUNT = 1;
    private static final int PEON_COUNT = 10;
    private static final double ROTATION_OFFSET_X = 40;
    private static final double ROTATION_OFFSET_Y = -120;
    private static final int TREE_GROWTH_VARIANCE = 16 * Tree.GROW_SPEED;
    private static final int INITIAL_RANDOM_SEED = 8844;

    private final transient PrincipalComponent tower;
    private final transient BufferedImage image;
    private final int[] pixels;

    private List<Entity> entities = new ArrayList<>();
    private final Random random = new Random(INITIAL_RANDOM_SEED);

    private final Resources resources = new Resources();

    private double rot;
    private int population = 0;
    private int populationCap = 10;
    private int monsterPopulation = 0;

    private int warriorPopulation = 0;
    private int warriorPopulationCap = 0;

    public Island(PrincipalComponent tower, BufferedImage image) {
        this.tower = tower;
        this.image = image;

        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        for (int i = 0; i < TOWER_TRIAL_COUNT; ) {
            double x = generateRandomCoordinate();
            double y = generateRandomCoordinate();

            Tower t = new Tower(x, y);
            if (isFree(t.x, t.y, t.r)) {
                addEntity(t);
                i++;
            }
        }

        for (int i = 0; i < 7; i++) {
            double x = generateRandomCoordinate();
            double y = generateRandomCoordinate();
            addRocks(x, y);
        }

        for (int i = 0; i < 20; i++) {
            double x = generateRandomCoordinate();
            double y = generateRandomCoordinate();
            addForrest(x, y);
        }

        double xStart = ROTATION_OFFSET_X;
        double yStart = ROTATION_OFFSET_Y;
        House house = new House(xStart, yStart, HouseType.GUARDPOST);
        house.complete();
        addEntity(house);

        for (int i = 0; i < PEON_COUNT; ) {
            double x = xStart + generateRandomCoordinateInRange(32);
            double y = yStart + generateRandomCoordinateInRange(32);

            Peon peon = new Peon(x, y, 0);
            if (isFree(peon.x, peon.y, peon.r)) {
                addEntity(peon);
                i++;
            }
        }
    }

    private double generateRandomCoordinate() {
        return (random.nextDouble() * 256 - 128) * SCALING_FACTOR;
    }

    private double generateRandomCoordinateInRange(int range) {
        return random.nextDouble() * range - range / 2.0;
    }

    private void addRocks(double xo, double yo) {
        for (int i = 0; i < ROCK_COUNT; i++) {
            double x = xo + random.nextGaussian() * 10;
            double y = yo + random.nextGaussian() * 10;
            Rock rock = new Rock(x, y);

            if (isFree(rock.x, rock.y, rock.r)) {
                addEntity(rock);
            }
        }
    }

    private void addForrest(double xo, double yo) {
        for (int i = 0; i < FOREST_COUNT; i++) {
            double x = xo + random.nextGaussian() * 20;
            double y = yo + random.nextGaussian() * 20;
            Tree tree = new Tree(x, y, random.nextInt(TREE_GROWTH_VARIANCE));

            if (isFree(tree.x, tree.y, tree.r)) {
                addEntity(tree);
            }
        }
    }

    public void addEntity(Entity entity) {
        entity.init(this, tower.getBitmaps());
        entities.add(entity);
        entity.tick();
    }

    public boolean isFree(double x, double y, double r) {
        return isFree(x, y, r, null);
    }

    public boolean isFree(double x, double y, double r, Entity source) {
        if (!isOnGround(x, y)) return false;
        return entities.stream()
                .filter(e -> e != source && e.collides(x, y, r))
                .findFirst()
                .isEmpty();
    }

    public Entity getEntityAt(double x, double y, double r, TargetFilter filter) {
        return getEntityAt(x, y, r, filter, null);
    }

    public Entity getEntityAt(double x, double y, double r, TargetFilter filter, Entity exception) {

        return entities.stream()
                .filter(e -> e != exception && (filter == null || filter.accepts(e)))
                .filter(e -> e.collides(x, y, r))
                .min(Comparator.comparingDouble(e -> distanceSquared(e.x, e.y, x, y)))
                .orElse(null);
    }

    private double distanceSquared(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return dx * dx + dy * dy;
    }

    public void tick() {
        if (monsterPopulation < 0) {
            LOGGER.info("Monster population is less than 0!!");
            monsterPopulation = 0;
        }

        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            entity.tick();
            if (!entity.isAlive()) entities.remove(i--);
        }
    }

    public boolean isOnGround(double x, double y) {
        x /= SCALING_FACTOR;
        y /= SCALING_FACTOR;
        int xp = (int) (x + 128);
        int yp = (int) (y + 128);
        if (xp < 0 || yp < 0 || xp >= IMAGE_SIZE || yp >= IMAGE_SIZE) return false;

        return (pixels[yp << 8 | xp] >>> 24) > 128;
    }

    public Entity getEntityAtMouse(double x, double y, TargetFilter filter) {
        x *= 0.5;
        y *= -1;
        double sin = Math.sin(rot);
        double cos = Math.cos(rot);
        double xp = x * cos + y * sin;
        double yp = x * sin - y * cos;

        return getEntityAt(xp, yp, 8, filter);
    }

    public boolean canPlaceHouse(double x, double y, HouseType type) {
        if (resources.canAfford(type)) {
            x *= 0.5;
            y *= -1;
            double xp = rotateX(x, y);
            double yp = rotateY(x, y);

            House house = new House(xp, yp, type);
            if (isFree(house.x, house.y, house.r)) {
                return true;
            }
        }

        return false;
    }

    public void placeHouse(double x, double y, HouseType type) {
        if (resources.canAfford(type)) {
            x *= 0.5;
            y *= -1;
            double xp = rotateX(x, y);
            double yp = rotateY(x, y);

            House house = new House(xp, yp, type);
            if (isFree(house.x, house.y, house.r)) {
                Sounds.play(new Sound.Plant());
                addEntity(house);
                resources.charge(type);
            }
        }
    }

    private double rotateX(double x, double y) {
        return x * Math.cos(rot) + y * Math.sin(rot);
    }

    private double rotateY(double x, double y) {
        return x * Math.sin(rot) - y * Math.cos(rot);
    }

    public void win() {
        tower.win();
    }

    public BufferedImage getImage() {
        return image;
    }

    public int[] getPixels() {
        return pixels;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public Random getRandom() {
        return random;
    }

    public Resources getResources() {
        return resources;
    }

    public double getRot() {
        return rot;
    }

    public void setRot(double rot) {
        this.rot = rot;
    }

    public int getPopulation() {
        return population;
    }

    public int getPopulationCap() {
        return populationCap;
    }

    public int getMonsterPopulation() {
        return monsterPopulation;
    }

    public int getWarriorPopulation() {
        return warriorPopulation;
    }

    public void setWarriorPopulation(int warriorPopulation) {
        this.warriorPopulation = warriorPopulation;
    }

    public int getWarriorPopulationCap() {
        return warriorPopulationCap;
    }

    public void increasePopulation() {
        population++;
    }

    public void decreasePopulation() {
        population--;
    }

    public void decreaseWarriorPopulation() {
        warriorPopulation--;
    }

    public void increaseMonsterPopulation() {
        monsterPopulation++;
    }

    public void decreaseMonsterPopulation() {
        monsterPopulation--;
    }

    public void decreasePopulationCap(int amount) {
        populationCap -= amount;
    }

    public void decreaseWarriorPopulationCap(int amount) {
        warriorPopulationCap -= amount;
    }

    public void increasePopulationCap(int amount) {
        populationCap += amount;
    }

    public void increaseWarriorPopulationCap(int amount) {
        warriorPopulationCap += amount;
    }
}