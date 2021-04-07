package org.example.wrapper;

public class ObjectWrapper {
    private Object object;
    private String destroyMethodName;
    private String initMethodName;

    public ObjectWrapper(Object object, String destroyMethodName, String initMethodName) {
        this.object = object;
        this.destroyMethodName = destroyMethodName;
        this.initMethodName = initMethodName;
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

    @Override
    public String toString() {
        return "ObjectWrapper{" +
                "object=" + object +
                '}';
    }
}
