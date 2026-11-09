package com.netease.mc.pcmod.libs.util;

import com.netease.mc.pcmod.libs.log.Logger;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ItemRendererHelper {
    private static final String ITEM_RENDERER_EQUIPPED_PROGRESS_MAIN_HAND = "field_187469_f";
    private static final String ITEM_RENDERER_PREV_EQUIPPED_PROGRESS_MAIN_HAND = "field_187470_g";
    private static final String ITEM_RENDERER_EQUIPPED_PROGRESS_OFF_HAND = "field_187471_h";
    private static final String ITEM_RENDERER_PREV_EQUIPPED_PROGRESS_OFF_HAND = "field_187472_i";
    private static final String ENTITY_LIVING_BASE_TICKS_SINCE_LAST_SWING = "field_184617_aD";
    private static final String ENTITY_PLAYER_FOOD_STATS = "field_71100_bB";
    private static final String FOOD_STATS_FOOD_LEVEL = "field_75127_a";
    private static final String FOOD_STATS_FOOD_SATURATION_LEVEL = "field_75125_b";
    private static final String FOOD_STATS_FOOD_EXHAUSTION_LEVEL = "field_75126_c";
    private static final String FOOD_STATS_FOOD_TIMER = "field_75123_d";

    public static float getEquippedProgressMainHand(ItemRenderer instance) {
        return ((Float)ItemRendererHelper.getPrivateValue(ItemRenderer.class, instance, ITEM_RENDERER_EQUIPPED_PROGRESS_MAIN_HAND)).floatValue();
    }

    public static float getPrevEquippedProgressMainHand(ItemRenderer instance) {
        return ((Float)ItemRendererHelper.getPrivateValue(ItemRenderer.class, instance, ITEM_RENDERER_PREV_EQUIPPED_PROGRESS_MAIN_HAND)).floatValue();
    }

    public static float getEquippedProgressOffHand(ItemRenderer instance) {
        return ((Float)ItemRendererHelper.getPrivateValue(ItemRenderer.class, instance, ITEM_RENDERER_EQUIPPED_PROGRESS_OFF_HAND)).floatValue();
    }

    public static float getPrevEquippedProgressOffHand(ItemRenderer instance) {
        return ((Float)ItemRendererHelper.getPrivateValue(ItemRenderer.class, instance, ITEM_RENDERER_PREV_EQUIPPED_PROGRESS_OFF_HAND)).floatValue();
    }

    public static FoodStats getFoodStats(EntityPlayer instance) {
        return (FoodStats)ItemRendererHelper.getPrivateValue(EntityPlayer.class, instance, ENTITY_PLAYER_FOOD_STATS);
    }

    public static int getFoodLevel(FoodStats instance) {
        return (Integer)ItemRendererHelper.getPrivateValue(FoodStats.class, instance, FOOD_STATS_FOOD_LEVEL);
    }

    public static float getFoodSaturationLevel(FoodStats instance) {
        return ((Float)ItemRendererHelper.getPrivateValue(FoodStats.class, instance, FOOD_STATS_FOOD_SATURATION_LEVEL)).floatValue();
    }

    public static float getFoodExhaustionLevel(FoodStats instance) {
        return ((Float)ItemRendererHelper.getPrivateValue(FoodStats.class, instance, FOOD_STATS_FOOD_EXHAUSTION_LEVEL)).floatValue();
    }

    public static int getFoodTimer(FoodStats instance) {
        return (Integer)ItemRendererHelper.getPrivateValue(FoodStats.class, instance, FOOD_STATS_FOOD_TIMER);
    }

    public static void setEquippedProgressMainHand(ItemRenderer instance, float value) {
        ItemRendererHelper.setPrivateValue(ItemRenderer.class, instance, Float.valueOf(value), ITEM_RENDERER_EQUIPPED_PROGRESS_MAIN_HAND);
    }

    public static void setPrevEquippedProgressMainHand(ItemRenderer instance, float value) {
        ItemRendererHelper.setPrivateValue(ItemRenderer.class, instance, Float.valueOf(value), ITEM_RENDERER_PREV_EQUIPPED_PROGRESS_MAIN_HAND);
    }

    public static void setEquippedProgressOffHand(ItemRenderer instance, float value) {
        ItemRendererHelper.setPrivateValue(ItemRenderer.class, instance, Float.valueOf(value), ITEM_RENDERER_EQUIPPED_PROGRESS_OFF_HAND);
    }

    public static void setPrevEquippedProgressOffHand(ItemRenderer instance, float value) {
        ItemRendererHelper.setPrivateValue(ItemRenderer.class, instance, Float.valueOf(value), ITEM_RENDERER_PREV_EQUIPPED_PROGRESS_OFF_HAND);
    }

    public static void setTicksSinceLastSwing(EntityLivingBase instance, int value) {
        ItemRendererHelper.setPrivateValue(EntityLivingBase.class, instance, value, ENTITY_LIVING_BASE_TICKS_SINCE_LAST_SWING);
    }

    public static void setFoodStats(EntityPlayer instance, FoodStats value) {
        ItemRendererHelper.setPrivateValue(EntityPlayer.class, instance, value, ENTITY_PLAYER_FOOD_STATS);
    }

    public static void setFoodLevel(FoodStats instance, int value) {
        ItemRendererHelper.setPrivateValue(FoodStats.class, instance, value, FOOD_STATS_FOOD_LEVEL);
    }

    public static void setFoodSaturationLevel(FoodStats instance, float value) {
        ItemRendererHelper.setPrivateValue(FoodStats.class, instance, Float.valueOf(value), FOOD_STATS_FOOD_SATURATION_LEVEL);
    }

    public static void setFoodExhaustionLevel(FoodStats instance, float value) {
        ItemRendererHelper.setPrivateValue(FoodStats.class, instance, Float.valueOf(value), FOOD_STATS_FOOD_EXHAUSTION_LEVEL);
    }

    public static void setFoodTimer(FoodStats instance, int value) {
        ItemRendererHelper.setPrivateValue(FoodStats.class, instance, value, FOOD_STATS_FOOD_TIMER);
    }

    private static <T> void setPrivateValue(Class<T> clazz, T instance, Object value, String name) {
        try {
            ObfuscationReflectionHelper.setPrivateValue(clazz, instance, (Object)value, (String)name);
        }
        catch (Exception e) {
            Logger.error("Setting field \"" + name + "\" failed", e);
        }
    }

    private static <T> Object getPrivateValue(Class<T> clazz, T instance, String name) {
        try {
            return ObfuscationReflectionHelper.getPrivateValue(clazz, instance, (String)name);
        }
        catch (Exception e) {
            Logger.error("Getting field \"" + name + "\" failed", e);
            return null;
        }
    }
}

