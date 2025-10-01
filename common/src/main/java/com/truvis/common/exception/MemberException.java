package com.truvis.common.exception;

public class MemberException extends BusinessException {

    public MemberException(String message) {
        super("MEMBER_ERROR", message);
    }

    public MemberException(String errorCode, String message) {
        super(errorCode, message);
    }

    // 정적 팩토리 메서드들 (편의성)
    public static MemberException emailAlreadyExists(String email) {
        return new MemberException("MEMBER_001", "이미 가입된 이메일입니다: " + email);
    }

    public static MemberException emailNotVerified() {
        return new MemberException("MEMBER_002", "이메일 인증이 완료되지 않았습니다");
    }

    public static MemberException passwordNotMatched() {
        return new MemberException("MEMBER_003", "비밀번호가 일치하지 않습니다");
    }

    public static MemberException userNotFound(Long userId) {
        return new MemberException("MEMBER_004", "사용자를 찾을 수 없습니다: " + userId);
    }
}