package com.backend.escort.payload.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class AlertRequest {
    private String tag;
    private String body;
    List<MultipartFile> files;
}
