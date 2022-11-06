package com.backend.escort.security.service;

import com.backend.escort.controller.StudentController;
import com.backend.escort.model.DriverImages;
import com.backend.escort.model.Image;
import com.backend.escort.repository.DriverImageRepository;
import com.backend.escort.repository.ImagesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class ImageStorageServiceImpl implements ImageStorageService{

    @Autowired
    ImagesRepository imagesRepository;

    @Autowired
    DriverImageRepository driverImageRepository;

    private final Path root = Paths.get("uploads");
    @Override
    public void init() throws IOException {
        Files.createDirectory(root);
    }

    @Override
    public void save(MultipartFile file, String uniqueAlertId) throws IOException {
        Files.copy(file.getInputStream(),this.root.resolve(file.getOriginalFilename()));
        // Create a new file as we are saving

        System.out.println("PATH " + file.getOriginalFilename());
        Path path = this.root.resolve(file.getOriginalFilename());
        String url = MvcUriComponentsBuilder
                .fromMethodName(StudentController.class, "getFile", path.getFileName().toString()).build().toString();

        Image image = new Image(
                url,
                uniqueAlertId
        );


        System.out.println("IMAGE SAVED " + file.getName());
        imagesRepository.save(image);
    }

    @Override
    public void save2(MultipartFile file,Long driverId) throws IOException {
        Files.copy(file.getInputStream(),this.root.resolve(file.getOriginalFilename()));
        Path path = this.root.resolve(file.getOriginalFilename());
        String url = MvcUriComponentsBuilder
                .fromMethodName(StudentController.class, "getFile", path.getFileName().toString()).build().toString();

        DriverImages driverImage = new DriverImages(
                driverId,
                url
        );
        driverImageRepository.save(driverImage);
    }


    @Override
    public Resource load(String name) throws MalformedURLException {
        Path file = root.resolve(name);
        Resource resource = new UrlResource(file.toUri());
        if(resource.exists()|| resource.isReadable())
            return resource;
        else
            throw new RuntimeException("FAILED TO READ FILE");
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    @Override
    public Stream<Path> loadAll() throws IOException {
        return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
    }
}
