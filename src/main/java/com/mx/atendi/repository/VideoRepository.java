package com.mx.atendi.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.mx.atendi.entity.Video;

@Repository
public interface VideoRepository extends ReactiveMongoRepository<Video, String> {
}

