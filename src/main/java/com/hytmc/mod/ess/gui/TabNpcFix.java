package com.hytmc.mod.ess.gui;

import com.google.common.collect.Lists;
import com.netease.mc.pcmod.libs.proxy.IModProxy;
import com.netease.mc.pcmod.libs.util.Compat;
import com.netease.mc.pcmod.libs.util.MinecraftUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TabNpcFix
implements IModProxy {
    private static Object PLAYER_MODEL_FLAG;
    private static Method getDataWatcherMethod;
    private static Method eventTypeMethod;
    private static Field eventTypeField;
    private static boolean eventTypeResolved;

    @Override
    public void onFMLPostInit(FMLPostInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register((Object)this);
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Pre event) {
        if (!this.isPlayerList(event)) {
            return;
        }
        NetHandlerPlayClient connection = MinecraftUtil.getMinecraft().getConnection();
        if (connection == null) {
            return;
        }
        WorldClient world = Minecraft.getMinecraft().world;
        if (world == null) {
            return;
        }
        ArrayList dels = Lists.newArrayList();
        for (NetworkPlayerInfo networkPlayerInfo : connection.getPlayerInfoMap()) {
            EntityPlayer playerEntityByName = world.getPlayerEntityByName(networkPlayerInfo.getGameProfile().getName());
            if (playerEntityByName == null || !this.isNPC(playerEntityByName)) continue;
            dels.add(networkPlayerInfo);
        }
        connection.getPlayerInfoMap().removeAll(dels);
    }

    private boolean isPlayerList(RenderGameOverlayEvent.Pre event) {
        Object type = this.getEventType(event);
        if (type == null) {
            return false;
        }
        return RenderGameOverlayEvent.ElementType.PLAYER_LIST.equals(type);
    }

    private Object getEventType(RenderGameOverlayEvent.Pre event) {
        if (!eventTypeResolved) {
            eventTypeMethod = Compat.findMethod(RenderGameOverlayEvent.Pre.class, "func_147683_M", new Class<?>[0]);
            if (eventTypeMethod == null) {
                eventTypeMethod = Compat.findMethod(RenderGameOverlayEvent.Pre.class, "getType", new Class<?>[0]);
            }
            eventTypeField = Compat.findField(RenderGameOverlayEvent.Pre.class, "type");
            eventTypeResolved = true;
        }
        if (eventTypeMethod != null) {
            try {
                Object type = eventTypeMethod.invoke(event);
                if (type != null) {
                    return type;
                }
            } catch (Throwable ignored) {
            }
        }
        if (eventTypeField != null) {
            try {
                return eventTypeField.get(event);
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private boolean isNPC(EntityPlayer entityPlayer) {
        if (!entityPlayer.getAlwaysRenderNameTagForRender()) {
            return false;
        }
        if (!Compat.isNewApi()) {
            return false;
        }
        if (this.isLeftHand(entityPlayer)) {
            return false;
        }
        return this.getPlayerModelFlag(entityPlayer) == -1;
    }

    private boolean isLeftHand(EntityPlayer entityPlayer) {
        try {
            Class<?> handSideClass = Class.forName("net.minecraft.util.EnumHandSide");
            Method method = Compat.findMethod(EntityPlayer.class, "func_184591_cq", new Class<?>[0]);
            if (method == null) {
                method = Compat.findMethod(EntityPlayer.class, "getPrimaryHand", new Class<?>[0]);
            }
            if (method == null) {
                return false;
            }
            Object handSide = method.invoke(entityPlayer);
            if (handSide == null) {
                return false;
            }
            Object right = handSideClass.getField("RIGHT").get(null);
            return right == null ? false : !right.equals(handSide);
        } catch (Throwable e) {
            return false;
        }
    }

    private byte getPlayerModelFlag(EntityPlayer entityPlayer) {
        if (PLAYER_MODEL_FLAG == null) {
            Field PLAYER_MODEL_FLAG_F = Compat.findField(EntityPlayer.class, "field_184827_bp");
            if (PLAYER_MODEL_FLAG_F == null) {
                return 99;
            }
            try {
                PLAYER_MODEL_FLAG = PLAYER_MODEL_FLAG_F.get(EntityPlayer.class);
            } catch (Throwable e) {
                return 99;
            }
            if (PLAYER_MODEL_FLAG == null) {
                return 99;
            }
        }
        if (getDataWatcherMethod == null) {
            getDataWatcherMethod = Compat.findMethod(EntityPlayer.class, "func_184212_Q", new Class<?>[0]);
            if (getDataWatcherMethod == null) {
                getDataWatcherMethod = Compat.findMethod(EntityPlayer.class, "getDataWatcher", new Class<?>[0]);
            }
            if (getDataWatcherMethod == null) {
                return 99;
            }
        }
        try {
            Object watcher = getDataWatcherMethod.invoke(entityPlayer);
            if (watcher == null) {
                return 99;
            }
            Method get = Compat.findMethod(watcher.getClass(), "func_187225_a", PLAYER_MODEL_FLAG.getClass());
            if (get == null) {
                get = Compat.findMethod(watcher.getClass(), "get", PLAYER_MODEL_FLAG.getClass());
            }
            if (get == null) {
                return 99;
            }
            Object value = get.invoke(watcher, PLAYER_MODEL_FLAG);
            if (value instanceof Byte) {
                return (Byte)value;
            }
            if (value instanceof Number) {
                return ((Number)value).byteValue();
            }
            return 99;
        } catch (Throwable e) {
            return 99;
        }
    }
}
