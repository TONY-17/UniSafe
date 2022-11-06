package com.backend.escort.helpers;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotFoundException extends RuntimeException{
    public NotFoundException(String exception){
        super(exception);
    }
}
