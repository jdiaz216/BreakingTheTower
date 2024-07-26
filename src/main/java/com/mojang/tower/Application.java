package com.mojang.tower;

import com.mojang.tower.ui.TowerComponent;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Application {

    public static void main(String[] args) {
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
