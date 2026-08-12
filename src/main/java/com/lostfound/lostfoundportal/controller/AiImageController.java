package com.lostfound.lostfoundportal.controller;

import com.lostfound.lostfoundportal.service.ImageDescriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Small JSON API used by the "Generate Description with AI" button on the
 * report-lost / report-found item forms. Not meant to be called directly by
 * a person - it backs a fetch() call from the form pages.
 */
@RestController
@RequestMapping("/api/ai")
public class AiImageController {

    private static final Logger log = LoggerFactory.getLogger(AiImageController.class);

    private final ImageDescriptionService imageDescriptionService;

    public AiImageController(ImageDescriptionService imageDescriptionService) {
        this.imageDescriptionService = imageDescriptionService;
    }

    @PostMapping("/describe-image")
    public ResponseEntity<Map<String, String>> describeImage(@RequestParam("image") MultipartFile image) {

        if (image.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Please choose an image first."));
        }

        try {
            String description = imageDescriptionService.describe(image);
            return ResponseEntity.ok(Map.of("description", description));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));

        } catch (Exception ex) {
            log.warn("AI image description failed", ex);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "AI description isn't available right now. You can still write your own description."));
        }
    }
}