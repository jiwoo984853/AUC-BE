package com.mutsa.springboot_auction.domain.auctionImage.controller;

import com.mutsa.springboot_auction.domain.auctionImage.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/s3/test")
public class S3TestController {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadTest(@RequestParam("file")MultipartFile file) {
        try {
            String imageUrl = s3Service.upload(file);
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("업로드실패");
        }
    }

}
