package com.hytmc.mod.ess.player.animation;

import com.netease.mc.pcmod.libs.proxy.ModProxy;
import com.netease.mc.pcmod.libs.util.Compat;
import com.netease.mc.pcmod.libs.util.MinecraftUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;




















@SideOnly(value=Side.CLIENT)
public class ModifyHandActive
extends ModProxy {
    private final Minecraft mc = MinecraftUtil.getMinecraft();

    @Override
    public void onFMLInit(FMLInitializationEvent event) {
        super.onFMLInit(event);
        if (!Compat.isNewApi()) {
            return;
        }
        MinecraftForge.EVENT_BUS.register((Object)this);
    }
































































}
