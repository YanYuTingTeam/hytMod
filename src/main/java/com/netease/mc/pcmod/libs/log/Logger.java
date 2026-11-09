package com.netease.mc.pcmod.libs.log;

import com.netease.mc.pcmod.libs.util.MinecraftUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.AbstractLogger;

public class Logger {
    private static MessageFactory MESSAGE_FACTORY = null;
    private static Class<?> chatComponentClassCache;
    private static boolean chatComponentMiss;
    private static Class<?> chatComponentTypeCache;
    private static boolean chatComponentTypeMiss;

    public static String getFormatMessage(String mes, Object ... objects) {
        if (MESSAGE_FACTORY == null) {
            try {
                MESSAGE_FACTORY = (MessageFactory)AbstractLogger.DEFAULT_MESSAGE_FACTORY_CLASS.newInstance();
            }
            catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
        return MESSAGE_FACTORY.newMessage(mes, objects).getFormattedMessage();
    }

    public static final void info(String mes, Object ... objects) {
        String text = getFormatMessage(mes, objects);
        try {
            FMLLog.info(text);
        } catch (Throwable ignored) {
            System.out.println(text);
        }
    }

    private static Class<?> getChatComponentClass() {
        if (chatComponentClassCache != null) {
            return chatComponentClassCache;
        }
        if (chatComponentMiss) {
            return null;
        }
        try {
            chatComponentClassCache = Class.forName("net.minecraft.util.text.TextComponentString");
        } catch (Throwable ignored) {
            try {
                chatComponentClassCache = Class.forName("net.minecraft.util.ChatComponentText");
            } catch (Throwable ignored2) {
                chatComponentMiss = true;
                return null;
            }
        }
        return chatComponentClassCache;
    }

    private static Class<?> getChatComponentType() {
        if (chatComponentTypeCache != null) {
            return chatComponentTypeCache;
        }
        if (chatComponentTypeMiss) {
            return null;
        }
        try {
            chatComponentTypeCache = Class.forName("net.minecraft.util.text.ITextComponent");
        } catch (Throwable ignored) {
            try {
                chatComponentTypeCache = Class.forName("net.minecraft.util.IChatComponent");
            } catch (Throwable ignored2) {
                chatComponentTypeMiss = true;
                return null;
            }
        }
        return chatComponentTypeCache;
    }

    public static void infoPlayer(String mes, Object ... objects) {
        EntityPlayer player = MinecraftUtil.getPlayer();
        if (player != null) {
            String formatMessage = "\u00a7c[\u00a7fNetEase\u00a7c] \u00a7f" + Logger.getFormatMessage(mes, objects);
            Class<?> clazz = getChatComponentClass();
            Class<?> type = getChatComponentType();
            if (clazz == null || type == null) {
                return;
            }
            try {
                Object component = clazz.getConstructor(String.class).newInstance(formatMessage);
                player.getClass().getMethod("sendMessage", type).invoke(player, component);
            } catch (Throwable ignored) {
            }
        }
    }

    public static final void error(String mes, Object ... objects) {
        String text = getFormatMessage(mes, objects);
        try {
            FMLLog.severe(text);
        } catch (Throwable ignored) {
            System.err.println(text);
        }
    }
}

