package com.backend.escort.payload.response;

import com.backend.escort.model.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CommentInfo {
    private Long id;
    private String username;
    private Comment comment;

}
