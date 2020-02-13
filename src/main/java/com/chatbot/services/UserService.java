package com.chatbot.services;

import com.chatbot.model.User;

import java.util.Set;

public interface UserService {

    Set<User> getUsers();

    User getUser(Long id);

    User addUser(User user);

    User saveUser(User user);
}
