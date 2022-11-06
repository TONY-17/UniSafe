package com.backend.escort.security.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface ImageStorageService {

    void init() throws IOException;
    void save(MultipartFile file, String alertId) throws IOException;
    void save2(MultipartFile file,Long driverId) throws IOException;
    Resource load(String name) throws MalformedURLException;
    void deleteAll();
    Stream<Path> loadAll() throws IOException;

}
