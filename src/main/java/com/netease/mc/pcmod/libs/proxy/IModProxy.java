package com.netease.mc.pcmod.libs.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public interface IModProxy {
    default public void onFMLPreInit(FMLPreInitializationEvent event) {
    }

    default public void onFMLInit(FMLInitializationEvent event) {
    }

    default public void onFMLPostInit(FMLPostInitializationEvent event) {
    }
}

