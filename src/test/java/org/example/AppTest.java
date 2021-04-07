package org.example;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AppTest {

    @Test
    public void shouldAnswerWithTrue() {
        Container container = new Container(RootConfiguration.class);
        Object cat = container.getBean("cat");
        Object dog = container.getBean("dog");
        Object user = container.getBean("user");
        System.out.println(cat);
        System.out.println(dog);
        System.out.println(user);
        container.close();
    }
}
