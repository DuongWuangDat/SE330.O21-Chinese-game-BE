package com.chinese_chess_BE.repository;

import com.chinese_chess_BE.model.History;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HistoryRepository extends MongoRepository<History, String> {

    Page<History> findByUser1IdOrUser2Id(String user1Id, String user2Id, Pageable pageRequest);
}
