package com.credshield.credshield.config;

import com.credshield.credshield.entity.User;
import com.credshield.credshield.repository.UserRepository;
import com.credshield.credshield.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        // Temporary debugging logs
        System.out.println("REQUEST: " + request.getRequestURI());
        System.out.println("AUTH HEADER PRESENT: "
                + (authorizationHeader != null));

        if (authorizationHeader != null
                && authorizationHeader.startsWith("Bearer ")) {

            String token = authorizationHeader.substring(7);

            try {
                String email = jwtService.extractEmail(token);

                System.out.println("TOKEN EMAIL: " + email);

                if (SecurityContextHolder.getContext()
                        .getAuthentication() == null) {

                    User user = userRepository.findByEmail(email)
                            .orElse(null);

                    System.out.println("USER ROLE: "
                            + (user == null
                            ? "USER NOT FOUND"
                            : user.getRole()));

                    if (user != null
                            && user.getRole() != null
                            && jwtService.isTokenValid(token, email)) {

                        SimpleGrantedAuthority authority =
                                new SimpleGrantedAuthority(
                                        "ROLE_" + user.getRole().name()
                                );

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        email,
                                        null,
                                        List.of(authority)
                                );

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource()
                                        .buildDetails(request)
                        );

                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication);

                        System.out.println("AUTHENTICATION SUCCESSFUL");
                    }
                }

            } catch (io.jsonwebtoken.JwtException
                     | IllegalArgumentException ex) {

                System.out.println("JWT ERROR: " + ex.getMessage());

                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}