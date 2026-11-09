package com.mchyt.sidebarmod.gui.screen;

import com.mchyt.sidebarmod.SidebarMod;
import com.mchyt.sidebarmod.gui.GuiSidebar;
import java.io.IOException;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.ScoreObjective;

public class GuiScreenSidebar
extends GuiScreen {
    protected SidebarMod mod;
    protected GuiSidebar sidebar;
    private boolean dragging;
    private int lastX;
    private int lastY;

    public GuiScreenSidebar(SidebarMod mod) {
        this.mod = mod;
        this.sidebar = mod.getSidebarGui();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScoreObjective scoreObjective;
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (this.mc.player != null && (scoreObjective = this.mc.player.getWorldScoreboard().getObjectiveInDisplaySlot(1)) != null) {
            this.sidebar.drawSidebar(scoreObjective, new ScaledResolution(this.mc));
        }
        if (this.dragging) {
            GuiSidebar sidebar = this.sidebar;
            sidebar.offsetX += mouseX - this.lastX;
            GuiSidebar sidebar2 = this.sidebar;
            sidebar2.offsetY += mouseY - this.lastY;
        }
        this.lastX = mouseX;
        this.lastY = mouseY;
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.sidebar.contains(mouseX, mouseY)) {
            this.dragging = true;
        }
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.dragging = false;
    }
}

