/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import me.chayapak1.chomens_bot.Bot;
import org.cloudburstmc.math.vector.Vector3i;

public class ScreensharePlugin {
    private final Bot bot;
    private String[][] screen;
    private int width = 80;
    private int height = 25;
    private int fps = 20;
    private Vector3i position;
    private boolean running = false;

    public ScreensharePlugin(Bot bot) {
        this.bot = bot;
        this.screen = new String[this.width][this.height];
    }

    public void start(Vector3i pos) {
        this.position = pos;
        this.running = true;
        new Thread(() -> {
            while (this.running) {
                this.captureTick();
                try {
                    Thread.sleep(1000 / this.fps);
                }
                catch (InterruptedException interruptedException) {}
            }
        }).start();
    }

    public void stop() {
        this.running = false;
    }

    public void setScreenSize(int width, int height) {
        this.width = width;
        this.height = height;
        this.screen = new String[width][height];
    }

    public void setFPS(int fps) {
        this.fps = fps;
    }

    private void captureTick() {
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.height; ++y) {
                this.screen[x][y] = " ";
            }
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getFPS() {
        return this.fps;
    }
}

