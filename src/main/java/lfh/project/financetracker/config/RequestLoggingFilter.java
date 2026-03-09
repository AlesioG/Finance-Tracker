package lfh.project.financetracker.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lfh.project.financetracker.entity.User;
import lfh.project.financetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString();

        try {
            MDC.put("requestId", requestId);
            MDC.put("method", request.getMethod());
            MDC.put("path", request.getRequestURI());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null
                    && authentication.isAuthenticated()
                    && authentication.getName() != null
                    && !"anonymousUser".equals(authentication.getName())) {

                String email = authentication.getName();
                MDC.put("userEmail", email);

                Optional<User> userOptional = userRepository.findByEmail(email);
                userOptional.ifPresent(user -> MDC.put("userId", String.valueOf(user.getId())));
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}