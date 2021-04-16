package org.example;

import org.example.annotation.*;

@Configuration
@ComponentScan(basePackage = "org.example")
public class RootConfiguration {

//    @Bean
//    @Scope(ScopeType.PROTOTYPE)
//    public Cat cat2(@Value("Cat 2") String name){
//        return new Cat(name);
//    }
//
//    @Bean
//    @Scope(ScopeType.PROTOTYPE)
//    public Cat cat(@Value("Cat") String name){
//        return new Cat(name);
//    }
//
//    @Bean
//    public Dog dog(@Value("Dog") String name){
//        return new Dog(name);
//    }
//
    @Bean
    public User user(Dog dog, Cat cat){
        return new User(cat, dog);
    }
//
//    @Bean
//    public User user2(Dog dog, @Qualifier("cat2") Cat cat){
//        return new User(cat, dog);
//    }
}
