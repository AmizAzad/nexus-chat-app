package com.chatapp.repository;

import com.chatapp.model.Group;
import com.chatapp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByGroupOrderByTimestampAsc(Group group);

    @Query("SELECT m FROM Message m WHERE m.dmChannel = :channel ORDER BY m.timestamp ASC")
    List<Message> findByDmChannelOrderByTimestampAsc(@Param("channel") String channel);

    @Query("SELECT m FROM Message m WHERE m.dmChannel LIKE %:username% ORDER BY m.timestamp DESC")
    List<Message> findRecentDmsByUsername(@Param("username") String username);
}
