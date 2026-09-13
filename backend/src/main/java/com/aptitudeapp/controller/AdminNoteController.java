package com.aptitudeapp.controller;

import com.aptitudeapp.dto.NoteDto;
import com.aptitudeapp.entity.Note;
import com.aptitudeapp.entity.Topic;
import com.aptitudeapp.repository.NoteRepository;
import com.aptitudeapp.repository.TopicRepository;
import com.aptitudeapp.service.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notes")
@RequiredArgsConstructor
public class AdminNoteController {

    private static final int SIGNED_URL_TTL_SECONDS = 60 * 60;

    private final NoteRepository noteRepository;
    private final TopicRepository topicRepository;
    private final SupabaseStorageService storageService;

    @GetMapping
    public List<NoteDto> list(@RequestParam Long topicId) {
        return noteRepository.findByTopicId(topicId).stream().map(this::toDto).toList();
    }

    @PostMapping
    public NoteDto upload(@RequestParam Long topicId,
                           @RequestParam String title,
                           @RequestParam("file") MultipartFile file,
                           @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail) {
        Topic topic = topicRepository.findById(topicId).orElseThrow();

        String filePath = storageService.upload(file, "notes/" + topicId);
        String thumbPath = (thumbnail != null && !thumbnail.isEmpty())
                ? storageService.upload(thumbnail, "notes/" + topicId + "/thumbnails")
                : null;

        Note note = new Note();
        note.setTopic(topic);
        note.setTitle(title);
        note.setFileUrl(filePath);
        note.setThumbnailUrl(thumbPath);
        noteRepository.save(note);

        return toDto(note);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Note note = noteRepository.findById(id).orElseThrow();
        storageService.delete(note.getFileUrl());
        storageService.delete(note.getThumbnailUrl());
        noteRepository.delete(note);
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
