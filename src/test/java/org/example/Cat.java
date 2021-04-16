package org.example;

import org.example.annotation.Component;
import org.example.annotation.Value;

@Component
public class Cat {
    private String name;


    public Cat(@Value("Cat") String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return "Cat{" +
                "name='" + name + '\'' +
                '}';
    }
}
