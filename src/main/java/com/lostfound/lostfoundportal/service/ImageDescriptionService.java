package com.lostfound.lostfoundportal.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Sends a photo of a lost/found item to Gemini (via Spring AI) and asks for a
 * short, human-readable description that can be used to prefill the item's
 * description field.
 */
@Service
public class ImageDescriptionService {

    private static final String PROMPT = """
            You are helping someone fill in a listing on a campus Lost & Found
            portal. Look at the attached photo of an item and write ONE short
            description (maximum two sentences, plain text, no markdown, no
            preamble) that would help a stranger recognise the item: what it
            is, its color, brand or material if visible, and any distinguishing
            marks. Do not mention that you are an AI or that this is a photo.
            """;

    private final ChatClient chatClient;

    public ImageDescriptionService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String describe(MultipartFile image) throws IOException {

        MimeType mimeType = resolveImageMimeType(image);

        String description = chatClient.prompt()
                .user(userSpec -> userSpec
                        .text(PROMPT)
                        .media(mimeType, image.getResource()))
                .call()
                .content();

        if (description == null || description.isBlank()) {
            throw new IllegalStateException("Gemini returned an empty description.");
        }

        return description.trim();
    }

    private MimeType resolveImageMimeType(MultipartFile image) {
        String contentType = image.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Please upload a JPG, PNG or WEBP image.");
        }

        return MimeTypeUtils.parseMimeType(contentType);
    }
}