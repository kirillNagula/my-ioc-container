package org.example;

import org.example.annotation.Bean;
import org.example.annotation.Configuration;
import org.example.annotation.Value;

@Configuration
public class RootConfiguration {

    @Bean
    public Cat cat(@Value("Cat") String name){
        return new Cat(name);
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public Dog dog(@Value("Dog") String name){
        return new Dog(name);
    }

    @Bean
    public User user(Dog dog, Cat cat){
        return new User(cat, dog);
    }
}
