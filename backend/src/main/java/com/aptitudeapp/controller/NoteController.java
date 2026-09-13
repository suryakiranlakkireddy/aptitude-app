package com.aptitudeapp.controller;

import com.aptitudeapp.dto.NoteDto;
import com.aptitudeapp.entity.Note;
import com.aptitudeapp.repository.NoteRepository;
import com.aptitudeapp.service.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private static final int SIGNED_URL_TTL_SECONDS = 60 * 60;

    private final NoteRepository noteRepository;
    private final SupabaseStorageService storageService;

    @GetMapping
    public List<NoteDto> list(@RequestParam Long topicId) {
        return noteRepository.findByTopicId(topicId).stream()
                .map(this::toDto)
                .toList();
    }

    private NoteDto toDto(Note note) {
        String signedFileUrl = storageService.createSignedUrl(note.getFileUrl(), SIGNED_URL_TTL_SECONDS);
        String signedThumbUrl = note.getThumbnailUrl() != null
                ? storageService.createSignedUrl(note.getThumbnailUrl(), SIGNED_URL_TTL_SECONDS)
                : null;
        return new NoteDto(note.getId(), note.getTopic().getId(), note.getTitle(),
                signedFileUrl, signedThumbUrl, note.getUploadedAt());
    }
}
