package com.chinese_chess_BE.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "history")
public class History {
    @Id
    private String id;
    @DBRef
    private User user1;
    @DBRef
    private User user2;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String user1Id;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String user2Id;
    private int user1Score;
    private int user2Score;
    private String winner;
    private Date createdAt;
}
