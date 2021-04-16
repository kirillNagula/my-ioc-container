package org.example.wrapper;

public class ObjectWrapper {
    private Object object;
    private String destroyMethodName;
    private String initMethodName;
    private boolean isSingleton;

    public ObjectWrapper(Object object, String destroyMethodName, String initMethodName, boolean isSingleton) {
        this.object = object;
        this.destroyMethodName = destroyMethodName;
        this.initMethodName = initMethodName;
        this.isSingleton = isSingleton;
    }

    public ObjectWrapper() { }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getDestroyMethodName() {
        return destroyMethodName;
    }

    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }

    public String getInitMethodName() {
        return initMethodName;
    }

    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

    public boolean isSingleton() {
        return isSingleton;
    }

    public void setSingleton(boolean singleton) {
        isSingleton = singleton;
    }

    @Override
    public String toString() {
        return "ObjectWrapper{" +
                "object=" + object +
                '}';
    }
}
