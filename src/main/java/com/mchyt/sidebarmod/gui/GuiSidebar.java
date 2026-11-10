package com.mchyt.sidebarmod.gui;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import com.netease.mc.pcmod.libs.util.Compat;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import org.lwjgl.opengl.GL11;

public class GuiSidebar
extends Gui {
    private FontRenderer fr;
    private int sidebarX;
    private int sidebarY;
    private int sidebarWidth;
    private int sidebarHeight;
    public boolean enabled;
    public boolean shadow;
    public float scale;
    public int offsetX;
    public int offsetY;
    public int color;
    public int alpha;
    public boolean chromaEnabled;
    public int chromaSpeed;

    public GuiSidebar() {
        this.fr = Minecraft.getMinecraft().fontRenderer;
        this.enabled = true;
        this.offsetX = 0;
        this.offsetY = 0;
        this.scale = 1.0f;
        this.shadow = false;
        this.alpha = 50;
        this.chromaEnabled = false;
        this.chromaSpeed = 2;
    }

    public boolean contains(int mouseX, int mouseY) {
        float mscale = this.scale - 1.0f;
        float minX = (float)this.sidebarX - (float)this.sidebarWidth * mscale;
        float maxX = minX + (float)this.sidebarWidth * this.scale;
        float maxY = (float)this.sidebarY + (float)(this.sidebarHeight / 2) * mscale;
        float minY = maxY - (float)this.sidebarHeight * this.scale;
        return (float)mouseX > minX && (float)mouseX < maxX && (float)mouseY > minY - (float)this.fr.FONT_HEIGHT * this.scale && (float)mouseY < maxY;
    }

    public void drawSidebar(ScoreObjective sidebar, ScaledResolution res) {
        if (!this.enabled) {
            return;
        }
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        Scoreboard scoreboard = sidebar.getScoreboard();
        ArrayList<Score> scores = new ArrayList<Score>();
        this.sidebarWidth = fr.getStringWidth(sidebar.getDisplayName());
        for (Score score : scoreboard.getSortedScores(sidebar)) {
            String name = score.getPlayerName();
            if (scores.size() >= 15 || name == null || name.startsWith("#")) continue;
            ScorePlayerTeam team = scoreboard.getPlayersTeam(name);
            String s2 = "";
            String str = ScorePlayerTeam.formatPlayerName((Team)team, (String)name) + "";
            this.sidebarWidth = Math.max(this.sidebarWidth, fr.getStringWidth(str));
            scores.add(score);
        }
        this.sidebarHeight = scores.size() * fr.FONT_HEIGHT;
        this.sidebarX = res.getScaledWidth() - this.sidebarWidth - 3 + this.offsetX;
        this.sidebarY = res.getScaledHeight() / 2 + this.sidebarHeight / 3 + this.offsetY;
        int scalePointX = this.sidebarX + this.sidebarWidth;
        int scalePointY = this.sidebarY - this.sidebarHeight / 2;
        float mscale = this.scale - 1.0f;
        GL11.glTranslatef((float)((float)(-scalePointX) * mscale), (float)((float)(-scalePointY) * mscale), (float)0.0f);
        GL11.glScalef((float)this.scale, (float)this.scale, (float)1.0f);
        int index = 0;
        for (Score score2 : scores) {
            ScorePlayerTeam team2 = scoreboard.getPlayersTeam(score2.getPlayerName());
            String s3 = ScorePlayerTeam.formatPlayerName((Team)team2, (String)score2.getPlayerName());
            String s4 = "";
            int scoreX = this.sidebarX + this.sidebarWidth + 1;
            int scoreY = this.sidebarY - ++index * fr.FONT_HEIGHT;
            GuiSidebar.drawRect((int)(this.sidebarX - 2), (int)scoreY, (int)scoreX, (int)(scoreY + fr.FONT_HEIGHT), (int)this.getColor(false));
            this.drawString(s3, this.sidebarX, scoreY, 0x20FFFFFF);
            this.drawString(s4, scoreX - fr.getStringWidth(s4), scoreY, 0x20FFFFFF);
            if (index != scores.size()) continue;
            String s5 = sidebar.getDisplayName();
            GuiSidebar.drawRect((int)(this.sidebarX - 2), (int)(scoreY - fr.FONT_HEIGHT - 1), (int)scoreX, (int)(scoreY - 1), (int)this.getColor(true));
            GuiSidebar.drawRect((int)(this.sidebarX - 2), (int)(scoreY - 1), (int)scoreX, (int)scoreY, (int)this.getColor(false));
            this.drawString(s5, this.sidebarX + (this.sidebarWidth - fr.getStringWidth(s5)) / 2, scoreY - fr.FONT_HEIGHT, 0x20FFFFFF);
        }
        GL11.glScalef((float)(1.0f / this.scale), (float)(1.0f / this.scale), (float)1.0f);
        GL11.glTranslatef((float)((float)scalePointX * mscale), (float)((float)scalePointY * mscale), (float)0.0f);
    }

    private int getColor(boolean darker) {
        int rgb = this.color;
        if (this.chromaEnabled) {
            long time = 10000L / (long)this.chromaSpeed;
            rgb = Color.HSBtoRGB(System.currentTimeMillis() % time / time, 0.8f, 0.8f);
        }
        return darker ? this.getColorWithAlpha(rgb, Math.min(255, this.alpha + 10)) : this.getColorWithAlpha(rgb, this.alpha);
    }

    private int getColorWithAlpha(int rgb, int a) {
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int b = rgb & 0xFF;
        return a << 24 | r << 16 | g << 8 | b;
    }

    private void drawString(String str, int x, int y, int color) {
        Compat.drawStringWithShadow(this.fr, str, x, y, color, this.shadow);
    }
}

