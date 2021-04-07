package org.example;

import org.example.annotation.Bean;
import org.example.annotation.Value;
import org.example.comparator.MethodParameterCountComparator;
import org.example.wrapper.ObjectWrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class Container {
    private final Class<?> CONFIG_CLASS;
    private final Map<String, ObjectWrapper> CONTAINER = new HashMap<>();

    public Container(Class<?> configClass) {
        this.CONFIG_CLASS = configClass;
        init();
    }

    public void close() {
        Collection<ObjectWrapper> values = CONTAINER.values();
        for (ObjectWrapper value : values) {
            Object object = value.getObject();
            String destroyMethodName = value.getDestroyMethodName();
            if (destroyMethodName.length() > 0){
                try {
                    object.getClass().getDeclaredMethod(destroyMethodName).invoke(object);
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Object getBean(String name) {
        return CONTAINER.get(name);
    }

    public List<Object> getBeans() {
        return new ArrayList<>(CONTAINER.values());
    }

    private void init() {
        List<Method> methods = scanBeanClass();
        methodFactory(methods);
    }

    private List<Method> scanBeanClass() {
        List<Method> methods = new ArrayList<>();
        Method[] declaredMethods = CONFIG_CLASS.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.isAnnotationPresent(Bean.class)) {
                methods.add(declaredMethod);
            }
        }
        methods.sort(new MethodParameterCountComparator());
        return methods;
    }

    private void methodFactory(List<Method> methods) {
        try {
            Object o = CONFIG_CLASS.getDeclaredConstructors()[0].newInstance();
            for (Method method : methods) {
                String initMethodName = method.getDeclaredAnnotation(Bean.class).initMethod();
                String destroyMethod = method.getDeclaredAnnotation(Bean.class).destroyMethod();
                String name = method.getName();
                Parameter[] parameters = method.getParameters();
                List<Object> objectList = getListParameters(parameters);
                Object invoke = method.invoke(o, objectList.toArray());
                initMethod(initMethodName, invoke);
                ObjectWrapper objectWrapper = new ObjectWrapper(invoke, destroyMethod, initMethodName);
                CONTAINER.put(name, objectWrapper);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private List<Object> getListParameters(Parameter[] parameters){
        List<Object> objects = new ArrayList<>();
        for (Parameter parameter : parameters) {
            if (parameter.isAnnotationPresent(Value.class)) {
                String value = parameter.getDeclaredAnnotation(Value.class).value();
                objects.add(value);
            } else {
                Class<?> type = parameter.getType();
                Collection<ObjectWrapper> values = CONTAINER.values();
                for (ObjectWrapper value : values) {
                    if (value.getObject().getClass().equals(type))  objects.add(value.getObject());
                }
            }
        }
        return objects;
    }

    private void initMethod(String initMethodName, Object invoke) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (initMethodName.length() > 0) {
            invoke.getClass().getDeclaredMethod(initMethodName).invoke(invoke);
        }
    }

}
