package com.hytmc.mod.ess;

import com.hytmc.mod.ess.effect.DamageShower;
import com.hytmc.mod.ess.gui.TabNpcFix;
import com.hytmc.mod.ess.option.ModifyDefaultSetting;
import com.hytmc.mod.ess.player.animation.ModifyHandActive;
import com.hytmc.mod.ess.player.sprint.AutoSprint;
import com.netease.mc.pcmod.libs.proxy.IModProxy;
import com.netease.mc.pcmod.libs.proxy.ModProxy;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid="ess", name="Ess")
public class Ess
extends ModProxy {
    public static final String ESS_MODID = "ess";
    public static final String ESS_NAME = "Ess";
    private final List<IModProxy> modProxyList = new ArrayList<IModProxy>();

    public Ess() {
        this.registerModProxy(new AutoSprint());
        this.registerModProxy(new ModifyDefaultSetting());
        this.registerModProxy(new ModifyHandActive());
        this.registerModProxy(new DamageShower());
        this.registerModProxy(new TabNpcFix());
    }

    private void registerModProxy(IModProxy modProxy) {
        this.modProxyList.add(modProxy);
    }

    @Override
    @Mod.EventHandler
    public void onFMLPreInit(FMLPreInitializationEvent event) {
        super.onFMLPreInit(event);
        this.modProxyList.forEach(iModProxy -> iModProxy.onFMLPreInit(event));
    }

    @Override
    @Mod.EventHandler
    public void onFMLInit(FMLInitializationEvent event) {
        super.onFMLInit(event);
        this.modProxyList.forEach(iModProxy -> iModProxy.onFMLInit(event));
    }

    @Override
    @Mod.EventHandler
    public void onFMLPostInit(FMLPostInitializationEvent event) {
        super.onFMLPostInit(event);
        this.modProxyList.forEach(iModProxy -> iModProxy.onFMLPostInit(event));
    }
}

