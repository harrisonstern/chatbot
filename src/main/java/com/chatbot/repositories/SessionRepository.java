package com.chatbot.repositories;

import com.chatbot.model.Session;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends CrudRepository<Session, UUID> {

    Optional<Session> findBySessionUUID(UUID sessionUUID);
}
