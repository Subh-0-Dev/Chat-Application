package com.subh.DemoChat.Repository;

import com.subh.DemoChat.Entity.SingleMessage;
import com.subh.DemoChat.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SingleRepository extends JpaRepository<SingleMessage, Long> {

    @Query("SELECT m FROM SingleMessage m WHERE " +
            "(m.sender = :user1 AND m.receiver = :user2) OR " +
            "(m.sender = :user2 AND m.receiver = :user1) " +
            "ORDER BY m.timestamp")
    List<SingleMessage> findChatBetweenUsers(@Param("user1") UserEntity user1,
                                             @Param("user2") UserEntity user2);

    @Query("SELECT DISTINCT m.receiver FROM SingleMessage m WHERE m.sender = :sender")
    List<UserEntity> findDistinctReceiversBySender(@Param("sender") UserEntity sender);

    @Query("SELECT DISTINCT m.sender FROM SingleMessage m WHERE m.receiver = :receiver")
    List<UserEntity> findDistinctSendersByReceiver(@Param("receiver") UserEntity receiver);

    List<SingleMessage> findBySenderAndReceiver(UserEntity sender, UserEntity receiver);

}