package org.example;

import org.junit.Test;


public class AppTest {

    @Test
    public void shouldAnswerWithTrue() {
        Container container = new Container(RootConfiguration.class);
        System.out.println(container.getBeans());
        container.close();
    }

}
