package com.chatbot.services;

import com.chatbot.model.Session;

import java.util.UUID;

public interface SessionService {

    Session getSession(UUID id);

    Session addSession(Session session);

    Session saveSession(Session session);
}
