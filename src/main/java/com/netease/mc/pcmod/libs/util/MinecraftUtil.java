package com.netease.mc.pcmod.libs.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class MinecraftUtil {
    private static Minecraft minecraft;
    private static TextureManager textureManager;
    private static RenderManager renderManager;

    public static Minecraft getMinecraft() {
        if (minecraft == null) {
            minecraft = Minecraft.getMinecraft();
        }
        return minecraft;
    }

    public static int getRenderWidth() {
        if (MinecraftUtil.getMinecraft().currentScreen != null) {
            return MinecraftUtil.getMinecraft().currentScreen.width;
        }
        return new ScaledResolution(MinecraftUtil.getMinecraft()).getScaledWidth();
    }

    public static int getRenderHeight() {
        if (MinecraftUtil.getMinecraft().currentScreen != null) {
            return MinecraftUtil.getMinecraft().currentScreen.height;
        }
        return new ScaledResolution(MinecraftUtil.getMinecraft()).getScaledHeight();
    }

    public static EntityPlayer getPlayer() {
        return MinecraftUtil.getMinecraft().player;
    }

    public static UUID getOfflineUUID(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }

    public static void runMinecraft(Runnable runnable) {
        MinecraftUtil.getMinecraft().addScheduledTask(runnable);
    }

    public static FontRenderer getFontRenderer() {
        return MinecraftUtil.getMinecraft().fontRenderer;
    }

    private static TextureManager getTextureManager() {
        if (textureManager == null) {
            textureManager = MinecraftUtil.getMinecraft().getTextureManager();
        }
        return textureManager;
    }

    public static void bindTexture(ResourceLocation resourceLocation) {
        MinecraftUtil.getTextureManager().bindTexture(resourceLocation);
    }

    public static File getGameDir() {
        return MinecraftUtil.getMinecraft().mcDataDir;
    }

    public static File getGameFile(String ... paths) {
        return new File(MinecraftUtil.getGameDir(), Arrays.stream(paths).map(s -> "/" + s).collect(Collectors.joining()));
    }

    public static RenderManager getRenderManger() {
        if (renderManager == null) {
            renderManager = MinecraftUtil.getMinecraft().getRenderManager();
        }
        return renderManager;
    }
}

