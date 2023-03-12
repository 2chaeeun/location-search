package com.chaeeun.locationsearch.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Set;

@Repository
@Slf4j
public class RankingRepository {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    public RankingRepository(RedisTemplate<String, String> redisTemplate, ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.redisTemplate = redisTemplate;
    }

    public void zincrby(String key, double increment, String member) {
        reactiveRedisTemplate.opsForZSet().incrementScore(key, member, increment)
                        .onErrorResume(error -> {
                            log.error("Redis ERROR", error);
                            return Mono.empty();
                        }).subscribe();
    }

    public Set<ZSetOperations.TypedTuple<String>> zrevrange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

}
