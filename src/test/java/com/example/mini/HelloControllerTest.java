package com.example.mini;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HelloControllerTest {

    private final HelloController controller = new HelloController();

    @Test
    void helloDefault() {
        assertEquals("Hello, World!", controller.hello("World"));
    }

    @Test
    void helloWithName() {
        assertEquals("Hello, Baby!", controller.hello("Baby"));
    }
}
