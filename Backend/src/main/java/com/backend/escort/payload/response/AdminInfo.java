package com.backend.escort.payload.response;

import com.backend.escort.model.Organisation;
import com.backend.escort.model.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminInfo {
    private List<Organisation> organisations;
    private String rating;
}
