package com.hytmc.mod.ess.player.animation;

import com.netease.mc.pcmod.libs.proxy.ModProxy;
import com.netease.mc.pcmod.libs.util.Compat;
import com.netease.mc.pcmod.libs.util.MinecraftUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(value=Side.CLIENT)
public class ModifyHandActive
extends ModProxy {
    private final Minecraft mc = MinecraftUtil.getMinecraft();

    public static boolean getCanStackBlock(ItemStack stack) {
        return stack.getItem() instanceof ItemSword;
    }

    @Override
    public void onFMLInit(FMLInitializationEvent event) {
        super.onFMLInit(event);
        if (!Compat.isNewApi()) {
            return;
        }
        MinecraftForge.EVENT_BUS.register((Object)new ModernHandler(this));
    }

    @SideOnly(value=Side.CLIENT)
    public static class ModernHandler {
        private final ModifyHandActive outer;

        ModernHandler(ModifyHandActive outer) {
            this.outer = outer;
        }

        @SubscribeEvent
        public void onRenderLiving(RenderLivingEvent.Pre<AbstractClientPlayer> evt) {
            if (!(evt.getEntity() instanceof AbstractClientPlayer)) {
                return;
            }
            AbstractClientPlayer player = (AbstractClientPlayer)evt.getEntity();
            if (!player.isHandActive() || !ModifyHandActive.getCanStackBlock(player.getActiveItemStack())) {
                return;
            }
            ModelPlayer model = (ModelPlayer)evt.getRenderer().getMainModel();
            boolean offHandLeft = player.getActiveHand() == EnumHand.OFF_HAND && player.getPrimaryHand() == EnumHandSide.RIGHT;
            boolean mainHandLeft = player.getActiveHand() == EnumHand.MAIN_HAND && player.getPrimaryHand() == EnumHandSide.LEFT;
            if (offHandLeft || mainHandLeft) {
                if (model.leftArmPose == ModelBiped.ArmPose.ITEM) {
                    model.leftArmPose = ModelBiped.ArmPose.BLOCK;
                }
            } else if (model.rightArmPose == ModelBiped.ArmPose.ITEM) {
                model.rightArmPose = ModelBiped.ArmPose.BLOCK;
            }
        }

        @SubscribeEvent
        public void onRenderHand(RenderSpecificHandEvent evt) {
            EntityPlayer player = MinecraftUtil.getPlayer();
            if (player == null || !player.isHandActive() || player.getActiveHand() != evt.getHand()) {
                return;
            }
            ItemStack stack = evt.getItemStack();
            if (!ModifyHandActive.getCanStackBlock(stack)) {
                return;
            }
            GlStateManager.pushMatrix();
            boolean rightHand = (evt.getHand() == EnumHand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite()) == EnumHandSide.RIGHT;
            this.transformSideFirstPerson(rightHand ? 1.0f : -1.0f, evt.getEquipProgress());
            this.outer.mc.getItemRenderer().renderItemSide((EntityLivingBase)player, stack, rightHand ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !rightHand);
            GlStateManager.popMatrix();
            evt.setCanceled(true);
        }

        private void transformSideFirstPerson(float side, float equippedProg) {
            GlStateManager.translate(side * 0.56f, -0.52f + equippedProg * -0.6f, -0.72f);
            GlStateManager.translate(side * -0.14142136f, 0.08f, 0.14142136f);
            GlStateManager.rotate(-102.25f, 1.0f, 0.0f, 0.0f);
            GlStateManager.rotate(side * 13.365f, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(side * 78.05f, 0.0f, 0.0f, 1.0f);
        }

        @SubscribeEvent
        public void onRightClickItem(PlayerInteractEvent.RightClickItem evt) {
            EntityPlayer player = evt.getEntityPlayer();
            if (!ModifyHandActive.getCanStackBlock(evt.getItemStack())) {
                return;
            }
            EnumAction action = player.getHeldItemOffhand().getItemUseAction();
            if (action == EnumAction.NONE || action == EnumAction.EAT && !player.canEat(false)) {
                player.setActiveHand(evt.getHand());
                evt.setCancellationResult(EnumActionResult.SUCCESS);
                evt.setCanceled(true);
            }
        }

        @SubscribeEvent
        public void onItemUseStart(LivingEntityUseItemEvent.Start evt) {
            if (evt.getEntityLiving() instanceof EntityPlayer && ModifyHandActive.getCanStackBlock(evt.getItem())) {
                int swordUseDuration = 72000;
                evt.setDuration(swordUseDuration);
            }
        }
    }
}
