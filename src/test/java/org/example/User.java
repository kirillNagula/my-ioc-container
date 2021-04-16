package org.example;

import org.example.annotation.Component;

//@Component
public class User {
    private Cat cat;
    private Dog dog;

    public User(Cat cat, Dog dog) {
        this.cat = cat;
        this.dog = dog;
    }

    public Cat getCat() {
        return cat;
    }

    @Override
    public String toString() {
        return "User{" +
                "cat=" + cat +
                ", dog=" + dog +
                '}';
    }
}
