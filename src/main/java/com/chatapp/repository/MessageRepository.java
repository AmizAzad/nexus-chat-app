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

    // For TTL: only return non-expired, non-deleted messages
    @Query("SELECT m FROM Message m WHERE m.group = :group AND m.deleted = false AND (m.expiresAt IS NULL OR m.expiresAt > :now) ORDER BY m.timestamp ASC")
    List<Message> findActiveByGroup(@Param("group") Group group, @Param("now") LocalDateTime now);

    @Query("SELECT m FROM Message m WHERE m.dmChannel = :channel AND m.deleted = false AND (m.expiresAt IS NULL OR m.expiresAt > :now) ORDER BY m.timestamp ASC")
    List<Message> findActiveByDmChannel(@Param("channel") String channel, @Param("now") LocalDateTime now);

    // Pinned messages
    @Query("SELECT m FROM Message m WHERE m.group = :group AND m.pinned = true AND m.deleted = false ORDER BY m.pinnedAt DESC")
    List<Message> findPinnedByGroup(@Param("group") Group group);

    // Search
    @Query("SELECT m FROM Message m WHERE m.deleted = false AND LOWER(m.content) LIKE LOWER(CONCAT('%',:query,'%')) AND (m.expiresAt IS NULL OR m.expiresAt > :now) ORDER BY m.timestamp DESC")
    List<Message> searchMessages(@Param("query") String query, @Param("now") LocalDateTime now);

    // Count unread - messages after a given time in a group
    @Query("SELECT COUNT(m) FROM Message m WHERE m.group = :group AND m.deleted = false AND m.timestamp > :since AND (m.expiresAt IS NULL OR m.expiresAt > :now)")
    long countUnreadInGroup(@Param("group") Group group, @Param("since") LocalDateTime since, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.dmChannel = :channel AND m.deleted = false AND m.timestamp > :since AND (m.expiresAt IS NULL OR m.expiresAt > :now)")
    long countUnreadInDm(@Param("channel") String channel, @Param("since") LocalDateTime since, @Param("now") LocalDateTime now);

    // Last N messages for AI summarization
    @Query("SELECT m FROM Message m WHERE m.group = :group AND m.deleted = false AND (m.expiresAt IS NULL OR m.expiresAt > :now) ORDER BY m.timestamp DESC")
    List<Message> findRecentByGroup(@Param("group") Group group, @Param("now") LocalDateTime now);
}
