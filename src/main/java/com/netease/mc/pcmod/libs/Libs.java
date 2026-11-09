package com.netease.mc.pcmod.libs;

import com.netease.mc.pcmod.libs.proxy.ModProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid="libs", name="Libs")
public class Libs
extends ModProxy {
    public static final String LIBS_MODID = "libs";
    public static final String LIBS_NAME = "Libs";

    @Override
    @Mod.EventHandler
    public void onFMLPreInit(FMLPreInitializationEvent event) {
        super.onFMLPreInit(event);
    }

    @Override
    @Mod.EventHandler
    public void onFMLInit(FMLInitializationEvent event) {
        super.onFMLInit(event);
    }

    @Override
    @Mod.EventHandler
    public void onFMLPostInit(FMLPostInitializationEvent event) {
        super.onFMLPostInit(event);
    }
}

