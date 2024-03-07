package com.chinese_chess_BE.service;

import com.chinese_chess_BE.model.History;
import com.chinese_chess_BE.model.User;
import com.chinese_chess_BE.repository.HistoryRepository;
import com.chinese_chess_BE.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class HistoryService {
    @Autowired
    HistoryRepository historyRepository;
    @Autowired
    UserRepository userRepository;

    public List<History> getAllHistory(){
        List<History> historyList= historyRepository.findAll();
        return historyList;
    }

    public Page<History> getAllByUserId(String userID, int page, int limit){
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        PageRequest pageRequest = PageRequest.of(page-1,limit,sort);
        return historyRepository.findByUser1IdOrUser2Id(userID,userID, pageRequest);
    }

    public History createNewHistory(History history){
        if(history.getUser1Id().equals(history.getUser2Id()) ){
            return null;
        }
        history.setCreatedAt(new Date());
        User user1= userRepository.findById(history.getUser1Id()).get();
        User user2 = userRepository.findById(history.getUser2Id()).get();
        history.setUser2(user2);
        history.setUser1(user1);
        if(history.getUser1Score()> history.getUser2Score()){
            history.setWinner(history.getUser1Id());
        }
        else if(history.getUser1Score()< history.getUser2Score()){
            history.setWinner(history.getUser2Id());
        }
        else {
            history.setWinner("-1");
        }
        return historyRepository.save(history);

    }
}
