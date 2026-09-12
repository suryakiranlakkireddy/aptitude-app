package com.aptitudeapp.controller;

import com.aptitudeapp.entity.Topic;
import com.aptitudeapp.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/topics")
@RequiredArgsConstructor
public class AdminTopicController {

    private final TopicRepository topicRepository;

    @GetMapping
    public List<Topic> list() {
        return topicRepository.findAll();
    }

    @PostMapping
    public Topic create(@RequestBody Topic topic) {
        return topicRepository.save(topic);
    }

    @PutMapping("/{id}")
    public Topic update(@PathVariable Long id, @RequestBody Topic updated) {
        Topic topic = topicRepository.findById(id).orElseThrow();
        topic.setName(updated.getName());
        topic.setDescription(updated.getDescription());
        topic.setCategory(updated.getCategory());
        return topicRepository.save(topic);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        topicRepository.deleteById(id);
    }
}
