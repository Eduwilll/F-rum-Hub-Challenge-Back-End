package com.example.forum.service;

import com.example.forum.domain.Course;
import com.example.forum.domain.Topic;
import com.example.forum.domain.TopicStatus;
import com.example.forum.domain.User;
import com.example.forum.dto.CreateTopicRequest;
import com.example.forum.dto.UpdateTopicRequest;
import com.example.forum.exception.CourseNotFoundException;
import com.example.forum.exception.DuplicateTopicException;
import com.example.forum.exception.TopicNotFoundException;
import com.example.forum.exception.UnauthorizedOperationException;
import com.example.forum.repository.CourseRepository;
import com.example.forum.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final CourseRepository courseRepository;
    private final UserService userService;

    public Page<Topic> findAll(Pageable pageable) {
        return topicRepository.findAllByOrderByDataCriacaoDesc(pageable);
    }

    public Page<Topic> findByStatus(TopicStatus status, Pageable pageable) {
        return topicRepository.findByStatusOrderByDataCriacaoDesc(status, pageable);
    }

    public Topic findById(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new TopicNotFoundException(id));
    }

    @Transactional
    public Topic createTopic(CreateTopicRequest request, User author) {
        // Check for duplicate topic
        if (topicRepository.existsByTituloAndMensagem(request.titulo(), request.mensagem())) {
            throw new DuplicateTopicException();
        }

        // Find course
        Course course = courseRepository.findById(request.cursoId())
                .orElseThrow(() -> new CourseNotFoundException(request.cursoId()));

        // Create topic
        Topic topic = new Topic(request.titulo(), request.mensagem(), author, course);
        return topicRepository.save(topic);
    }

    @Transactional
    public Topic updateTopic(Long id, UpdateTopicRequest request, User currentUser) {
        Topic topic = findById(id);

        // Check authorization - only author or moderator can update
        if (!canModifyTopic(topic, currentUser)) {
            throw new UnauthorizedOperationException("Você só pode atualizar seus próprios tópicos");
        }

        // Check for duplicate if title or message changed
        if (!topic.getTitulo().equals(request.titulo()) || !topic.getMensagem().equals(request.mensagem())) {
            if (topicRepository.existsByTituloAndMensagem(request.titulo(), request.mensagem())) {
                throw new DuplicateTopicException();
            }
        }

        // Update topic
        topic.updateContent(request.titulo(), request.mensagem());
        return topicRepository.save(topic);
    }

    @Transactional
    public void deleteTopic(Long id, User currentUser) {
        Topic topic = findById(id);

        // Check authorization - only author or moderator can delete
        if (!canModifyTopic(topic, currentUser)) {
            throw new UnauthorizedOperationException("Você só pode deletar seus próprios tópicos");
        }

        topicRepository.delete(topic);
    }

    @Transactional
    public Topic closeTopic(Long id, User currentUser) {
        Topic topic = findById(id);

        // Check authorization - only author can close their own topic
        if (!topic.isAuthor(currentUser)) {
            throw new UnauthorizedOperationException("Apenas o autor pode fechar o tópico");
        }

        topic.close();
        return topicRepository.save(topic);
    }

    @Transactional
    public Topic openTopic(Long id, User currentUser) {
        Topic topic = findById(id);

        // Check authorization - only author or moderator can reopen
        if (!canModifyTopic(topic, currentUser)) {
            throw new UnauthorizedOperationException("Você não tem permissão para reabrir este tópico");
        }

        topic.open();
        return topicRepository.save(topic);
    }

    @Transactional
    public Topic updateTopicStatus(Long id, TopicStatus status, User currentUser) {
        Topic topic = findById(id);

        // Check authorization - only author or moderator can change status
        if (!canModifyTopic(topic, currentUser)) {
            throw new UnauthorizedOperationException("Você não tem permissão para alterar o status deste tópico");
        }

        if (status == TopicStatus.CLOSED) {
            topic.close();
        } else {
            topic.open();
        }

        return topicRepository.save(topic);
    }

    private boolean canModifyTopic(Topic topic, User user) {
        return topic.isAuthor(user) || userService.isUserModerator(user);
    }

    public boolean isTopicAuthor(Long topicId, User user) {
        Topic topic = findById(topicId);
        return topic.isAuthor(user);
    }

    public long countTopics() {
        return topicRepository.count();
    }

    public long countTopicsByStatus(TopicStatus status) {
        return topicRepository.findByStatusOrderByDataCriacaoDesc(status, Pageable.unpaged()).getTotalElements();
    }
}