package com.example.forum.repository;

import com.example.forum.domain.Topic;
import com.example.forum.domain.TopicStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    
    Page<Topic> findAllByOrderByDataCriacaoDesc(Pageable pageable);
    
    Page<Topic> findByStatusOrderByDataCriacaoDesc(TopicStatus status, Pageable pageable);
    
    boolean existsByTituloAndMensagem(String titulo, String mensagem);
}