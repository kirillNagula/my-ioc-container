package org.example;

public class Dog {
    private String name;

    public Dog(String name) {
        this.name = name;
    }

    public void init(){
        System.out.println("Init dog");
    }

    public void destroy(){
        System.out.println("Destroy dog");
    }

    @Override
    public String toString() {
        return "Dog{" +
                "name='" + name + '\'' +
                '}';
    }
}

