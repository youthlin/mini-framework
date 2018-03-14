package com.youthlin.mvc.util;

import com.youthlin.ioc.util.ClassUtil;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * https://www.cnblogs.com/relucent/p/6525821.html
 * org.springframework.core.LocalVariableTableParameterNameDiscoverer
 * 创建: youthlin.chen
 * 时间: 2018-03-14 13:32
 */
@Resource
public class LocalVariableTableParameterNameDiscoverer implements ParameterNameDiscoverer {
    private Map<Member, String[]> map = new HashMap<>();

    @Override
    public String[] getParameterNames(final Method method) {
        return getParameterNames0(method);
    }

    @Override
    public String[] getParameterNames(Constructor constructor) {
        return getParameterNames0(constructor);
    }

    private String[] getParameterNames0(Member member) {
        String[] result = map.get(member);
        if (result != null) {
            return result;
        }
        Class<?> declaringClass = member.getDeclaringClass();
        ClassLoader declaringClassLoader = declaringClass.getClassLoader();
        Type declaringType = Type.getType(declaringClass);
        String url = declaringType.getInternalName() + ".class";
        InputStream classFileInputStream = declaringClassLoader.getResourceAsStream(url);
        if (classFileInputStream == null) {
            throw new IllegalArgumentException(
                    "The constructor's class loader cannot find the bytecode that defined the constructor's class (URL: "
                            + url + ")");
        }
        try {
            ClassReader cr = new ClassReader(classFileInputStream);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cr.accept(new ParameterNameDiscoveringVisitor(cw, declaringClass, map), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map.get(member);
    }


    /**
     * Helper class that inspects all methods (constructor included) and then
     * attempts to find the parameter names for that member.
     */
    private static class ParameterNameDiscoveringVisitor extends ClassAdapter {
        private static final String STATIC_CLASS_INIT = "<clinit>";
        private final Class<?> clazz;
        private final Map<Member, String[]> memberMap;

        private ParameterNameDiscoveringVisitor(ClassVisitor cv, Class<?> clazz, Map<Member, String[]> memberMap) {
            super(cv);
            this.clazz = clazz;
            this.memberMap = memberMap;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            if (!STATIC_CLASS_INIT.equals(name)) {
                return new LocalVariableTableVisitor(mv, clazz, memberMap, name, desc, Modifier.isStatic(access));
            }
            return mv;
        }

    }

    private static class LocalVariableTableVisitor extends MethodAdapter {

        private static final String CONSTRUCTOR = "<init>";

        private final Class<?> clazz;
        private final Map<Member, String[]> memberMap;
        private final String name;
        private final Type[] args;
        private final boolean isStatic;
        private String[] parameterNames;
        private boolean hasLvtInfo = false;

        /*
         * The nth entry contains the slot index of the LVT table entry holding the
         * argument name for the nth parameter.
         */
        private final int[] lvtSlotIndex;

        private LocalVariableTableVisitor(MethodVisitor mv, Class<?> clazz, Map<Member, String[]> map, String name,
                String desc, boolean isStatic) {
            super(mv);
            this.clazz = clazz;
            this.memberMap = map;
            this.name = name;
            // determine args
            args = Type.getArgumentTypes(desc);
            this.parameterNames = new String[args.length];
            this.isStatic = isStatic;
            this.lvtSlotIndex = computeLvtSlotIndices(isStatic, args);
        }

        @Override
        public void visitLocalVariable(String name, String description, String signature, Label start, Label end,
                int index) {
            this.hasLvtInfo = true;
            for (int i = 0; i < lvtSlotIndex.length; i++) {
                if (lvtSlotIndex[i] == index) {
                    this.parameterNames[i] = name;
                }
            }
        }

        @Override
        public void visitEnd() {
            if (this.hasLvtInfo || (this.isStatic && this.parameterNames.length == 0)) {
                // visitLocalVariable will never be called for static no args methods
                // which doesn't use any local variables.
                // This means that hasLvtInfo could be false for that kind of methods
                // even if the class has local variable info.
                memberMap.put(resolveMember(), parameterNames);
            }
        }

        private Member resolveMember() {
            ClassLoader loader = clazz.getClassLoader();
            Class<?>[] classes = new Class<?>[args.length];

            // resolve args
            for (int i = 0; i < args.length; i++) {
                String className = args[i].getClassName();
                classes[i] = ClassUtil.resolveClassName(className, loader);
            }
            try {
                if (CONSTRUCTOR.equals(name)) {
                    return clazz.getDeclaredConstructor(classes);
                }

                return clazz.getDeclaredMethod(name, classes);
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException("Method [" + name
                        + "] was discovered in the .class file but cannot be resolved in the class object", ex);
            }
        }

        private static int[] computeLvtSlotIndices(boolean isStatic, Type[] paramTypes) {
            int[] lvtIndex = new int[paramTypes.length];
            int nextIndex = (isStatic ? 0 : 1);
            for (int i = 0; i < paramTypes.length; i++) {
                lvtIndex[i] = nextIndex;
                if (isWideType(paramTypes[i])) {
                    nextIndex += 2;
                } else {
                    nextIndex++;
                }
            }
            return lvtIndex;
        }

        private static boolean isWideType(Type aType) {
            // float is not a wide type
            return (aType == Type.LONG_TYPE || aType == Type.DOUBLE_TYPE);
        }

    }

    public static void main(String[] args) {
        LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
        Class<? extends LocalVariableTableParameterNameDiscoverer> clazz = discoverer.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            System.out.println(method);
            System.out.println(Arrays.toString(discoverer.getParameterNames(method)));
            System.out.println();
        }
    }

}
