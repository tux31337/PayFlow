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
    
    // Secondary Index 키
    private static final String EMAIL_INDEX_PREFIX = "notification:by-email:";  // 이메일별 최근 알림 ID
    private static final String FAILED_INDEX = "notification:failed:index";     // 실패 알림 ID Set
    private static final String PENDING_INDEX = "notification:pending:index";   // 대기 알림 ID Set
    private static final String SENDING_INDEX = "notification:sending:index";   // 발송중 알림 ID Set
    private static final String SENT_INDEX = "notification:sent:index";         // 발송완료 알림 ID Set

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

            // 📇 인덱스 저장 (O(1) 조회를 위한 Secondary Index)
            saveIndexes(notification);

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
     * - ⚡ O(N) Set 기반 조회 (KEYS 명령어 사용 안 함)
     */
    public List<Notification> findFailedNotifications() {
        List<Notification> failedList = new ArrayList<>();

        // 📇 실패 알림 인덱스에서 ID 목록 가져오기 (O(N))
        Set<String> failedIds = redisTemplate.opsForSet().members(FAILED_INDEX);

        if (failedIds != null && !failedIds.isEmpty()) {
            for (String notificationId : failedIds) {
                Notification notification = findById(notificationId);
                if (notification != null) {
                    failedList.add(notification);
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

        // 📇 인덱스에서도 삭제
        removeFromIndexes(notification);

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

        // 2. 이전 상태의 인덱스에서 제거
        removeFromStatusIndex(notification.getId(), oldStatus);

        // 3. 새 상태로 저장 (인덱스도 자동 저장됨)
        save(notification);

        log.debug("알림 상태 이동: id={}, {} → {}",
                notification.getId(),
                oldStatus,
                notification.getStatus());
    }


    /**
     * 이메일 주소로 가장 최근 알림 조회
     * - 인증번호 검증 시 발송 상태 확인용
     * - ⚡ O(1) 직접 접근 (이메일 인덱스 사용)
     */
    public Notification findLatestByRecipient(String recipient) {
        // 📇 이메일 인덱스에서 알림 ID 바로 조회 (O(1))
        String emailKey = EMAIL_INDEX_PREFIX + recipient;
        String notificationId = redisTemplate.opsForValue().get(emailKey);

        if (notificationId != null) {
            // 알림 ID로 실제 데이터 조회
            Notification notification = findById(notificationId);
            
            if (notification != null) {
                log.debug("이메일로 알림 조회 성공: recipient={}, id={}, status={}",
                        recipient, notificationId, notification.getStatus());
                return notification;
            }
        }

        log.debug("이메일로 알림 없음: recipient={}", recipient);
        return null;
    }

    /**
     * 📊 상태별 알림 개수 (모니터링용)
     * - ⚡ O(1) Set 크기 조회 (KEYS 명령어 사용 안 함)
     */
    public long countByStatus(NotificationStatus status) {
        String indexKey = getStatusIndexKey(status);
        Long count = redisTemplate.opsForSet().size(indexKey);
        return count != null ? count : 0;
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

    /**
     * 📇 인덱스 저장 (이메일 인덱스 + 상태별 Set 인덱스)
     */
    private void saveIndexes(Notification notification) {
        // 1. 이메일 인덱스 저장 (최근 알림 ID)
        String emailKey = EMAIL_INDEX_PREFIX + notification.getRecipient();
        redisTemplate.opsForValue().set(
                emailKey,
                notification.getId(),
                notification.getTimeToLive()
        );

        // 2. 상태별 Set 인덱스에 추가
        String statusIndexKey = getStatusIndexKey(notification.getStatus());
        redisTemplate.opsForSet().add(statusIndexKey, notification.getId());
    }

    /**
     * 📇 인덱스에서 제거
     */
    private void removeFromIndexes(Notification notification) {
        // 1. 이메일 인덱스에서 제거 (해당 이메일의 최근 알림이면)
        String emailKey = EMAIL_INDEX_PREFIX + notification.getRecipient();
        String currentId = redisTemplate.opsForValue().get(emailKey);
        if (notification.getId().equals(currentId)) {
            redisTemplate.delete(emailKey);
        }

        // 2. 상태별 Set 인덱스에서 제거
        removeFromStatusIndex(notification.getId(), notification.getStatus());
    }

    /**
     * 📇 상태별 Set 인덱스에서 제거
     */
    private void removeFromStatusIndex(String notificationId, NotificationStatus status) {
        String statusIndexKey = getStatusIndexKey(status);
        redisTemplate.opsForSet().remove(statusIndexKey, notificationId);
    }

    /**
     * 📇 상태별 인덱스 키 조회
     */
    private String getStatusIndexKey(NotificationStatus status) {
        return switch (status) {
            case PENDING -> PENDING_INDEX;
            case SENDING -> SENDING_INDEX;
            case FAILED -> FAILED_INDEX;
            case SENT -> SENT_INDEX;
        };
    }
}