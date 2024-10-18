package com.carsharingapp.security;

import com.carsharingapp.dto.user.UserLoginRequestDto;
import com.carsharingapp.dto.user.UserLoginResponseDto;
import com.carsharingapp.exception.EntityNotFoundException;
import com.carsharingapp.exception.LoginException;
import com.carsharingapp.messages.ExceptionMessages;
import com.carsharingapp.model.Role;
import com.carsharingapp.model.User;
import com.carsharingapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public UserLoginResponseDto authenticate(UserLoginRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.getEmail()).orElseThrow(
                () -> new EntityNotFoundException(
                        ExceptionMessages.USER_DO_NOT_REGISTERED_EXCEPTION)
        );
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestDto.getEmail(),
                            requestDto.getPassword()));
        } catch (BadCredentialsException e) {
            throw new LoginException(ExceptionMessages.INVALID_PASSWORD_EXCEPTION, e);
        } catch (AuthenticationException e) {
            throw new LoginException(ExceptionMessages.AUTHENTICATION_FAILURE_EXCEPTION, e);
        }
        String jwt = jwtUtil.generateToken(user);
        UserLoginResponseDto responseDto = new UserLoginResponseDto();
        responseDto.setToken(jwt);
        return responseDto;
    }

    public boolean telegramAdminsAuthenticate(String email, String password, Long chatId)
            throws AuthenticationException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        User user = (User) authentication.getPrincipal();
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.ROLE_MANAGER);
        if (isAdmin) {
            user.setTgChatId(chatId);
            userRepository.save(user);
        }
        return isAdmin;
    }
}
