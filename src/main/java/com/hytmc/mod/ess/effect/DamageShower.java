package com.hytmc.mod.ess.effect;

import com.google.common.collect.Maps;
import com.netease.mc.pcmod.libs.log.Logger;
import com.netease.mc.pcmod.libs.proxy.IModProxy;
import com.netease.mc.pcmod.libs.time.TimeMaster;
import com.netease.mc.pcmod.libs.util.MinecraftUtil;
import io.netty.util.internal.ConcurrentSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

public class DamageShower
implements IModProxy {
    private static final float live = 1000.0f;
    private static final ResourceLocation resourceLocation = new ResourceLocation("ess", "textures/misc/attack.png");
    Map<Integer, Pair<Boolean, Float>> map = Maps.newHashMap();
    Map<Integer, Float> lastHealth = Maps.newHashMap();
    Set<DamageInfo> damageInfoSet = new ConcurrentSet();
    static final Map<Integer, Pair<Boolean, Long>> pendingCrit = new ConcurrentHashMap<Integer, Pair<Boolean, Long>>();
    static volatile boolean drawLogged;
    static volatile long lastDrawLogTime;
    static volatile int drawFailCount;
    static volatile boolean genLogged;
    static volatile boolean drawNonEmptyLogged;
    static volatile boolean baselineLogged;
    static volatile boolean diffLogged;
    static volatile boolean genericErrLogged;
    static volatile int livingLogCount;
    static volatile Class<?> livingEventClass;
    static volatile java.lang.reflect.Method livingEventMethod;
    static volatile java.lang.reflect.Field livingEventField;
    static volatile Class<?> attackEventClass;
    static volatile java.lang.reflect.Method attackEventMethod;
    static volatile java.lang.reflect.Field attackEventField;

    @Override
    public void onFMLInit(FMLInitializationEvent event) {
        boolean critExists = DamageShower.classExists("net.minecraftforge.event.entity.player.CriticalHitEvent");
        boolean renderAvail = DamageShower.isRenderAvailable();
        boolean projExists = DamageShower.classExists("net.minecraftforge.event.entity.ProjectileImpactEvent");
        boolean useCrit = critExists && renderAvail && projExists;
        try {
            Logger.info("dmg init crit={} render={} proj={} -> {}", critExists, renderAvail, projExists, useCrit ? "CriticalHitBridge" : "GenericEventProxy");
        } catch (Throwable ignored) {
        }
        if (useCrit) {
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

    public void onGenericLivingUpdate(Object event) {
        EntityLivingBase entity = DamageShower.getLivingEntity(event);
        if (entity == null) {
            return;
        }
        int entityId = entity.getEntityId();
        if (!entity.isEntityAlive()) {
            this.lastHealth.remove(entityId);
            return;
        }
        float health = entity.getHealth();
        Float oldHealth = this.lastHealth.get(entityId);
        if (oldHealth == null) {
            this.lastHealth.put(entityId, health);
            if (!DamageShower.baselineLogged) {
                DamageShower.baselineLogged = true;
                try {
                    Logger.info("dmg baseline id={} hp={}", entityId, health);
                } catch (Throwable ignored) {
                }
            }
            return;
        }
        float diff = oldHealth.floatValue() - health;
        this.lastHealth.put(entityId, health);
        if (diff <= 0.001f) {
            return;
        }
        if (!DamageShower.diffLogged) {
            DamageShower.diffLogged = true;
            try {
                Logger.info("dmg diff id={} old={} new={}", entityId, oldHealth, health);
            } catch (Throwable ignored) {
            }
        }
        boolean crit = false;
        Pair<Boolean, Float> known = this.map.remove(entityId);
        if (known != null && known.getKey() != null) {
            crit = known.getKey().booleanValue();
        }
        Pair<Boolean, Long> pending = DamageShower.pendingCrit.remove(entityId);
        if (pending != null) {
            if (pending.getKey() != null && pending.getRight() != null && TimeMaster.time() - pending.getRight() < 800L) {
                crit = pending.getKey().booleanValue();
            }
        } else if (!crit) {
            EntityPlayer local = MinecraftUtil.getPlayer();
            if (local != null && entity != local && DamageShower.isCritReady(local) && DamageShower.lookAtEntity(local, entity)) {
                crit = true;
            }
        }
        int subtract = (int)Math.ceil(diff);
        if (subtract <= 0) {
            subtract = 1;
        }
        float displayPosX = (float)entity.posX;
        float displayPosY = (float)(entity.posY + (double)(entity.height * 0.8f));
        float displayPosZ = (float)entity.posZ;
        ThreadLocalRandom current = ThreadLocalRandom.current();
        displayPosX = (float)DamageShower.lerp((double)displayPosX - 0.5, (double)displayPosX + 0.5, current.nextDouble());
        float finaldisplayPosZ = (float)DamageShower.lerp((double)displayPosZ - 0.75, (double)displayPosZ + 0.75, current.nextDouble());
        DamageInfo damageInfo = new DamageInfo(subtract, crit, displayPosX, (double)displayPosY + 0.25, displayPosZ, displayPosX, displayPosY + 2.0f, finaldisplayPosZ);
        this.damageInfoSet.add(damageInfo);
        DamageShower.logGen(entityId, subtract);
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
        if (entity == null) {
            return;
        }
        int entityId = entity.getEntityId();
        if (!entity.isEntityAlive()) {
            this.map.remove(entityId);
            this.lastHealth.remove(entityId);
            return;
        }
        float health = entity.getHealth();
        if (!this.map.containsKey(entityId)) {
            Float oldHealth = this.lastHealth.get(entityId);
            if (oldHealth == null) {
                this.lastHealth.put(entityId, health);
                return;
            }
            float diff = oldHealth.floatValue() - health;
            this.lastHealth.put(entityId, health);
            if (diff <= 0.001f) {
                return;
            }
            int subtract = (int)Math.ceil(diff);
            if (subtract <= 0) {
                subtract = 1;
            }
            int currentHealth = (int)Math.ceil(health);
            if (subtract <= 0) {
                subtract = currentHealth;
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
            DamageInfo damageInfo = new DamageInfo(subtract, false, displayPosX, (double)displayPosY + 0.25, displayPosZ, displayPosX, displayPosY + 2.0f, finaldisplayPosZ);
            this.damageInfoSet.add(damageInfo);
            DamageShower.logGen(entityId, subtract);
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
        DamageShower.logGen(entityId, -subtract);
        entity.setLastAttackedEntity(null);
        this.map.remove(entityId);
        this.lastHealth.put(entityId, entity.getHealth());
    }

    public void doDraw(float partialTicks) {
        if (!DamageShower.drawNonEmptyLogged && !this.damageInfoSet.isEmpty()) {
            DamageShower.drawNonEmptyLogged = true;
            try {
                Logger.info("dmg draw n={}", this.damageInfoSet.size());
            } catch (Throwable ignored) {
            }
        }
        EntityPlayer player = MinecraftUtil.getPlayer();
        double posX = DamageShower.lerp(player.prevPosX, player.posX, partialTicks);
        double posY = DamageShower.lerp(player.prevPosY, player.posY, partialTicks);
        double posZ = DamageShower.lerp(player.prevPosZ, player.posZ, partialTicks);
        GlStateManager.disableLighting();
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
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

    static boolean isCritReady(EntityPlayer player) {
        return player.fallDistance > 0.0f && !player.onGround && !player.isInWater() && !player.isOnLadder();
    }

    static boolean lookAtEntity(EntityPlayer player, Entity entity) {
        double dx = entity.posX - player.posX;
        double dy = entity.posY - player.posY;
        double dz = entity.posZ - player.posZ;
        double distSq = dx * dx + dy * dy + dz * dz;
        if (distSq > 16.0 || distSq < 1.0E-6) {
            return false;
        }
        double yaw = Math.toRadians(player.rotationYaw);
        double pitch = Math.toRadians(player.rotationPitch);
        double lookX = -Math.sin(yaw) * Math.cos(pitch);
        double lookY = -Math.sin(pitch);
        double lookZ = Math.cos(yaw) * Math.cos(pitch);
        return (dx * lookX + dy * lookY + dz * lookZ) / Math.sqrt(distSq) > 0.5;
    }

    public static void drawScaledCustomSizeModalRect(float x, float y, float u, float v, float uWidth, float vHeight, float width, float height, float tileWidth, float tileHeight) {
        float f = 1.0f / tileWidth;
        float f1 = 1.0f / tileHeight;
        try {
            Class<?> tessellatorClass = Class.forName("net.minecraft.client.renderer.Tessellator");
            java.lang.reflect.Method getInstance = DamageShower.findMethod(tessellatorClass, new String[]{"func_178181_a", "getInstance"});
            Object tessellator = getInstance.invoke(null);
            java.lang.reflect.Field bufferField = null;
            for (String fieldName : new String[]{"field_178183_a", "buffer"}) {
                try {
                    bufferField = tessellatorClass.getDeclaredField(fieldName);
                    break;
                } catch (Throwable ignored) {
                }
            }
            if (bufferField == null) {
                DamageShower.logDrawFail("tessellator buffer not found, tried field_178183_a/buffer", "Tessellator");
                return;
            }
            bufferField.setAccessible(true);
            Object buffer = bufferField.get(tessellator);
            if (buffer == null) {
                DamageShower.logDrawFail("tessellator buffer is null", "Tessellator");
                return;
            }
            if (!DamageShower.drawLogged) {
                DamageShower.drawLogged = true;
                try {
                    Logger.info("[ess/DamageShower] draw ok via field:{}", bufferField.getName());
                } catch (Throwable ignored) {
                }
            }
            Class<?> vertexFormatClass = Class.forName("net.minecraft.client.renderer.vertex.VertexFormat");
            java.lang.reflect.Method beginMethod = DamageShower.findMethod(buffer.getClass(), new String[]{"func_181668_a", "begin"}, int.class, vertexFormatClass);
            java.lang.reflect.Method posMethod = DamageShower.findMethod(buffer.getClass(), new String[]{"func_181662_b", "pos"}, double.class, double.class, double.class);
            java.lang.reflect.Method texMethod = DamageShower.findMethod(posMethod.getReturnType(), new String[]{"func_187315_a", "func_181673_a", "tex"}, double.class, double.class);
            java.lang.reflect.Method endVertexMethod = DamageShower.findMethod(posMethod.getReturnType(), new String[]{"func_181675_d", "endVertex"});
            beginMethod.invoke(buffer, 7, DefaultVertexFormats.POSITION_TEX);
            Object v0 = posMethod.invoke(buffer, (double)x, (double)(y + height), 0.0);
            texMethod.invoke(v0, (double)(u * f), (double)((v + vHeight) * f1));
            endVertexMethod.invoke(v0);
            Object v1 = posMethod.invoke(buffer, (double)(x + width), (double)(y + height), 0.0);
            texMethod.invoke(v1, (double)((u + uWidth) * f), (double)((v + vHeight) * f1));
            endVertexMethod.invoke(v1);
            Object v2 = posMethod.invoke(buffer, (double)(x + width), (double)y, 0.0);
            texMethod.invoke(v2, (double)((u + uWidth) * f), (double)(v * f1));
            endVertexMethod.invoke(v2);
            Object v3 = posMethod.invoke(buffer, (double)x, (double)y, 0.0);
            texMethod.invoke(v3, (double)(u * f), (double)(v * f1));
            endVertexMethod.invoke(v3);
            java.lang.reflect.Method drawMethod = DamageShower.findMethod(tessellatorClass, new String[]{"func_78381_a", "draw"});
            drawMethod.invoke(tessellator);
        } catch (Throwable ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException)ex;
            }
            DamageShower.logDrawFail("drawScaledCustomSizeModalRect failed: " + ex, ex.getClass().getName());
        }
    }

    static Entity getAttackTarget(Object event) {
        if (event == null) {
            return null;
        }
        Class<?> eventClass = event.getClass();
        if (attackEventClass == eventClass) {
            return DamageShower.getAttackTargetCached(event);
        }
        try {
            java.lang.reflect.Method method = eventClass.getMethod("getTarget");
            Object value = method.invoke(event);
            if (value instanceof Entity) {
                attackEventField = null;
                attackEventMethod = method;
                attackEventClass = eventClass;
                return (Entity)value;
            }
        } catch (Throwable ignored) {
        }
        try {
            java.lang.reflect.Field field = DamageShower.findField(eventClass, "target");
            Object value = field.get(event);
            if (value instanceof Entity) {
                attackEventMethod = null;
                attackEventField = field;
                attackEventClass = eventClass;
                return (Entity)value;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    static Entity getAttackTargetCached(Object event) {
        try {
            Object value = attackEventMethod != null ? attackEventMethod.invoke(event) : attackEventField.get(event);
            if (value instanceof Entity) {
                return (Entity)value;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    static EntityLivingBase getLivingEntity(Object event) {
        if (event == null) {
            return null;
        }
        Class<?> eventClass = event.getClass();
        if (livingEventClass == eventClass) {
            return DamageShower.getLivingEntityCached(event);
        }
        try {
            java.lang.reflect.Method method = eventClass.getMethod("getEntityLiving");
            Object value = method.invoke(event);
            if (value instanceof EntityLivingBase) {
                livingEventField = null;
                livingEventMethod = method;
                livingEventClass = eventClass;
                return (EntityLivingBase)value;
            }
        } catch (Throwable ignored) {
        }
        try {
            java.lang.reflect.Field field = DamageShower.findField(eventClass, "entityLiving");
            Object value = field.get(event);
            if (value instanceof EntityLivingBase) {
                livingEventMethod = null;
                livingEventField = field;
                livingEventClass = eventClass;
                return (EntityLivingBase)value;
            }
        } catch (Throwable ignored) {
        }
        try {
            java.lang.reflect.Method method = eventClass.getMethod("getEntity");
            Object value = method.invoke(event);
            if (value instanceof EntityLivingBase) {
                livingEventField = null;
                livingEventMethod = method;
                livingEventClass = eventClass;
                return (EntityLivingBase)value;
            }
        } catch (Throwable ignored) {
        }
        try {
            java.lang.reflect.Field field = DamageShower.findField(eventClass, "entity");
            Object value = field.get(event);
            if (value instanceof EntityLivingBase) {
                livingEventMethod = null;
                livingEventField = field;
                livingEventClass = eventClass;
                return (EntityLivingBase)value;
            }
        } catch (Throwable ignored) {
        }
        if (!DamageShower.genericErrLogged) {
            DamageShower.genericErrLogged = true;
            try {
                Logger.error("dmg getLivingEntity fail class={}", event.getClass().getName());
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    static EntityLivingBase getLivingEntityCached(Object event) {
        try {
            Object value = livingEventMethod != null ? livingEventMethod.invoke(event) : livingEventField.get(event);
            if (value instanceof EntityLivingBase) {
                return (EntityLivingBase)value;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    static java.lang.reflect.Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            try {
                java.lang.reflect.Field field = current.getDeclaredField(name);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field;
            } catch (Throwable ignored) {
            }
        }
        throw new NoSuchFieldException(name);
    }

    static java.lang.reflect.Method findMethod(Class<?> clazz, String[] names, Class<?>... types) throws NoSuchMethodException {
        for (String name : names) {
            try {
                return clazz.getMethod(name, types);
            } catch (Throwable ignored) {
            }
        }
        throw new NoSuchMethodException(names[0]);
    }

    static float getPartialTicks(Object event) {
        if (event != null) {
            try {
                Object value = event.getClass().getMethod("getPartialTicks").invoke(event);
                if (value instanceof Number) {
                    return ((Number)value).floatValue();
                }
            } catch (Throwable ignored) {
            }
            try {
                Object value = DamageShower.findField(event.getClass(), "partialTicks").get(event);
                if (value instanceof Number) {
                    return ((Number)value).floatValue();
                }
            } catch (Throwable ignored) {
            }
        }
        return 1.0f;
    }

    static void logGen(int id, int val) {
        if (DamageShower.genLogged) {
            return;
        }
        DamageShower.genLogged = true;
        try {
            Logger.info("dmg gen id={} val={}", id, val);
        } catch (Throwable ignored) {
        }
    }

    static void logDrawFail(String message, String context) {
        long now = TimeMaster.time();
        if (DamageShower.drawFailCount > 0 && now - DamageShower.lastDrawLogTime < 5000L) {
            DamageShower.drawFailCount++;
            return;
        }
        DamageShower.drawFailCount++;
        DamageShower.lastDrawLogTime = now;
        try {
            Logger.error("[ess/DamageShower] {} ({}) count={}", message, context, DamageShower.drawFailCount);
        } catch (Throwable ignored) {
            try {
                System.err.println("[ess/DamageShower] " + message);
            } catch (Throwable ignored2) {
            }
        }
        if (DamageShower.drawFailCount == 1) {
            try {
                new Exception(message).printStackTrace(System.err);
            } catch (Throwable ignored) {
            }
        }
    }

    public class GenericEventProxy {
        @SubscribeEvent
        public void onAttack(AttackEntityEvent event) {
            try {
                Entity target = DamageShower.getAttackTarget(event);
                if (target == null) {
                    return;
                }
                EntityPlayer attacker = MinecraftUtil.getPlayer();
                DamageShower.pendingCrit.put(target.getEntityId(), Pair.of(attacker != null && DamageShower.isCritReady(attacker), TimeMaster.time()));
            } catch (Throwable ignored) {
            }
        }

        @SubscribeEvent
        public void onLiving(net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent event) {
            DamageShower.this.damageInfoSet.removeIf(DamageInfo::cleanable);
            if (DamageShower.livingLogCount < 5) {
                ++DamageShower.livingLogCount;
                try {
                    EntityLivingBase living = DamageShower.getLivingEntity(event);
                    Logger.info("dmg living #{} event={} entity={}", DamageShower.livingLogCount, event.getClass().getSimpleName(), living == null ? "null" : living.getClass().getName());
                } catch (Throwable ignored) {
                }
            }
            DamageShower.this.onGenericLivingUpdate(event);
        }

        @SubscribeEvent
        public void onRender(RenderWorldLastEvent event) {
            try {
                DamageShower.this.doDraw(DamageShower.getPartialTicks(event));
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

