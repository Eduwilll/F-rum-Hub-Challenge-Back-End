package com.example.forum.service;

import com.example.forum.domain.Response;
import com.example.forum.domain.Topic;
import com.example.forum.domain.User;
import com.example.forum.dto.CreateResponseRequest;
import com.example.forum.exception.ResponseNotFoundException;
import com.example.forum.exception.TopicNotFoundException;
import com.example.forum.exception.UnauthorizedOperationException;
import com.example.forum.repository.ResponseRepository;
import com.example.forum.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResponseService {

    private final ResponseRepository responseRepository;
    private final TopicRepository topicRepository;
    private final UserService userService;

    public List<Response> findByTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicNotFoundException(topicId));
        return responseRepository.findByTopicoOrderByDataCriacaoAsc(topic);
    }

    public Response findById(Long id) {
        return responseRepository.findById(id)
                .orElseThrow(() -> new ResponseNotFoundException(id));
    }

    @Transactional
    public Response createResponse(Long topicId, CreateResponseRequest request, User author) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicNotFoundException(topicId));

        Response response = new Response(request.mensagem(), topic, author);
        return responseRepository.save(response);
    }

    @Transactional
    public Response markAsSolution(Long responseId, User currentUser) {
        Response response = findById(responseId);
        Topic topic = response.getTopico();

        // Only topic author can mark solutions
        if (!topic.isAuthor(currentUser)) {
            throw new UnauthorizedOperationException("Apenas o autor do tópico pode marcar uma resposta como solução");
        }

        // Unmark previous solution if exists
        Optional<Response> currentSolution = responseRepository.findByTopicoAndSolucaoTrue(topic);
        if (currentSolution.isPresent()) {
            currentSolution.get().unmarkAsSolution();
            responseRepository.save(currentSolution.get());
        }

        // Mark new solution
        response.markAsSolution();
        Response savedResponse = responseRepository.save(response);

        // Close the topic when a solution is marked
        topic.close();
        topicRepository.save(topic);

        return savedResponse;
    }

    @Transactional
    public Response unmarkAsSolution(Long responseId, User currentUser) {
        Response response = findById(responseId);
        Topic topic = response.getTopico();

        // Only topic author can unmark solutions
        if (!topic.isAuthor(currentUser)) {
            throw new UnauthorizedOperationException("Apenas o autor do tópico pode desmarcar uma resposta como solução");
        }

        response.unmarkAsSolution();
        Response savedResponse = responseRepository.save(response);

        // Reopen the topic when solution is unmarked
        topic.open();
        topicRepository.save(topic);

        return savedResponse;
    }

    @Transactional
    public void deleteResponse(Long responseId, User currentUser) {
        Response response = findById(responseId);

        // Only response author or moderator can delete
        if (!canModifyResponse(response, currentUser)) {
            throw new UnauthorizedOperationException("Você só pode deletar suas próprias respostas");
        }

        // If deleting a solution, reopen the topic
        if (response.getSolucao()) {
            Topic topic = response.getTopico();
            topic.open();
            topicRepository.save(topic);
        }

        responseRepository.delete(response);
    }

    public Optional<Response> findSolutionByTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicNotFoundException(topicId));
        return responseRepository.findByTopicoAndSolucaoTrue(topic);
    }

    public long countResponsesByTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicNotFoundException(topicId));
        return responseRepository.findByTopicoOrderByDataCriacaoAsc(topic).size();
    }

    public boolean isResponseAuthor(Long responseId, User user) {
        Response response = findById(responseId);
        return response.isAuthor(user);
    }

    public boolean canMarkAsSolution(Long responseId, User user) {
        Response response = findById(responseId);
        return response.getTopico().isAuthor(user);
    }

    private boolean canModifyResponse(Response response, User user) {
        return response.isAuthor(user) || userService.isUserModerator(user);
    }
}