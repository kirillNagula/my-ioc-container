package org.example;

import org.example.annotation.Component;
import org.example.annotation.Destroy;
import org.example.annotation.Init;
import org.example.annotation.Value;

@Component
public class Dog {
    private String name;

    public Dog(@Value("Dog") String name) {
        this.name = name;
    }

    @Init
    public void init(){
        System.out.println("Init dog");
    }

    @Destroy
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

