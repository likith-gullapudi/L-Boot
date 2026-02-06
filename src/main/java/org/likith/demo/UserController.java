package org.likith.demo;

import org.likith.annotations.*;
import org.likith.model.User;
@LComponent
@Lcontroller
public class UserController {

    @LPostMapping("/users")
    public User createUser(@LRequestBody User user) {
        System.out.println("Creating user: " + user.getName());
        return user;
    }

    @LGetMapping("/users/{id}")
    public String getUser(@LPathVariable("id") String userId) {
        return "User ID: " + userId;
    }

    @LGetMapping("/search")
    public String search(@LRequestParam("query") String query) {
        return "Searching for: " + query;
    }
}