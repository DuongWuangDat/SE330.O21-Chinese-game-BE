package com.chinese_chess_BE.route;

import com.chinese_chess_BE.model.History;
import com.chinese_chess_BE.model.Message;
import com.chinese_chess_BE.model.User;
import com.chinese_chess_BE.repository.HistoryRepository;
import com.chinese_chess_BE.service.AuthService;
import com.chinese_chess_BE.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final AuthService authService;
    @GetMapping("/all")
    public ResponseEntity<List<History>> getAll(){
        System.out.println("I have ever passed here");
        List<History> historyList = historyService.getAllHistory();

        if (historyList.isEmpty()) {
            return ResponseEntity.noContent().build(); // Return 204 No Content if the list is empty
        } else {
            return ResponseEntity.ok(historyList);
        }
    }
    @GetMapping("/find")
    public ResponseEntity<Page<History>> getHistoryByUserID(@RequestParam int page, @RequestParam int size){
        User user = authService.authenticateSecurityContextHolder();
        if(user==null){
            return ResponseEntity.status(401).body(null);
        }

        Page<History> historyList = historyService.getAllByUserId(user.getId(), page, size);
        return ResponseEntity.ok(historyList);
    }

    @PostMapping("/create")
    public ResponseEntity<Message> createNewHistory(@RequestBody History history, @RequestParam int winScore, @RequestParam int loseScore){
        History historyResult= historyService.createNewHistory(history, winScore,loseScore);
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
