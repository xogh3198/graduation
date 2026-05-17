package com.project.graduation.service.auth;

import com.project.graduation.domain.user.User;
import com.project.graduation.domain.user.UserRepository;
import com.project.graduation.dto.auth.AuthResponse;
import com.project.graduation.dto.auth.LoginRequest;
import com.project.graduation.dto.auth.PasswordResetRequest;
import com.project.graduation.dto.auth.RegisterRequest;
import com.project.graduation.exception.ApiException;
import com.project.graduation.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setNickname(request.getNickname());
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return new AuthResponse(
                jwtTokenProvider.createAccessToken(user.getId(), user.getEmail()),
                jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail())
        );
    }

    public void resetPassword(PasswordResetRequest request) {
        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "등록되지 않은 이메일입니다."));
        log.info("비밀번호 재설정 메일 발송 요청: {}", request.getEmail());
    }
}
