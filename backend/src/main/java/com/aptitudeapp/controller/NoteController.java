package com.aptitudeapp.controller;

import com.aptitudeapp.entity.Note;
import com.aptitudeapp.entity.Topic;
import com.aptitudeapp.repository.NoteRepository;
import com.aptitudeapp.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteRepository noteRepository;
    private final TopicRepository topicRepository;

    @GetMapping
    public List<Note> list(@RequestParam Long topicId) {
        return noteRepository.findByTopicId(topicId);
    }

    // fileUrl / thumbnailUrl are expected to already point to Supabase Storage
    // (upload happens client-side or via a separate signed-URL endpoint - TODO).
    @PostMapping("/admin")
    public Note create(@RequestBody Note note, @RequestParam Long topicId) {
        Topic topic = topicRepository.findById(topicId).orElseThrow();
        note.setTopic(topic);
        return noteRepository.save(note);
    }
}
