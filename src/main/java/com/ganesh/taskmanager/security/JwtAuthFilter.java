package com.ganesh.taskmanager.security;

import com.ganesh.taskmanager.entity.ResignationRequest;
import com.ganesh.taskmanager.entity.User;
import com.ganesh.taskmanager.repository.ResignationRepository;
import com.ganesh.taskmanager.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ResignationRepository resignationRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            final String identifier = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);

            User user = userRepository.findByEmailIgnoreCase(identifier)
                    .or(() -> userRepository.findByUsernameIgnoreCase(identifier))
                    .or(() -> userRepository.findByEmployeeCodeIgnoreCase(identifier))
                    .orElse(null);

            String principalEmail = identifier;
            if (user != null) {
                Optional<ResignationRequest> resignOpt = resignationRepository.findFirstByEmployeeAndStatusOrderByCreatedAtDesc(user, ResignationRequest.ResignationStatus.APPROVED);
                if (resignOpt.isPresent()) {
                    LocalDate lastDay = resignOpt.get().getApprovedLastDay() != null ? resignOpt.get().getApprovedLastDay() : resignOpt.get().getProposedLastDay();
                    if (lastDay != null && LocalDate.now().isAfter(lastDay)) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"timestamp\":\"" + java.time.LocalDateTime.now() + "\",\"status\":401,\"code\":\"ACCOUNT_EXPIRED\",\"message\":\"Account Deactivated: Your notice period and last working day (" + lastDay + ") was completed.\"}");
                        return;
                    }
                }

                if (role == null || role.trim().isEmpty()) {
                    role = user.getRole().name();
                }
                principalEmail = user.getEmail(); // Ensure canonical email in SecurityContext
            } else if (role == null || role.trim().isEmpty()) {
                throw new RuntimeException("User not found");
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principalEmail,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"timestamp\":\"" + java.time.LocalDateTime.now() + "\",\"status\":401,\"code\":\"UNAUTHORIZED\",\"message\":\"Invalid or expired JWT token\",\"path\":\"" + request.getRequestURI() + "\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}