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

    public static MemberException emailUserCannotHaveSocialProvider() {
        return new MemberException("MEMBER_011", "이메일 가입 사용자는 소셜 프로바이더를 가질 수 없습니다.");
    }

    public static MemberException socialUserCannotHavePassword() {
        return new MemberException("MEMBER_012", "소셜 가입 사용자는 비밀번호를 가질 수 없습니다.");
    }

    public static MemberException socialAccountAlreadyExists(String provider) {
        return new MemberException("MEMBER_013", provider + " 계정이 이미 연동되어 있습니다.");
    }

    public static MemberException emailAlreadyExistsWithDifferentSignUpType(String email, String existingType) {
        return new MemberException("MEMBER_014",
                String.format("이메일 %s는 이미 %s 방식으로 가입되어 있습니다.",
                        email, existingType)
        );
    }

    public static MemberException invalidSocialAccount() {
        return new MemberException("MEMBER_015", "유효하지 않은 소셜 계정 정보입니다.");
    }

    public static MemberException unsupportedSocialProvider(String provider) {
        return new MemberException("MEMBER_016", "지원하지 않는 소셜 프로바이더입니다: " + provider);
    }
}