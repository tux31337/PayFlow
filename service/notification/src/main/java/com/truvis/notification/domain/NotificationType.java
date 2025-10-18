package com.truvis.notification.domain;

/**
 * 알림 유형
 * - 어떤 목적의 알림인지 구분
 */
public enum NotificationType {
    VERIFICATION_CODE(
            "인증번호 발송",
            "회원가입/로그인 시 이메일 인증번호 발송",
            true,   // 긴급
            10      // 유효시간 10분
    ),
    PASSWORD_RESET(
            "비밀번호 재설정",
            "비밀번호 찾기 링크 발송",
            true,   // 긴급
            30      // 유효시간 30분
    ),
    WELCOME(
            "가입 환영",
            "회원가입 완료 축하 메시지",
            false,  // 일반
            0       // 유효시간 없음
    ),
    TRANSACTION_ALERT(
            "거래 알림",
            "주식 매수/매도 완료 알림",
            true,   // 긴급
            0       // 유효시간 없음
    ),
    MARKETING(
            "마케팅",
            "프로모션 및 이벤트 안내",
            false,  // 일반
            0       // 유효시간 없음
    );

    private final String description;
    private final String purpose;
    private final boolean urgent;
    private final int validityMinutes;  // 유효시간 (분)

    NotificationType(String description, String purpose, boolean urgent, int validityMinutes) {
        this.description = description;
        this.purpose = purpose;
        this.urgent = urgent;
        this.validityMinutes = validityMinutes;
    }

    public String getDescription() {
        return description;
    }

    public String getPurpose() {
        return purpose;
    }

    /**
     * 긴급한 알림인지 (우선순위 높음)
     */
    public boolean isUrgent() {
        return urgent;
    }

    /**
     * 유효시간이 있는 알림인지
     */
    public boolean hasValidity() {
        return validityMinutes > 0;
    }

    public int getValidityMinutes() {
        return validityMinutes;
    }

    /**
     * 타입별 타임아웃 설정 (초)
     */
    public int getTimeoutSeconds() {
        return urgent ? 10 : 30;
    }

    /**
     * 해당 타입이 특정 채널을 지원하는지
     */
    public boolean supportsChannel(NotificationChannel channel) {
        return switch (this) {
            case VERIFICATION_CODE, PASSWORD_RESET ->
                    channel == NotificationChannel.EMAIL || channel == NotificationChannel.SMS;
            case TRANSACTION_ALERT ->
                    channel == NotificationChannel.EMAIL ||
                            channel == NotificationChannel.PUSH ||
                            channel == NotificationChannel.SMS;
            case WELCOME, MARKETING -> true;  // 모든 채널 지원
        };
    }

    /**
     * 문자열로부터 타입 찾기
     */
    public static NotificationType fromString(String type) {
        try {
            return NotificationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 알림 타입: " + type);
        }
    }
}