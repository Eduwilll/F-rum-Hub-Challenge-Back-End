package com.example.forum.repository;

import com.example.forum.domain.Response;
import com.example.forum.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResponseRepository extends JpaRepository<Response, Long> {
    
    List<Response> findByTopicoOrderByDataCriacaoAsc(Topic topico);
    
    Optional<Response> findByTopicoAndSolucaoTrue(Topic topico);
}