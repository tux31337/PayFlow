package com.truvis.user.api;

import com.truvis.common.response.ApiResponse;
import com.truvis.user.application.UserService;
import com.truvis.user.model.SignUpRequest;
import com.truvis.user.model.SignUpResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 회원가입 API
     * POST /api/user/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
            @Valid @RequestBody SignUpRequest request) {

        log.info("회원가입 요청: email={}, name={}", request.getEmail(), request.getName());

        SignUpResponse response = userService.signUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "회원가입이 완료되었습니다"));
    }

    @GetMapping("/test")
    public String test() {
        return "Hello, this is a test API!";
    }
}
