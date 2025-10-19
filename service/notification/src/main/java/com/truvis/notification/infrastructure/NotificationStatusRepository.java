package com.truvis.notification.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.truvis.notification.domain.Notification;
import com.truvis.notification.domain.NotificationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Redis 기반 알림 상태 저장소
 * - 알림 상태별로 Redis에 저장
 * - JSON 직렬화 사용 (기존 String RedisTemplate 활용)
 */
@Repository
@Slf4j
public class NotificationStatusRepository {

    private final RedisTemplate<String, String> redisTemplate;  // 🎯 String!
    private final ObjectMapper objectMapper;

    // Redis 키 접두사
    private static final String PENDING_PREFIX = "notification:pending:";
    private static final String SENDING_PREFIX = "notification:sending:";
    private static final String FAILED_PREFIX = "notification:failed:";
    private static final String SENT_PREFIX = "notification:sent:";

    public NotificationStatusRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());  // LocalDateTime 지원
    }

    /**
     * 🎯 알림 저장 (상태별로 다른 키 사용)
     */
    public void save(Notification notification) {
        String key = getKeyByStatus(notification);

        try {
            // Notification → JSON String
            String json = objectMapper.writeValueAsString(notification);

            redisTemplate.opsForValue().set(
                    key,
                    json,
                    notification.getTimeToLive()  // TTL 자동 설정
            );

            log.debug("알림 상태 저장: id={}, status={}, key={}",
                    notification.getId(),
                    notification.getStatus(),
                    key);

        } catch (JsonProcessingException e) {
            log.error("알림 직렬화 실패: id={}", notification.getId(), e);
            throw new RuntimeException("알림 저장 실패", e);
        }
    }

    /**
     * 🔍 알림 조회
     */
    public Notification findById(String notificationId) {
        // 모든 prefix에서 찾아보기
        String[] prefixes = {PENDING_PREFIX, SENDING_PREFIX, FAILED_PREFIX, SENT_PREFIX};

        for (String prefix : prefixes) {
            String key = prefix + notificationId;
            String json = redisTemplate.opsForValue().get(key);

            if (json != null) {
                try {
                    // JSON String → Notification
                    Notification notification = objectMapper.readValue(json, Notification.class);
                    log.debug("알림 조회 성공: id={}, prefix={}", notificationId, prefix);
                    return notification;
                } catch (JsonProcessingException e) {
                    log.error("알림 역직렬화 실패: key={}", key, e);
                }
            }
        }

        log.debug("알림 없음: id={}", notificationId);
        return null;
    }

    /**
     * 🔄 실패한 알림 목록 조회 (재시도용!)
     */
    public List<Notification> findFailedNotifications() {
        List<Notification> failedList = new ArrayList<>();

        // FAILED prefix로 시작하는 모든 키 조회
        Set<String> keys = redisTemplate.keys(FAILED_PREFIX + "*");

        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                String json = redisTemplate.opsForValue().get(key);
                if (json != null) {
                    try {
                        Notification notification = objectMapper.readValue(json, Notification.class);
                        failedList.add(notification);
                    } catch (JsonProcessingException e) {
                        log.error("실패 알림 역직렬화 실패: key={}", key, e);
                    }
                }
            }
        }

        log.debug("실패 알림 조회: {} 건", failedList.size());
        return failedList;
    }

    /**
     * 🗑️ 알림 삭제
     */
    public void delete(Notification notification) {
        String key = getKeyByStatus(notification);
        redisTemplate.delete(key);

        log.debug("알림 삭제: id={}, status={}",
                notification.getId(),
                notification.getStatus());
    }

    /**
     * 🔄 상태 변경 시 키 이동
     * 예: PENDING → SENDING으로 상태 변경 시
     */
    public void moveStatus(Notification notification, NotificationStatus oldStatus) {
        // 1. 이전 상태의 키 삭제
        String oldKey = getKeyByStatus(notification.getId(), oldStatus);
        redisTemplate.delete(oldKey);

        // 2. 새 상태로 저장
        save(notification);

        log.debug("알림 상태 이동: id={}, {} → {}",
                notification.getId(),
                oldStatus,
                notification.getStatus());
    }


    /**
     * 이메일 주소로 가장 최근 알림 조회
     * - 인증번호 검증 시 발송 상태 확인용
     */
    public Notification findLatestByRecipient(String recipient) {
        // 모든 상태의 prefix 확인
        String[] prefixes = {SENDING_PREFIX, PENDING_PREFIX, SENT_PREFIX, FAILED_PREFIX};

        // 최근 알림을 찾기 위해 각 prefix에서 조회
        for (String prefix : prefixes) {
            Set<String> keys = redisTemplate.keys(prefix + "*");

            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    String json = redisTemplate.opsForValue().get(key);
                    if (json != null) {
                        try {
                            Notification notification = objectMapper.readValue(json, Notification.class);

                            // recipient가 일치하는 알림 찾기
                            if (notification.getRecipient().equals(recipient)) {
                                log.debug("이메일로 알림 조회: recipient={}, status={}",
                                        recipient, notification.getStatus());
                                return notification;
                            }
                        } catch (JsonProcessingException e) {
                            log.error("알림 역직렬화 실패: key={}", key, e);
                        }
                    }
                }
            }
        }

        log.debug("이메일로 알림 없음: recipient={}", recipient);
        return null;
    }

    /**
     * 📊 상태별 알림 개수 (모니터링용)
     */
    public long countByStatus(NotificationStatus status) {
        String prefix = switch (status) {
            case PENDING -> PENDING_PREFIX;
            case SENDING -> SENDING_PREFIX;
            case FAILED -> FAILED_PREFIX;
            case SENT -> SENT_PREFIX;
        };

        Set<String> keys = redisTemplate.keys(prefix + "*");
        return keys != null ? keys.size() : 0;
    }

    /**
     * 🔑 상태에 따른 Redis 키 생성
     */
    private String getKeyByStatus(Notification notification) {
        return getKeyByStatus(notification.getId(), notification.getStatus());
    }

    private String getKeyByStatus(String notificationId, NotificationStatus status) {
        String prefix = switch (status) {
            case PENDING -> PENDING_PREFIX;
            case SENDING -> SENDING_PREFIX;
            case FAILED -> FAILED_PREFIX;
            case SENT -> SENT_PREFIX;
        };
        return prefix + notificationId;
    }
}