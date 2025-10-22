package com.truvis.notification.domain;

public enum NotificationChannel {
    EMAIL("이메일", "user@example.com", true),
    SMS("문자메시지", "010-1234-5678", true),
    PUSH("푸시 알림", "모바일 앱 푸시", false),
    KAKAO("카카오톡", "카카오톡 채널", false);

    private final String description;
    private final String example;
    private final boolean instant;  // 즉시 발송 가능 여부

    NotificationChannel(String description, String example, boolean instant) {
        this.description = description;
        this.example = example;
        this.instant = instant;
    }

    public String getDescription() {
        return description;
    }

    public String getExample() {
        return example;
    }

    /**
     * 즉시 발송 가능한 채널인지
     * EMAIL, SMS는 즉시 발송, PUSH/KAKAO는 배치 처리
     */
    public boolean isInstant() {
        return instant;
    }

    /**
     * 재시도 가능한 채널인지
     */
    public boolean isRetryable() {
        return this == EMAIL || this == SMS;
    }

    /**
     * 채널별 최대 재시도 횟수
     */
    public int maxRetryCount() {
        return switch (this) {
            case EMAIL -> 3;
            case SMS -> 2;
            case PUSH, KAKAO -> 1;
        };
    }

    /**
     * 문자열로부터 채널 찾기
     */
    public static NotificationChannel fromString(String channel) {
        try {
            return NotificationChannel.valueOf(channel.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 알림 채널: " + channel);
        }
    }
}
