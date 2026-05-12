package com.chatapp.repository;

import com.chatapp.model.Group;
import com.chatapp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByGroupOrderByTimestampAsc(Group group);

    @Query("SELECT m FROM Message m WHERE m.dmChannel = :channel ORDER BY m.timestamp ASC")
    List<Message> findByDmChannelOrderByTimestampAsc(@Param("channel") String channel);

    @Query("SELECT m FROM Message m WHERE m.dmChannel LIKE %:username% ORDER BY m.timestamp DESC")
    List<Message> findRecentDmsByUsername(@Param("username") String username);

    @Query("SELECT m FROM Message m WHERE m.expiresAt <= :now")
    List<Message> findExpiredMessages(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.expiresAt <= :now")
    int deleteExpiredMessages(@Param("now") LocalDateTime now);

    // For TTL: only return non-expired messages
    @Query("SELECT m FROM Message m WHERE m.group = :group AND (m.expiresAt IS NULL OR m.expiresAt > :now) ORDER BY m.timestamp ASC")
    List<Message> findActiveByGroup(@Param("group") Group group, @Param("now") LocalDateTime now);

    @Query("SELECT m FROM Message m WHERE m.dmChannel = :channel AND (m.expiresAt IS NULL OR m.expiresAt > :now) ORDER BY m.timestamp ASC")
    List<Message> findActiveByDmChannel(@Param("channel") String channel, @Param("now") LocalDateTime now);
}
