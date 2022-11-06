package com.backend.escort.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequest {
    private int rating;
    private String comment;
}
