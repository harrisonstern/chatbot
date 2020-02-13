package com.chatbot.services;

import com.chatbot.model.Session;
import com.chatbot.repositories.SessionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SessionServiceImpl implements SessionService {

    SessionRepository sessionRepository;

    public SessionServiceImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public Session getSession(UUID id) {
       return sessionRepository.findBySessionUUID(id).get();
    }

    @Override
    public Session addSession(Session session) {

        return sessionRepository.save(session);

    }

    @Override
    public Session saveSession(Session session) {
        return sessionRepository.save(session);
    }
}
