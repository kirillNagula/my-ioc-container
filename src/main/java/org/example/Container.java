package org.example;

import org.example.annotation.*;
import org.example.comparator.MethodParameterCountComparator;
import org.example.wrapper.ObjectWrapper;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class Container {
    private final Class<?> CONFIG_CLASS;
    private final Map<String, ObjectWrapper> CONTAINER = new HashMap<>();
    private final Map<String, Method> PROTOTYPE_METHODS = new HashMap<>();
    private final Map<String, List<Object>> PROTOTYPE_PARAMETERS = new HashMap<>();


    public Container(Class<?> configClass) {
        this.CONFIG_CLASS = configClass;
        init();
    }

    public Object getBean(String name) {
        return CONTAINER.get(name).getObject();
    }

    public List<Object> getBeans() {
        return new ArrayList<>(CONTAINER.values());
    }

    public void close() {
        destroyMethodScope();
    }

    private void init() {
        List<Method> methodsInConfigurationClass = scanBeanClass();
        String basePackage = CONFIG_CLASS.getDeclaredAnnotation(ComponentScan.class).basePackage();
        List<Constructor<?>> constructorList = scanClassInPackageFindConstructor(basePackage);
        Map<String, List<Method>> methodMap = scanClassInPackageFindMethod(basePackage);
        beanFactory(methodsInConfigurationClass, constructorList, methodMap);
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

    private List<Constructor<?>> scanClassInPackageFindConstructor(String basePackage){
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(Component.class);
        List<Constructor<?>> constructorList = new ArrayList<>();
        for (Class<?> aClass : typesAnnotatedWith) {
            Constructor<?>[] declaredConstructors = aClass.getDeclaredConstructors();
            Optional<Constructor<?>> min = Arrays.
                    stream(declaredConstructors).
                    min(Comparator.comparingInt(Constructor::getParameterCount));
            constructorList.add(min.orElse(null));
        }
        return constructorList;
    }

    private Map<String, List<Method>> scanClassInPackageFindMethod(String basePackage){
        Map<String, List<Method>> methods = new HashMap<>();
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(Component.class);
        for (Class<?> aClass : typesAnnotatedWith) {
            Method[] aClassMethods = aClass.getMethods();
            List<Method> methodList = new ArrayList<>();
            for (Method method : aClassMethods) {
                if (method.isAnnotationPresent(Init.class)) {
                    methodList.add(method);
                    methods.put(aClass.getSimpleName().toLowerCase(), methodList);
                } else if (method.isAnnotationPresent(Destroy.class)){
                    methodList.add(method);
                    methods.put(aClass.getSimpleName().toLowerCase(), methodList);
                } else methods.put(aClass.getSimpleName().toLowerCase(), methodList);
            }
        }
        return methods;
    }


    private void beanFactory(List<Method> methods, List<Constructor<?>> constructors, Map<String, List<Method>> methodMap) {
        for (Constructor<?> constructor : constructors) {
            try {
                Object newInstance;
                if (constructor.getParameterCount() > 0) {
                    Parameter[] parameters = constructor.getParameters();
                    List<Object> objectList = getListParameters(parameters);
                    newInstance = constructor.newInstance(objectList.toArray());
                } else newInstance = constructor.newInstance();
                String[] split = constructor.getName().split("[.]");
                String className = split[split.length - 1].toLowerCase();
                List<Method> initDestroyMethod = methodMap.get(className);
                String initMethodName = initMethodInClass(initDestroyMethod, newInstance);
                String destroyMethodName = destroyMethodInClass(initDestroyMethod);
                ObjectWrapper objectWrapper = new ObjectWrapper(newInstance, destroyMethodName, initMethodName, true);
                CONTAINER.put(className, objectWrapper);
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        try {
            Object o = CONFIG_CLASS.getDeclaredConstructors()[0].newInstance();
            for (Method method : methods) {
                String initMethodName = method.getDeclaredAnnotation(Bean.class).initMethod();
                String destroyMethodName = method.getDeclaredAnnotation(Bean.class).destroyMethod();
                Parameter[] parameters = method.getParameters();
                List<Object> objectList = getListParameters(parameters);
                Object invoke = method.invoke(o, changedList(objectList, o).toArray());
                initMethodScope(initMethodName, invoke);
                ObjectWrapper objectWrapper = new ObjectWrapper(invoke, destroyMethodName, initMethodName, true);
                if (method.isAnnotationPresent(Scope.class)) {
                    ScopeType value = method.getDeclaredAnnotation(Scope.class).value();
                    if (value.equals(ScopeType.PROTOTYPE)){
                        objectWrapper.setSingleton(false);
                        PROTOTYPE_METHODS.put(method.getReturnType().getSimpleName(), method);
                        PROTOTYPE_PARAMETERS.put(method.getReturnType().getSimpleName(), objectList);
                    }
                }
                CONTAINER.put(method.getName().toLowerCase(), objectWrapper);
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
            } else if (parameter.isAnnotationPresent(Qualifier.class)){
                String value = parameter.getDeclaredAnnotation(Qualifier.class).value();
                ObjectWrapper objectWrapper = CONTAINER.get(value);
                if (objectWrapper == null) {
                    throw new NoSuchElementException();
                }
                objects.add(objectWrapper.getObject());
            } else {
                Class<?> type = parameter.getType();
                Collection<ObjectWrapper> values = CONTAINER.values();
                int count = 0;
                for (ObjectWrapper val : values) {
                    if (count > 1) throw new NoSuchElementException();
                    if (val.getObject().getClass().equals(type))  {
                        objects.add(val.getObject());
                        count++;
                    }
                }
            }
        }
        return objects;
    }

    private List<Object> changedList(List<Object> objectList, Object o) throws InvocationTargetException, IllegalAccessException {
        List<Object> changedList = new ArrayList<>();
        for (Object o1 : objectList) {
            if (PROTOTYPE_PARAMETERS.containsKey(o1.getClass().getSimpleName())) {
                List<Object> list = PROTOTYPE_PARAMETERS.get(o1.getClass().getSimpleName());
                Method method1 = PROTOTYPE_METHODS.get(o1.getClass().getSimpleName());
                Object prototypeObject = method1.invoke(o, list.toArray());
                System.out.println(prototypeObject);
                changedList.add(prototypeObject);
            } else changedList.add(o1);
        }
        return changedList;
    }

    private void initMethodScope(String initMethodName, Object invoke) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (initMethodName.length() > 0) {
            invoke.getClass().getDeclaredMethod(initMethodName).invoke(invoke);
        }
    }

    private void destroyMethodScope(){
        Collection<ObjectWrapper> values = CONTAINER.values();
        for (ObjectWrapper value : values) {
            Object object = value.getObject();
            String destroyMethodName = value.getDestroyMethodName();
            if (destroyMethodName == null) continue;
            if (destroyMethodName.length() == 0) continue;
            try {
                object.getClass().getDeclaredMethod(destroyMethodName).invoke(object);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private String initMethodInClass(List<Method> methods, Object instance) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (!methods.isEmpty()) {
            for (Method method : methods) {
                if (method.isAnnotationPresent(Init.class)) {
                    instance.getClass().getDeclaredMethod(method.getName().toLowerCase()).invoke(instance);
                    return method.getName().toLowerCase();
                }
            }
        }
        return null;
    }

    private String destroyMethodInClass(List<Method> methods){
        if (!methods.isEmpty()){
            for (Method method : methods){
                if (method.isAnnotationPresent(Destroy.class)){
                    return method.getName().toLowerCase();
                }
            }
        }
        return null;
    }

}
