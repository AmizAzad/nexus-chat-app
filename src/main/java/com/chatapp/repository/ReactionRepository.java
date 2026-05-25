package com.chatapp.repository;

import com.chatapp.model.Message;
import com.chatapp.model.MessageReaction;
import com.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<MessageReaction, Long> {
    List<MessageReaction> findByMessage(Message message);

    @Query("SELECT r FROM MessageReaction r WHERE r.message.id IN :messageIds")
    List<MessageReaction> findByMessageIdIn(@Param("messageIds") List<Long> messageIds);

    Optional<MessageReaction> findByMessageAndUserAndEmoji(Message message, User user, String emoji);

    void deleteByMessageAndUserAndEmoji(Message message, User user, String emoji);
}

