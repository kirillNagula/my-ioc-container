package org.example;

public class User {
    private Cat cat;
    private Dog dog;

    public User(Cat cat, Dog dog) {
        this.cat = cat;
        this.dog = dog;
    }

    @Override
    public String toString() {
        return "User{" +
                "cat=" + cat +
                ", dog=" + dog +
                '}';
    }
}
