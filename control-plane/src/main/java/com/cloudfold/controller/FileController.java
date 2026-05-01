package com.cloudfold.controller;

import com.cloudfold.dto.InitUploadRequest;
import com.cloudfold.dto.InitUploadResponse;
import com.cloudfold.service.FileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService service;

    public FileController(FileService service){
        this.service = service;
    }

    @PostMapping("/init-upload")
    public InitUploadResponse initUpload(@RequestBody InitUploadRequest request){
        return service.intiUpload(request);
    }
}
