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

    public static MemberException userNotFoundByEmail(String email) {
        return new MemberException("MEMBER_005", "존재하지 않는 사용자입니다");
    }

    public static MemberException invalidPassword() {
        return new MemberException("MEMBER_006", "비밀번호가 일치하지 않습니다");
    }

    public static MemberException socialUserCannotUsePasswordLogin() {
        return new MemberException("MEMBER_007", "소셜 로그인 사용자는 비밀번호 로그인을 사용할 수 없습니다");
    }

    public static MemberException passwordNotSet() {
        return new MemberException("MEMBER_008", "비밀번호가 설정되지 않은 사용자입니다");
    }

    public static MemberException invalidRefreshToken() {
        return new MemberException("MEMBER_009", "유효하지 않은 리프레시 토큰입니다");
    }

    public static MemberException expiredRefreshToken() {
        return new MemberException("MEMBER_010", "만료된 리프레시 토큰입니다");
    }
}