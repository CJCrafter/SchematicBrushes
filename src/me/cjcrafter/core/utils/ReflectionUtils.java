package me.cjcrafter.core.utils;

import org.bukkit.Bukkit;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {

    // The bukkit version string used in package names
    private static String versionString = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

    /**
     * Don't let anyone instantiate this class
     */
    private ReflectionUtils() {
    }

    /**
     * Tries to find class from net.minecraft.server.SERVERVERSION.className
     *
     * @param className the net minecraft server (NMS) class name to search
     * @return class object or null if not found
     */
    public static Class<?> getNMSClass(@Nonnull String className) {
        try {
            return Class.forName("net.minecraft.server." + versionString + "." + className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Tries to find class from org.bukkit.craftbukkit.SERVERVERSION.className
     *
     * @param className the craftbukkit class name to search
     * @return class object or null if not found
     */
    public static Class<?> getCBClass(@Nonnull String className) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + versionString + "." + className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * @param classObject the class from where to get constructor
     * @param parameters the params for constructor
     * @return the constructor or null if not found
     */
    public static Constructor<?> getConstructor(@Nonnull Class<?> classObject, Class<?>... parameters) {
        try {
            return classObject.getConstructor(parameters);
        } catch (NoSuchMethodException | SecurityException e) {
            return null;
        }
    }

    /**
     * Instantiates new object with given constructor and params
     *
     * @param constructor the constructor to construct
     * @param parameters the params for constructor (must match)
     * @return the new instance as object
     */
    public static Object newInstance(@Nonnull Constructor<?> constructor, Object... parameters) {
        try {
            return constructor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }
    }

    /**
     * @param classObject the class from where to get field
     * @param fieldName the field name in class
     * @return the field or null if not found
     */
    public static Field getField(@Nonnull Class<?> classObject, @Nonnull String fieldName) {
        try {
            Field field = classObject.getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            return null;
        }
    }

    /**
     * @param field the field to get value from
     * @param instance the instance holding field (null in static use)
     * @return the field object or null if not found
     */
    public static Object invokeField(@Nonnull Field field, @Nonnull Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return null;
        }
    }

    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            DebugUtils.log(Log.ERROR, "Issue getting class!", e);
            return null;
        }
    }

    /**
     * @param field the field to set new value
     * @param instance the instance holding field (null in static use)
     * @param value the new value for field
     */
    public static void setField(@Nonnull Field field, Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalArgumentException | IllegalAccessException ignore) {}
    }

    /**
     * @param classObject the class from where to get method
     * @param methodName the method name in class
     * @param parameters the params for method
     * @return the method or null if not found
     */
    public static Method getMethod(@Nonnull Class<?> classObject, @Nonnull String methodName, Class<?>... parameters) {
        try {
            Method method = classObject.getDeclaredMethod(methodName, parameters);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method;
        } catch (NoSuchMethodException | SecurityException e) {
            return null;
        }
    }

    /**
     * @param method the method to modify
     * @param instance the instance used to invoke method (null in static use)
     * @param parameters the parmas of method
     * @return the method object or null if not found or null if method is for e.g void
     */
    public static Object invokeMethod(@Nonnull Method method, Object instance, Object... parameters) {
        try {
            return method.invoke(instance, parameters);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }
    }
}
