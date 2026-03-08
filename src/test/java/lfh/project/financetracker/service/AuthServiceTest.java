package lfh.project.financetracker.service;

import lfh.project.financetracker.dto.request.RegisterRequest;
import lfh.project.financetracker.entity.User;
import lfh.project.financetracker.repository.UserRepository;
import lfh.project.financetracker.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateUserAndReturnToken() {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(jwtService.generateToken("test@example.com")).thenReturn("jwtToken");

        var response = authService.register(request);

        assertEquals("jwtToken", response.getToken());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldFail_whenEmailAlreadyExists() {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );
    }
}