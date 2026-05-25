package com.chatapp.repository;

import com.chatapp.model.UserChannelState;
import com.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserChannelStateRepository extends JpaRepository<UserChannelState, Long> {
    Optional<UserChannelState> findByUserAndChannelKey(User user, String channelKey);
    List<UserChannelState> findByUser(User user);
}

