package com.chinese_chess_BE.route;

import com.chinese_chess_BE.model.History;
import com.chinese_chess_BE.model.Message;
import com.chinese_chess_BE.model.User;
import com.chinese_chess_BE.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/history")
public class HistoryRoute {
    private final HistoryService historyService;
    @GetMapping("/find")
    public ResponseEntity<Page<History>> getHistoryByUserID(@RequestParam int page, @RequestParam int size){
        User user = new User();
        if(SecurityContextHolder.getContext() != null){
            if(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()){
                user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            }
        }
        Page<History> historyList = historyService.getAllByUserId(user.getId(), page, size);
        return ResponseEntity.ok(historyList);
    }

    @PostMapping("/create")
    public ResponseEntity<Message> createNewHistory(@RequestBody History history){
        History historyResult= historyService.createNewHistory(history);
        if(historyResult==null){
            return ResponseEntity.status(403).body(
                    Message.builder()
                            .message("Some thing went wrong")
                    .build());
        }
        return ResponseEntity.ok(
                Message.builder()
                        .message("Created successfully")
                        .build());
    }
}
