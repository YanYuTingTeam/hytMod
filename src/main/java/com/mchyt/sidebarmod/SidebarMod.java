package com.mchyt.sidebarmod;

import com.mchyt.sidebarmod.gui.GuiSidebar;
import com.mchyt.sidebarmod.gui.GuiSidebarIngame;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mod(modid="sidebarmod", name="SidebarMod", version="1.0", acceptedMinecraftVersions="[1.8.9,1.12.2]")
public class SidebarMod {
    public static final String MODID = "sidebarmod";
    public static final String NAME = "SidebarMod";
    public static final String VERSION = "1.0";
    private GuiSidebar guiSidebar;
    private Minecraft mc = Minecraft.getMinecraft();
    private GuiSidebarIngame ingame;
    private static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("DIRT BLOCK >> " + getDirtRegistryName());
        MinecraftForge.EVENT_BUS.register((Object)this);
        this.guiSidebar = new GuiSidebar();
        this.ingame = new GuiSidebarIngame(this, this.mc);
    }

    public static String getDirtRegistryName() {
        try {
            Method method = Blocks.DIRT.getClass().getMethod("getRegistryName");
            return String.valueOf(method.invoke(Blocks.DIRT));
        } catch (Throwable ignore) {
        }
        try {
            Class<?> blockClass = Class.forName("net.minecraft.block.Block");
            Field registryField = blockClass.getField("blockRegistry");
            Object registry = registryField.get(null);
            Method nameMethod = registry.getClass().getMethod("getNameForObject", blockClass);
            return String.valueOf(nameMethod.invoke(registry, Blocks.DIRT));
        } catch (Throwable ignore) {
        }
        return "minecraft:dirt";
    }

    public GuiSidebar getSidebarGui() {
        return this.guiSidebar;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!(this.mc.ingameGUI instanceof GuiSidebarIngame)) {
            this.mc.ingameGUI = this.ingame;
        }
    }
}

