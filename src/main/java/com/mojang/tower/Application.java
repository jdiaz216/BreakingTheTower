package com.mojang.tower;

import com.mojang.tower.ui.TowerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {

        LOGGER.info("Starting the game");

        final TowerComponent tower = new TowerComponent(512, 320);

        Frame frame = new Frame("Breaking the Tower");
        frame.add(tower);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                tower.stop();
                System.exit(0);
            }
        });
        frame.setVisible(true);
        tower.start();
    }
}
