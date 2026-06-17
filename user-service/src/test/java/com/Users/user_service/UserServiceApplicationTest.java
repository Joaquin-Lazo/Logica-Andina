package com.Users.user_service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires database connection — run only inside Docker")
class UserServiceApplicationTests {
    @Test
    void contextLoads() {
    }
}