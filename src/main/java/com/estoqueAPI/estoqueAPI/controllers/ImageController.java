package com.estoqueAPI.estoqueAPI.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.bucketName}")
    private String bucketName;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // Faz o upload da imagem para o S3
            String fileName = file.getOriginalFilename();
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build(),
                RequestBody.fromBytes(file.getBytes())
            );
    
            // Gera a URL pública da imagem
            String imageUrl = "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Falha ao enviar a imagem");
        }
    }

    @GetMapping("/{filename}")
    public ResponseEntity<String> getImage(@PathVariable String filename) {
        // Retorna a URL da imagem no S3
        String imageUrl = "https://" + bucketName + ".s3.amazonaws.com/" + filename;
        return ResponseEntity.ok(imageUrl);
    }
}