package com.hytmc.mod.ess.player.sprint;

import com.netease.mc.pcmod.libs.log.Logger;
import com.netease.mc.pcmod.libs.proxy.IModProxy;
import com.netease.mc.pcmod.libs.time.TimeMaster;
import com.netease.mc.pcmod.libs.util.Compat;
import com.netease.mc.pcmod.libs.util.MinecraftUtil;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class AutoSprint
implements IModProxy {
    public static Object sprintKey;
    public static final Minecraft mc = MinecraftUtil.getMinecraft();
    private long sprintHelperRenderTime = -1L;
    private final List<String> sprintHelperTips = Arrays.asList("\u00a7a\u672c\u63d0\u793a\u4f1a\u663e\u793a(%second%)\u79d2", "\u00a771.\u6309\u4e0b %key% \u952e\u53ef\u5207\u6362\u5f00\u542f\u81ea\u52a8\u75be\u8dd1", "\u00a772.\u957f\u6309 %key% 1.5\u79d2\u53ef\u4ee5\u5f00\u5173\u63d0\u793a\u6e32\u67d3", "\u00a773.\u9f20\u6807\u53ef\u4ee5\u62d6\u52a8\u81ea\u52a8\u75be\u8dd1\u6587\u5b57\u663e\u793a\u4f4d\u7f6e");
    private final String enabletext = "\u81ea\u52a8\u75be\u8dd1(%key%)";
    private final String disabletext = "\u81ea\u52a8\u75be\u8dd1(%key%)";
    private int posX = 0;
    private int posY = 0;
    private boolean enabled = true;
    private boolean showText = true;
    private boolean lastKeyPress = false;
    private long keyPressTime = -1L;
    private int clickX = -1;
    private int clickY = -1;
    private int lastclickX = -1;
    private int lastclickY = -1;

    @Override
    public void onFMLPreInit(FMLPreInitializationEvent event) {
        sprintKey = createSprintKey();
        Compat.registerKeyBinding(sprintKey);
        MinecraftForge.EVENT_BUS.register((Object)this);
        Logger.info("auto sprint is already loaded.", new Object[0]);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        TimeMaster.refresh();
        if (AutoSprint.mc.world == null) {
            return;
        }
        boolean changeKeyPress = false;
        if (this.lastKeyPress && this.keyPressTime != -1L && TimeMaster.time() - this.keyPressTime > 1500L) {
            this.showText = !this.showText;
            changeKeyPress = true;
        }
        int sprintKey = Compat.getKeyCode(AutoSprint.mc.gameSettings.keyBindSprint);
        if (Compat.isKeyDown(AutoSprint.sprintKey)) {
            if (!this.lastKeyPress) {
                this.keyPressTime = TimeMaster.time();
            }
            this.lastKeyPress = true;
        } else {
            this.lastKeyPress = false;
            if (TimeMaster.time() - this.keyPressTime < 1500L) {
                if (this.sprintHelperRenderTime == -1L) {
                    this.sprintHelperRenderTime = TimeMaster.time() + 6000L;
                }
                boolean bl = this.enabled = !this.enabled;
                if (!this.enabled && sprintKey > 0) {
                    Compat.setKeyBindState(sprintKey, Keyboard.isKeyDown(sprintKey));
                }
            }
            this.keyPressTime = -1L;
        }
        if (this.enabled) {
            Compat.setKeyBindState(sprintKey, true);
        }
        if (changeKeyPress) {
            this.keyPressTime = -1L;
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) throws IOException {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        TimeMaster.refresh();
        if (AutoSprint.mc.currentScreen != null && !(AutoSprint.mc.currentScreen instanceof GuiChat) || AutoSprint.mc.world == null || !Minecraft.isGuiEnabled()) {
            return;
        }
        int renderWidth = MinecraftUtil.getRenderWidth();
        int renderHeight = MinecraftUtil.getRenderHeight();
        String renderText = (this.enabled ? "\u81ea\u52a8\u75be\u8dd1(%key%)" : "\u81ea\u52a8\u75be\u8dd1(%key%)").replace("%key%", Compat.getDisplayName(sprintKey));
        FontRenderer fontRenderer = AutoSprint.mc.fontRenderer;
        int stringWidth = fontRenderer.getStringWidth(renderText);
        int x = Mouse.getEventX() * renderWidth / AutoSprint.mc.displayWidth;
        int y = renderHeight - Mouse.getEventY() * renderHeight / AutoSprint.mc.displayHeight - 1;
        if (Mouse.isButtonDown((int)0)) {
            if (this.clickX == -1) {
                if (x > this.posX + 1 && x < this.posX + stringWidth + 10 && (float)y > (float)this.posY + (float)renderHeight / 2.0f - 8.0f && (float)y < (float)this.posY + (float)renderHeight / 2.0f - 4.0f + 13.0f) {
                    if (this.lastclickX != -1 && this.lastclickY != -1) {
                        this.clickX = this.lastclickX;
                        this.clickY = this.lastclickY;
                    } else {
                        this.clickX = x;
                        this.clickY = y;
                    }
                }
            } else {
                this.posX = x - this.clickX;
                this.posY = y - this.clickY;
            }
        } else {
            if (this.clickX != -1 && this.clickY != -1) {
                this.lastclickX = this.clickX;
                this.lastclickY = this.clickY;
            }
            this.clickX = -1;
            this.clickY = -1;
        }
        if (this.showText) {
            if (this.enabled) {
                Compat.drawStringInt(fontRenderer, renderText, this.posX + 5, this.posY + renderHeight / 2 - 4, this.getTextColor());
            } else {
                Compat.drawStringInt(fontRenderer, renderText, this.posX + 5, this.posY + renderHeight / 2 - 4, -1352046231);
            }
            if (this.sprintHelperRenderTime != -1L && this.sprintHelperRenderTime > TimeMaster.time()) {
                ArrayList<String> strings = new ArrayList<String>(this.sprintHelperTips);
                for (int i = 0; i < strings.size(); ++i) {
                    strings.set(i, strings.get(i).replace("%key%", Compat.getDisplayName(sprintKey)).replace("%second%", (this.sprintHelperRenderTime - TimeMaster.time()) / 1000L + 1L + ""));
                }
                GuiUtils.drawHoveringText(strings, (int)this.posX, (int)((int)((float)this.posY + (float)renderHeight / 2.0f) + 24), (int)renderWidth, (int)renderHeight, (int)renderWidth, (FontRenderer)fontRenderer);
            }
        }
    }

    private Object createSprintKey() {
        Class clz = Compat.getKeyBindingClass();
        if (clz == null) {
            return null;
        }
        String name = "\u81ea\u52a8\u75be\u8dd1";
        String cat = "key.categories.movement";
        try {
            return clz.getConstructor(String.class, int.class, String.class).newInstance(name, 23, cat);
        } catch (Throwable ignored) {
        }
        try {
            return clz.getDeclaredConstructor(String.class, int.class, String.class).newInstance(name, 23, cat);
        } catch (Throwable ignored) {
        }
        try {
            return clz.getConstructor(String.class, String.class, int.class).newInstance(name, cat, 23);
        } catch (Throwable ignored) {
        }
        try {
            return clz.getDeclaredConstructor(String.class, String.class, int.class).newInstance(name, cat, 23);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private int getColor(int rgb) {
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int b = rgb & 0xFF;
        return r << 16 | g << 8 | b;
    }

    public int getTextColor() {
        int rgb = Color.HSBtoRGB((float)(TimeMaster.time() % 1000L) / 1000.0f, 0.8f, 0.8f);
        return this.getColor(rgb);
    }
}

