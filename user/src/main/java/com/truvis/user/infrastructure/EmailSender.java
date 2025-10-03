package com.truvis.user.infrastructure;

import com.truvis.common.exception.EmailVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailSender {
    
    @Autowired(required = false)  // 필수가 아니므로 Bean이 없어도 괜찮음
    private JavaMailSender mailSender;
    
    /**
     * 인증번호 이메일 전송
     */
    public void sendVerificationCodeEmail(String toEmail, String code) {
        
        if (mailSender == null) {
            // 개발 환경: JavaMailSender가 없으면 콘솔에 출력
            logEmailToConsole(toEmail, code);
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[Truvis] 이메일 인증번호입니다");
            message.setText(createVerificationCodeEmailContent(code));
            message.setFrom("noreply@truvis.com");
            
            mailSender.send(message);
            log.info("인증번호 이메일 전송 완료: {}", toEmail);
            
        } catch (Exception e) {
            log.error("인증번호 이메일 전송 실패: email={}, error={}", toEmail, e.getMessage());
            throw EmailVerificationException.emailSendFailed(toEmail);
        }
    }
    
    /**
     * 개발용 - 콘솔에 이메일 내용 출력
     */
    private void logEmailToConsole(String toEmail, String code) {
        String content = createVerificationCodeEmailContent(code);
        String subject = "[Truvis] 이메일 인증번호입니다";
        
        log.info("=== 개발용 이메일 전송 (콘솔 출력) ===");
        log.info("수신자: {}", toEmail);
        log.info("제목: {}", subject);
        log.info("내용:\n{}", content);
        log.info("=======================================");
    }
    
    /**
     * 인증번호 이메일 내용 생성
     */
    private String createVerificationCodeEmailContent(String code) {
        return String.format("""
            안녕하세요, Truvis입니다.
            
            회원가입을 위한 이메일 인증번호입니다.
            
            인증번호: %s
            
            위 인증번호를 입력하여 이메일 인증을 완료해주세요.
            
            ※ 이 인증번호는 10분 동안 유효합니다.
            ※ 본인이 요청하지 않은 이메일이라면 무시하셔도 됩니다.
            
            감사합니다.
            Truvis 팀
            """, code);
    }
}
