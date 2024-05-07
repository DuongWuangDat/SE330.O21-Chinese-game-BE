package com.chinese_chess_BE.repository;

import com.chinese_chess_BE.model.Token;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends MongoRepository<Token, String> {

    List<Token> findByUserIdAndExpiredAndRevoked(String userId, boolean expired, boolean revoked);
    Optional<Token> findByToken(String token);
    void deleteByUserId(String userId);
}
