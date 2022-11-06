package com.backend.escort.payload.response;

import com.backend.escort.model.Image;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class AlertInfo {
    private Long id;
    private String user;
    private String tag;
    private String body;
    private String timePosted;
    private Integer numberOfComments;
    private List<String> imageList;

}
