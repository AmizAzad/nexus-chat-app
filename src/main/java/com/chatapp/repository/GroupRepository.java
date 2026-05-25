package com.chatapp.repository;

import com.chatapp.model.Group;
import com.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    @Query("SELECT g FROM Group g JOIN g.members m WHERE m = :user")
    List<Group> findByMember(@Param("user") User user);

    @Query("SELECT g FROM Group g WHERE g.privateGroup = false")
    List<Group> findPublicGroups();
}
