package com.hytmc.mod.ess.coremod;

import net.minecraft.launchwrapper.IClassTransformer;

public class CoreTransformer
implements IClassTransformer {
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName("net.minecraft.scoreboard.ScorePlayerTeam");
        } catch (Throwable ignore) {
            return basicClass;
        }
        if (clazz != null) {
            try {
                Class<?> asm = Class.forName("org.objectweb.asm.tree.ClassNode");
                if (asm != null) {
                    return this.transform112(name, transformedName, basicClass);
                }
            } catch (Throwable ignore) {
                return basicClass;
            }
        }
        return basicClass;
    }

    private byte[] transform112(String name, String transformedName, byte[] basicClass) {
        byte[] out = basicClass;
        try {
            java.lang.reflect.Method m = this.getClass().getDeclaredMethod("doTransform112", String.class, String.class, byte[].class);
            m.setAccessible(true);
            out = (byte[])m.invoke(this, name, transformedName, basicClass);
        } catch (Throwable ignore) {
            return basicClass;
        }
        return out;
    }




































































}
