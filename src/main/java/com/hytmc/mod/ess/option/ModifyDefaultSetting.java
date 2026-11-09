package com.hytmc.mod.ess.option;

import com.netease.mc.pcmod.libs.log.Logger;
import com.netease.mc.pcmod.libs.proxy.IModProxy;
import com.netease.mc.pcmod.libs.util.Compat;
import com.netease.mc.pcmod.libs.util.MinecraftUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModifyDefaultSetting
implements IModProxy {
    private boolean firstJoin = true;
    private final Minecraft mc = MinecraftUtil.getMinecraft();

    @Override
    public void onFMLInit(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register((Object)this);
    }

    @SubscribeEvent
    public void onJoin(EntityJoinWorldEvent e) {
        if (!this.firstJoin) {
            return;
        }
        if (Compat.isNewApi()) {
            this.onJoinNewApi();
        } else {
            this.onJoinOldApi();
        }
        this.firstJoin = false;
    }

    private void onJoinNewApi() {
        if (this.mc.gameSettings.autoJump) {
            this.mc.gameSettings.autoJump = false;
            this.mc.gameSettings.saveOptions();
            Logger.info("save options autoJump success!", new Object[0]);
        }
        if (this.mc.gameSettings.attackIndicator != 0) {
            this.mc.gameSettings.attackIndicator = 0;
            this.mc.gameSettings.saveOptions();
            Logger.info("save options attackIndicator success!", new Object[0]);
        }









    }

    private void onJoinOldApi() {
        Field autoJump = Compat.findField(this.mc.gameSettings.getClass(), "field_189989_R", "autoJump");
        if (autoJump != null) {
            try {
                if (autoJump.getBoolean(this.mc.gameSettings)) {
                    autoJump.setBoolean(this.mc.gameSettings, false);
                    this.saveOptions();
                    Logger.info("save options autoJump success!", new Object[0]);
                }
            } catch (Throwable ignored) {
            }
        }
        Field attackIndicator = Compat.findField(this.mc.gameSettings.getClass(), "field_194557_aH", "attackIndicator");
        if (attackIndicator != null) {
            try {
                if (attackIndicator.getInt(this.mc.gameSettings) != 0) {
                    attackIndicator.setInt(this.mc.gameSettings, 0);
                    this.saveOptions();
                    Logger.info("save options attackIndicator success!", new Object[0]);
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private void saveOptions() {
        Method saveOptions = Compat.findMethod(this.mc.gameSettings.getClass(), "func_151694_c", new Class<?>[0]);
        if (saveOptions == null) {
            saveOptions = Compat.findMethod(this.mc.gameSettings.getClass(), "saveOptions", new Class<?>[0]);
        }
        if (saveOptions == null) {
            return;
        }
        try {
            saveOptions.invoke(this.mc.gameSettings);
        } catch (Throwable ignored) {
        }
    }
}
