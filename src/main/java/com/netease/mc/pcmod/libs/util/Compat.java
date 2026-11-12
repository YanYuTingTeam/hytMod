package com.netease.mc.pcmod.libs.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Compat {
    private static Boolean isNewApiCache;
    private static Class<?> keyBindingClassCache;
    private static boolean keyBindingMiss;
    private static Class<?> clientRegistryClassCache;
    private static boolean clientRegistryMiss;
    private static Method isKeyDownMethodCache;
    private static boolean isKeyDownMiss;
    private static Method getKeyCodeMethodCache;
    private static boolean getKeyCodeMiss;
    private static Method setKeyBindStateMethodCache;
    private static boolean setKeyBindStateMiss;
    private static Method registerKeyBindingMethodCache;
    private static boolean registerKeyBindingMiss;
    private static Method getDisplayNameMethodCache;
    private static boolean getDisplayNameMiss;
    private static Method getKeyDescriptionMethodCache;
    private static boolean getKeyDescriptionMiss;
    private static Method drawStringFlatCache;
    private static boolean drawStringFlatMiss;
    private static Method drawStringIntCache;
    private static boolean drawStringIntMiss;
    private static Method drawStringIntShadowCache;
    private static boolean drawStringIntShadowMiss;
    private static Method drawStringFlatShadowCache;
    private static boolean drawStringFlatShadowMiss;

    public static boolean isNewApi() {
        if (isNewApiCache != null) {
            return isNewApiCache;
        }
        boolean ok = false;
        try {
            Class.forName("net.minecraft.util.EnumHandSide");
            Class.forName("net.minecraft.network.datasync.DataParameter");
            ok = true;
        } catch (Throwable ignored) {
            ok = false;
        }
        isNewApiCache = ok;
        return ok;
    }

    public static Class<?> getKeyBindingClass() {
        if (keyBindingClassCache != null) {
            return keyBindingClassCache;
        }
        if (keyBindingMiss) {
            return null;
        }
        try {
            keyBindingClassCache = Class.forName("net.minecraft.client.settings.KeyBinding");
            return keyBindingClassCache;
        } catch (Throwable ignored) {
        }
        keyBindingMiss = true;
        return null;
    }

    public static Method getIsKeyDownMethod(Class<?> clazz) {
        if (isKeyDownMethodCache != null) {
            return isKeyDownMethodCache;
        }
        if (isKeyDownMiss) {
            return null;
        }
        Method m = findMethod(clazz, "func_151474_a", new Class<?>[0]);
        if (m == null) {
            m = findMethod(clazz, "isKeyDown", new Class<?>[0]);
        }
        if (m == null) {
            m = findMethod(clazz, "func_151470_d", new Class<?>[0]);
        }
        if (m == null) {
            m = findMethod(clazz, "isPressed", new Class<?>[0]);
        }
        if (m == null) {
            isKeyDownMiss = true;
            return null;
        }
        m.setAccessible(true);
        isKeyDownMethodCache = m;
        return m;
    }

    public static Method getGetKeyCodeMethod(Class<?> clazz) {
        if (getKeyCodeMethodCache != null) {
            return getKeyCodeMethodCache;
        }
        if (getKeyCodeMiss) {
            return null;
        }
        Method m = findMethod(clazz, "func_151463_i", new Class<?>[0]);
        if (m == null) {
            m = findMethod(clazz, "getKeyCode", new Class<?>[0]);
        }
        if (m == null) {
            getKeyCodeMiss = true;
            return null;
        }
        m.setAccessible(true);
        getKeyCodeMethodCache = m;
        return m;
    }

    public static Method getSetKeyBindStateMethod(Class<?> clazz) {
        if (setKeyBindStateMethodCache != null) {
            return setKeyBindStateMethodCache;
        }
        if (setKeyBindStateMiss) {
            return null;
        }
        Method m = findMethod(clazz, "func_74510_a", new Class<?>[]{int.class, boolean.class});
        if (m == null) {
            m = findMethod(clazz, "setKeyBindState", new Class<?>[]{int.class, boolean.class});
        }
        if (m == null) {
            setKeyBindStateMiss = true;
            return null;
        }
        m.setAccessible(true);
        setKeyBindStateMethodCache = m;
        return m;
    }

    public static Class<?> getClientRegistryClass() {
        if (clientRegistryClassCache != null) {
            return clientRegistryClassCache;
        }
        if (clientRegistryMiss) {
            return null;
        }
        try {
            clientRegistryClassCache = Class.forName("net.minecraftforge.fml.client.registry.ClientRegistry");
            return clientRegistryClassCache;
        } catch (Throwable ignored) {
        }
        clientRegistryMiss = true;
        return null;
    }

    public static Method getRegisterKeyBindingMethod(Class<?> clazz) {
        if (registerKeyBindingMethodCache != null) {
            return registerKeyBindingMethodCache;
        }
        if (registerKeyBindingMiss) {
            return null;
        }
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if ("registerKeyBinding".equals(method.getName())) {
                if (method.getParameterTypes().length == 1) {
                    method.setAccessible(true);
                    registerKeyBindingMethodCache = method;
                    return method;
                }
            }
        }
        registerKeyBindingMiss = true;
        return null;
    }

    public static Method getGetDisplayNameMethod(Class<?> clazz) {
        if (getDisplayNameMethodCache != null) {
            return getDisplayNameMethodCache;
        }
        if (getDisplayNameMiss) {
            return null;
        }
        Method m = findMethod(clazz, "func_151452_a", new Class<?>[0]);
        if (m == null) {
            m = findMethod(clazz, "getDisplayName", new Class<?>[0]);
        }
        if (m == null) {
            getDisplayNameMiss = true;
            return null;
        }
        m.setAccessible(true);
        getDisplayNameMethodCache = m;
        return m;
    }

    public static Method getGetKeyDescriptionMethod(Class<?> clazz) {
        if (getKeyDescriptionMethodCache != null) {
            return getKeyDescriptionMethodCache;
        }
        if (getKeyDescriptionMiss) {
            return null;
        }
        Method m = findMethod(clazz, "func_151463_h", new Class<?>[0]);
        if (m == null) {
            m = findMethod(clazz, "getKeyDescription", new Class<?>[0]);
        }
        if (m == null) {
            getKeyDescriptionMiss = true;
            return null;
        }
        m.setAccessible(true);
        getKeyDescriptionMethodCache = m;
        return m;
    }

    public static boolean setKeyBindState(int keyCode, boolean pressed) {
        Class<?> clazz = getKeyBindingClass();
        if (clazz == null) {
            return false;
        }
        Method m = getSetKeyBindStateMethod(clazz);
        if (m == null) {
            return false;
        }
        try {
            m.invoke(null, keyCode, pressed);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean registerKeyBinding(Object keyBinding) {
        Class<?> clazz = getClientRegistryClass();
        if (clazz == null || keyBinding == null) {
            return false;
        }
        Method m = getRegisterKeyBindingMethod(clazz);
        if (m == null) {
            return false;
        }
        try {
            m.invoke(null, keyBinding);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isKeyDown(Object keyBinding) {
        if (keyBinding == null) {
            return false;
        }
        Method m = getIsKeyDownMethod(keyBinding.getClass());
        if (m == null) {
            return false;
        }
        try {
            Object value = m.invoke(keyBinding);
            return Boolean.TRUE.equals(value);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static int getKeyCode(Object keyBinding) {
        if (keyBinding == null) {
            return 0;
        }
        Method m = getGetKeyCodeMethod(keyBinding.getClass());
        if (m == null) {
            return 0;
        }
        try {
            Object value = m.invoke(keyBinding);
            return ((Number) value).intValue();
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public static String getDisplayName(Object keyBinding) {
        if (keyBinding == null) {
            return "";
        }
        Method m = getGetDisplayNameMethod(keyBinding.getClass());
        if (m != null) {
            try {
                Object value = m.invoke(keyBinding);
                return value == null ? "" : value.toString();
            } catch (Throwable ignored) {
            }
        }
        Method d = getGetKeyDescriptionMethod(keyBinding.getClass());
        if (d != null) {
            try {
                Object value = d.invoke(keyBinding);
                if (value != null) {
                    String text = value.toString();
                    if (text != null && text.length() > 0 && !text.startsWith("key.")) {
                        return text;
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        int keyCode = getKeyCode(keyBinding);
        if (keyCode <= 0) {
            return "";
        }
        try {
            Class<?> keyboard = Class.forName("org.lwjgl.input.Keyboard");
            Method getKeyName = keyboard.getMethod("getKeyName", int.class);
            Object name = getKeyName.invoke(null, keyCode);
            if (name != null) {
                return name.toString();
            }
        } catch (Throwable ignored) {
        }
        return "";
    }

    public static Method getDrawStringFlatMethod(Class<?> clazz) {
        if (drawStringFlatCache != null) {
            return drawStringFlatCache;
        }
        if (drawStringFlatMiss) {
            return null;
        }
        Method m = findMethod(clazz, "func_175063_a", String.class, float.class, float.class, int.class);
        if (m == null) {
            m = findMethod(clazz, "drawString", String.class, float.class, float.class, int.class);
        }
        if (m == null) {
            drawStringFlatMiss = true;
            return null;
        }
        m.setAccessible(true);
        drawStringFlatCache = m;
        return m;
    }

    public static Method getDrawStringIntMethod(Class<?> clazz) {
        if (drawStringIntCache != null) {
            return drawStringIntCache;
        }
        if (drawStringIntMiss) {
            return null;
        }
        Method m = findMethod(clazz, "func_78276_b", String.class, int.class, int.class, int.class);
        if (m == null) {
            m = findMethod(clazz, "drawString", String.class, int.class, int.class, int.class);
        }
        if (m == null) {
            drawStringIntMiss = true;
            return null;
        }
        m.setAccessible(true);
        drawStringIntCache = m;
        return m;
    }

    public static void drawStringFlat(Object fontRenderer, String text, float x, float y, int color) {
        if (fontRenderer == null) {
            return;
        }
        Method m = getDrawStringFlatMethod(fontRenderer.getClass());
        if (m == null) {
            return;
        }
        try {
            m.invoke(fontRenderer, text, x, y, color);
        } catch (Throwable ignored) {
        }
    }

    public static void drawStringInt(Object fontRenderer, String text, int x, int y, int color) {
        if (fontRenderer == null) {
            return;
        }
        Method m = getDrawStringIntMethod(fontRenderer.getClass());
        if (m == null) {
            return;
        }
        try {
            m.invoke(fontRenderer, text, x, y, color);
        } catch (Throwable ignored) {
        }
    }

    public static Method getDrawStringIntShadowMethod(Class<?> clazz) {
        if (drawStringIntShadowCache != null) {
            return drawStringIntShadowCache;
        }
        if (drawStringIntShadowMiss) {
            return null;
        }
        Method m = findMethod(clazz, "func_175065_a", String.class, float.class, float.class, int.class, boolean.class);
        if (m == null) {
            m = findMethod(clazz, "func_175063_a", String.class, float.class, float.class, int.class);
        }
        if (m == null) {
            m = findMethod(clazz, "drawStringWithShadow", String.class, int.class, int.class, int.class);
        }
        if (m == null) {
            m = findMethod(clazz, "drawString", String.class, float.class, float.class, int.class, boolean.class);
        }
        if (m == null) {
            m = findMethod(clazz, "drawStringWithShadow", String.class, float.class, float.class, int.class);
        }
        if (m == null) {
            drawStringIntShadowMiss = true;
            return null;
        }
        m.setAccessible(true);
        drawStringIntShadowCache = m;
        return m;
    }

    public static Method getDrawStringFlatShadowMethod(Class<?> clazz) {
        if (drawStringFlatShadowCache != null) {
            return drawStringFlatShadowCache;
        }
        if (drawStringFlatShadowMiss) {
            return null;
        }
        Method m = findMethod(clazz, "func_175065_a", String.class, float.class, float.class, int.class, boolean.class);
        if (m == null) {
            m = findMethod(clazz, "func_175063_a", String.class, float.class, float.class, int.class);
        }
        if (m == null) {
            m = findMethod(clazz, "drawString", String.class, float.class, float.class, int.class, boolean.class);
        }
        if (m == null) {
            m = findMethod(clazz, "drawStringWithShadow", String.class, float.class, float.class, int.class);
        }
        if (m == null) {
            drawStringFlatShadowMiss = true;
            return null;
        }
        m.setAccessible(true);
        drawStringFlatShadowCache = m;
        return m;
    }

    public static void drawStringIntWithShadow(Object fontRenderer, String text, int x, int y, int color, boolean shadow) {
        if (fontRenderer == null) {
            return;
        }
        Method m = getDrawStringIntShadowMethod(fontRenderer.getClass());
        if (m == null) {
            drawStringInt(fontRenderer, text, x, y, color);
            return;
        }
        try {
            Class<?>[] types = m.getParameterTypes();
            if (types.length == 4) {
                if (types[1] == float.class) {
                    m.invoke(fontRenderer, text, (float)x, (float)y, color);
                } else {
                    m.invoke(fontRenderer, text, x, y, color);
                }
            } else if (types[1] == float.class) {
                m.invoke(fontRenderer, text, (float)x, (float)y, color, shadow);
            } else {
                m.invoke(fontRenderer, text, x, y, color, shadow);
            }
        } catch (Throwable ignored) {
            drawStringInt(fontRenderer, text, x, y, color);
        }
    }

    public static void drawStringFlatWithShadow(Object fontRenderer, String text, float x, float y, int color, boolean shadow) {
        if (fontRenderer == null) {
            return;
        }
        Method m = getDrawStringFlatShadowMethod(fontRenderer.getClass());
        if (m == null) {
            drawStringFlat(fontRenderer, text, x, y, color);
            return;
        }
        try {
            Class<?>[] types = m.getParameterTypes();
            if (types.length == 4) {
                if (types[1] == int.class) {
                    m.invoke(fontRenderer, text, (int)x, (int)y, color);
                } else {
                    m.invoke(fontRenderer, text, x, y, color);
                }
            } else if (types[1] == int.class) {
                m.invoke(fontRenderer, text, (int)x, (int)y, color, shadow);
            } else {
                m.invoke(fontRenderer, text, x, y, color, shadow);
            }
        } catch (Throwable ignored) {
            drawStringFlat(fontRenderer, text, x, y, color);
        }
    }

    public static void drawStringWithShadow(Object fontRenderer, String text, int x, int y, int color, boolean shadow) {
        drawStringIntWithShadow(fontRenderer, text, x, y, color, shadow);
    }

    public static Field findField(Class<?> clazz, String... names) {
        if (clazz == null) {
            return null;
        }
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (String name : names) {
                try {
                    Field field = current.getDeclaredField(name);
                    field.setAccessible(true);
                    return field;
                } catch (Throwable ignored) {
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    public static Method findMethod(Class<?> clazz, String name, Class<?>... params) {
        if (clazz == null) {
            return null;
        }
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                Method method = current.getDeclaredMethod(name, params);
                method.setAccessible(true);
                return method;
            } catch (Throwable ignored) {
            }
            current = current.getSuperclass();
        }
        return null;
    }
}
