package com.projectexpress.app.security;

import com.projectexpress.app.service.CustomUserDetailsService;
import com.projectexpress.app.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if(header == null || !header.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        String email;

        try {
            email = jwtService.extractEmail(token);
        } catch (RuntimeException e) {
            filterChain.doFilter(request, response);
            return;
        }

        if(email != null && SecurityContextHolder.getContext().getAuthentication() ==null){
            try {
                // CHECK THE DATABASE: Load the user by the email extracted from the token
                UserDetails user = customUserDetailsService.loadUserByUsername(email);
                //Verify token is still valid for this specific user
                if(jwtService.isTokenValid(token, user)){
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }catch(UsernameNotFoundException e){
                filterChain.doFilter(request, response);
                return;
            }
        }
        filterChain.doFilter(request, response);

    }
}
