package com.hytmc.mod.ess.effect;

import com.google.common.collect.Maps;
import com.netease.mc.pcmod.libs.proxy.IModProxy;
import com.netease.mc.pcmod.libs.time.TimeMaster;
import com.netease.mc.pcmod.libs.util.MinecraftUtil;
import io.netty.util.internal.ConcurrentSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

public class DamageShower
implements IModProxy {
    private static final float live = 1000.0f;
    private static final ResourceLocation resourceLocation = new ResourceLocation("ess", "textures/misc/attack.png");
    Map<Integer, Pair<Boolean, Float>> map = Maps.newHashMap();
    Set<DamageInfo> damageInfoSet = new ConcurrentSet();

    @Override
    public void onFMLInit(FMLInitializationEvent event) {
        if (DamageShower.classExists("net.minecraftforge.event.entity.player.CriticalHitEvent") && DamageShower.isRenderAvailable() && DamageShower.classExists("net.minecraftforge.event.entity.ProjectileImpactEvent")) {
            CriticalHitBridge.register(this);
        } else {
            MinecraftForge.EVENT_BUS.register((Object)new GenericEventProxy());
        }
    }

    public void onArrowHit(Object event) {
        try {
            java.lang.reflect.Method rayMethod = event.getClass().getMethod("getRayTraceResult");
            Object ray = rayMethod.invoke(event);
            Entity entityHit = (Entity)ray.getClass().getField("entityHit").get(ray);
            if (entityHit == null || !(entityHit instanceof EntityLivingBase)) {
                return;
            }
            Object arrow = event.getClass().getMethod("getArrow").invoke(event);
            boolean crit = (Boolean)arrow.getClass().getMethod("getIsCritical").invoke(arrow);
            this.map.put(entityHit.getEntityId(), Pair.of(crit, Float.valueOf(((EntityLivingBase)entityHit).getHealth())));
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException)ex;
            }
        }
    }

    public void onCriticalHit(Object event) {
        try {
            Entity target = (Entity)event.getClass().getMethod("getTarget").invoke(event);
            if (target instanceof EntityLivingBase) {
                EntityLivingBase entityLivingBase = (EntityLivingBase)target;
                boolean crit = (Boolean)event.getClass().getMethod("isVanillaCritical").invoke(event);
                this.map.put(target.getEntityId(), Pair.of(crit, Float.valueOf(entityLivingBase.getHealth())));
            }
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException)ex;
            }
        }
    }

    public void onLivingUpdate(Object event) {
        Pair<Boolean, Float> booleanFloatPair;
        this.damageInfoSet.removeIf(DamageInfo::cleanable);
        EntityLivingBase entity;
        try {
            entity = (EntityLivingBase)event.getClass().getMethod("getEntityLiving").invoke(event);
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException)ex;
            }
            return;
        }
        int entityId = entity.getEntityId();
        if (!this.map.containsKey(entityId)) {
            return;
        }
        int currentHealth = (int)Math.ceil(entity.getHealth());
        int subtract = currentHealth - ((Float)(booleanFloatPair = this.map.get(entityId)).getRight()).intValue();
        if (subtract >= 0) {
            return;
        }
        float displayPosX = (float)entity.posX;
        float displayPosY = (float)(entity.posY + (double)(entity.height * 0.8f));
        float displayPosZ = (float)entity.posZ;
        if (entity.getLastAttackedEntity() != null && entity.getLastAttackedEntity() instanceof EntityPlayer) {
            EntityLivingBase lastAttacker = entity.getLastAttackedEntity();






            displayPosY = (float)(entity.posY + (double)(lastAttacker.height * 0.8f));
        }
        ThreadLocalRandom current = ThreadLocalRandom.current();
        displayPosX = (float)DamageShower.lerp((double)displayPosX - 0.5, (double)displayPosX + 0.5, current.nextDouble());
        float finaldisplayPosZ = (float)DamageShower.lerp((double)displayPosZ - 0.75, (double)displayPosZ + 0.75, current.nextDouble());
        DamageInfo damageInfo = new DamageInfo(-subtract, (Boolean)booleanFloatPair.getKey(), displayPosX, (double)displayPosY + 0.25, displayPosZ, displayPosX, displayPosY + 2.0f, finaldisplayPosZ);
        this.damageInfoSet.add(damageInfo);
        entity.setLastAttackedEntity(null);
        this.map.remove(entityId);
    }

    public void doDraw(float partialTicks) {
        EntityPlayer player = MinecraftUtil.getPlayer();
        double posX = DamageShower.lerp(player.prevPosX, player.posX, partialTicks);
        double posY = DamageShower.lerp(player.prevPosY, player.posY, partialTicks);
        double posZ = DamageShower.lerp(player.prevPosZ, player.posZ, partialTicks);
        GlStateManager.disableLighting();
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GlStateManager.tryBlendFuncSeparate((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, (GlStateManager.SourceFactor)GlStateManager.SourceFactor.ONE, (GlStateManager.DestFactor)GlStateManager.DestFactor.ZERO);
        GlStateManager.enableRescaleNormal();
        MinecraftUtil.getMinecraft().getTextureManager().bindTexture(resourceLocation);
        for (DamageInfo value : this.damageInfoSet) {
            GlStateManager.pushMatrix();
            GlStateManager.disableCull();
            float p = (float)(value.produceTime - TimeMaster.time()) / 1000.0f;
            GlStateManager.translate((double)(DamageShower.lerp(value.getFinalPosX(), value.getPosX(), p) - posX), (double)(DamageShower.lerp(value.getFinalPosY(), value.getPosY(), p) - posY), (double)(DamageShower.lerp(value.getFinalPosZ(), value.getPosZ(), p) - posZ));
            double lerpScale = DamageShower.lerp(0.1, 1.0, (float)(value.produceTime - TimeMaster.time()) / 1000.0f);
            GlStateManager.scale((double)lerpScale, (double)lerpScale, (double)lerpScale);
            GlStateManager.rotate((float)(-player.rotationYaw), (float)0.0f, (float)1.0f, (float)0.0f);
            GlStateManager.rotate((float)((float)(MinecraftUtil.getMinecraft().gameSettings.thirdPersonView == 2 ? -1 : 1) * player.rotationPitch), (float)1.0f, (float)0.0f, (float)0.0f);
            GlStateManager.rotate((float)180.0f, (float)0.0f, (float)0.0f, (float)1.0f);
            for (DamageData damageData : value.getDamageDataList()) {
                GlStateManager.pushMatrix();
                GlStateManager.translate((float)damageData.getPosX(), (float)damageData.getPosY(), (float)damageData.getPosZ());
                DamageShower.drawScaledCustomSizeModalRect(0.0f, 0.0f, damageData.getStartU(), damageData.getStartV(), damageData.getEndU(), damageData.getEndV(), 0.5f, 0.64f, 512.0f, 512.0f);
                GlStateManager.popMatrix();
            }
            GlStateManager.enableCull();
            GlStateManager.popMatrix();
        }
    }

    public static double lerp(double a, double b, double position) {
        return a + (b - a) * position;
    }

    public static boolean isRenderAvailable() {
        return classExists("net.minecraft.client.renderer.BufferBuilder") && classExists("net.minecraft.util.math.Vec3d");
    }

    public static boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    public static void drawScaledCustomSizeModalRect(float x, float y, float u, float v, float uWidth, float vHeight, float width, float height, float tileWidth, float tileHeight) {
        float f = 1.0f / tileWidth;
        float f1 = 1.0f / tileHeight;
        try {
            Class<?> tessellatorClass = Class.forName("net.minecraft.client.renderer.Tessellator");
            java.lang.reflect.Method getInstance = tessellatorClass.getMethod("getInstance");
            Object tessellator = getInstance.invoke(null);
            Object buffer = tessellatorClass.getMethod("getBuffer").invoke(tessellator);
            buffer.getClass().getMethod("begin", int.class, Class.forName("net.minecraft.client.renderer.vertex.VertexFormat")).invoke(buffer, 7, DefaultVertexFormats.POSITION_TEX);
            java.lang.reflect.Method posMethod = buffer.getClass().getMethod("pos", double.class, double.class, double.class);
            Object v0 = posMethod.invoke(buffer, (double)x, (double)(y + height), 0.0);
            v0.getClass().getMethod("tex", double.class, double.class).invoke(v0, (double)(u * f), (double)((v + vHeight) * f1));
            v0.getClass().getMethod("endVertex").invoke(v0);
            Object v1 = posMethod.invoke(buffer, (double)(x + width), (double)(y + height), 0.0);
            v1.getClass().getMethod("tex", double.class, double.class).invoke(v1, (double)((u + uWidth) * f), (double)((v + vHeight) * f1));
            v1.getClass().getMethod("endVertex").invoke(v1);
            Object v2 = posMethod.invoke(buffer, (double)(x + width), (double)y, 0.0);
            v2.getClass().getMethod("tex", double.class, double.class).invoke(v2, (double)((u + uWidth) * f), (double)(v * f1));
            v2.getClass().getMethod("endVertex").invoke(v2);
            Object v3 = posMethod.invoke(buffer, (double)x, (double)y, 0.0);
            v3.getClass().getMethod("tex", double.class, double.class).invoke(v3, (double)(u * f), (double)(v * f1));
            v3.getClass().getMethod("endVertex").invoke(v3);
            tessellatorClass.getMethod("draw").invoke(tessellator);
        } catch (Throwable ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException)ex;
            }
        }
    }

    public class GenericEventProxy {
        @SubscribeEvent
        public void onLiving(net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent event) {
            DamageShower.this.onLivingUpdate(event);
        }

        @SubscribeEvent
        public void onRender(RenderWorldLastEvent event) {
            try {
                DamageShower.this.doDraw(event.getPartialTicks());
            } catch (Throwable ignore) {
            }
        }
    }

    public static class CriticalHitProxy {
        private final DamageShower target;

        public CriticalHitProxy(DamageShower target) {
            this.target = target;
        }

        @SubscribeEvent
        public void onArrow(net.minecraftforge.event.entity.ProjectileImpactEvent.Arrow event) {
            this.target.onArrowHit(event);
        }

        @SubscribeEvent
        public void onLiving(net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent event) {
            this.target.onLivingUpdate(event);
        }

        @SubscribeEvent
        public void onRender(RenderWorldLastEvent event) {
            try {
                this.target.doDraw(event.getPartialTicks());
            } catch (Throwable ignore) {
            }
        }

        @SubscribeEvent
        public void onCrit(net.minecraftforge.event.entity.player.CriticalHitEvent event) {
            this.target.onCriticalHit(event);
        }
    }

    public static class CriticalHitBridge {
        public static void register(DamageShower shower) {
            CriticalHitProxy proxy = new CriticalHitProxy(shower);
            MinecraftForge.EVENT_BUS.register((Object)proxy);
        }
    }

    static class DamageData {
        private final float startU;
        private final float startV;
        private final float endU;
        private final float endV;
        private final float posX;
        private final float posY;
        private final float posZ;

        public DamageData(float startU, float startV, float endU, float endV, float posX, float posY, float posZ) {
            this.startU = startU;
            this.startV = startV;
            this.endU = endU;
            this.endV = endV;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
        }

        public float getStartU() {
            return this.startU;
        }

        public float getStartV() {
            return this.startV;
        }

        public float getEndU() {
            return this.endU;
        }

        public float getEndV() {
            return this.endV;
        }

        public float getPosX() {
            return this.posX;
        }

        public float getPosY() {
            return this.posY;
        }

        public float getPosZ() {
            return this.posZ;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof DamageData)) {
                return false;
            }
            DamageData other = (DamageData)o;
            if (!other.canEqual(this)) {
                return false;
            }
            if (Float.compare(this.getStartU(), other.getStartU()) != 0) {
                return false;
            }
            if (Float.compare(this.getStartV(), other.getStartV()) != 0) {
                return false;
            }
            if (Float.compare(this.getEndU(), other.getEndU()) != 0) {
                return false;
            }
            if (Float.compare(this.getEndV(), other.getEndV()) != 0) {
                return false;
            }
            if (Float.compare(this.getPosX(), other.getPosX()) != 0) {
                return false;
            }
            if (Float.compare(this.getPosY(), other.getPosY()) != 0) {
                return false;
            }
            return Float.compare(this.getPosZ(), other.getPosZ()) == 0;
        }

        protected boolean canEqual(Object other) {
            return other instanceof DamageData;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            result = result * 59 + Float.floatToIntBits(this.getStartU());
            result = result * 59 + Float.floatToIntBits(this.getStartV());
            result = result * 59 + Float.floatToIntBits(this.getEndU());
            result = result * 59 + Float.floatToIntBits(this.getEndV());
            result = result * 59 + Float.floatToIntBits(this.getPosX());
            result = result * 59 + Float.floatToIntBits(this.getPosY());
            result = result * 59 + Float.floatToIntBits(this.getPosZ());
            return result;
        }

        public String toString() {
            return "DamageShower.DamageData(startU=" + this.getStartU() + ", startV=" + this.getStartV() + ", endU=" + this.getEndU() + ", endV=" + this.getEndV() + ", posX=" + this.getPosX() + ", posY=" + this.getPosY() + ", posZ=" + this.getPosZ() + ")";
        }
    }

    static class DamageInfo {
        private final long produceTime = TimeMaster.time() + 1000L;
        private final int subtract;
        private final double posX;
        private final double posY;
        private final double posZ;
        private final float finalPosX;
        private final float finalPosY;
        private final float finalPosZ;
        private final List<DamageData> damageDataList = new ArrayList<DamageData>();

        public DamageInfo(int subtract, boolean isCrit, double posX, double posY, double posZ, float finalPosX, float finalPosY, float finalPosZ) {
            this.subtract = subtract;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.finalPosX = finalPosX;
            this.finalPosY = finalPosY;
            this.finalPosZ = finalPosZ;
            this.parseRenderer(subtract, isCrit ? 64 : 0);
        }

        private void parseRenderer(int value, int v) {
            char[] healthStr = String.valueOf(value).toCharArray();
            int length = healthStr.length;
            float startPosX = (float)length * -0.25f;
            for (int i = 0; i < length; ++i) {
                int number = Character.getNumericValue(healthStr[i]);
                DamageData damageData = new DamageData(number * 50, v, 50.0f, 64.0f, startPosX + (float)i * 0.5f, 0.0f, 0.0f);
                this.damageDataList.add(damageData);
            }
        }

        public boolean cleanable() {
            return TimeMaster.time() > this.produceTime;
        }

        public long getProduceTime() {
            return this.produceTime;
        }

        public int getSubtract() {
            return this.subtract;
        }

        public double getPosX() {
            return this.posX;
        }

        public double getPosY() {
            return this.posY;
        }

        public double getPosZ() {
            return this.posZ;
        }

        public float getFinalPosX() {
            return this.finalPosX;
        }

        public float getFinalPosY() {
            return this.finalPosY;
        }

        public float getFinalPosZ() {
            return this.finalPosZ;
        }

        public List<DamageData> getDamageDataList() {
            return this.damageDataList;
        }
    }
}

