package com.netease.mc.pcmod.libs.proxy;

import com.netease.mc.pcmod.libs.log.Logger;
import com.netease.mc.pcmod.libs.proxy.IModProxy;
import java.io.File;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModProxy
implements IModProxy {
    protected File modJar;
    protected File configDir;

    @Override
    public void onFMLPreInit(FMLPreInitializationEvent event) {
        IModProxy.super.onFMLPreInit(event);
        this.modJar = event.getSourceFile();
        this.configDir = event.getModConfigurationDirectory();
        Logger.info("netease {}(mod) is already loaded.", event.getModMetadata().modId);
    }

    @Override
    public void onFMLInit(FMLInitializationEvent event) {
        IModProxy.super.onFMLInit(event);
    }

    @Override
    public void onFMLPostInit(FMLPostInitializationEvent event) {
        IModProxy.super.onFMLPostInit(event);
    }

    public File getModJar() {
        return this.modJar;
    }

    public File getConfigDir() {
        return this.configDir;
    }
}

