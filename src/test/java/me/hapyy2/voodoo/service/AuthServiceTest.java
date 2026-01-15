package me.hapyy2.voodoo.service;

import me.hapyy2.voodoo.dto.RegisterDto;
import me.hapyy2.voodoo.exception.BaseException;
import me.hapyy2.voodoo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private AuthService authService;

    @Test
    void shouldRegisterNewUser() {
        RegisterDto dto = new RegisterDto();
        dto.setUsername("newuser");
        dto.setPassword("pass");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");

        authService.register(dto);

        verify(userRepository).save(any());
    }

    @Test
    void shouldThrowExceptionIfUsernameExists() {
        RegisterDto dto = new RegisterDto();
        dto.setUsername("existing");

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(BaseException.class, () -> authService.register(dto));
    }
}