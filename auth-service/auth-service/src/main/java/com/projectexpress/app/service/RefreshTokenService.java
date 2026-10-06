package com.projectexpress.app.service;

import com.projectexpress.app.dto.RefreshTokenResponse;
import com.projectexpress.app.entity.RefreshToken;
import com.projectexpress.app.entity.User;
import com.projectexpress.app.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenExpiration;

    //Create refresh token
    public RefreshToken createRefreshToken(User user){

        RefreshToken refreshToken = RefreshToken.builder()
                                    .token(UUID.randomUUID().toString())
                                    .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                                    .user(user)
                                    .build();

        return refreshTokenRepository.save(refreshToken);
    }

    //verify expiry date
    public RefreshToken verifyExpiration(RefreshToken refreshToken){
        if(refreshToken.getExpiryDate().isBefore(Instant.now())){
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token has expired");
        }

        return refreshToken;
    }

    //find by token
    public RefreshToken findByToken(String token) {

        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
    }

    //create new access & refresh token (refresh-token rotation)
    public RefreshTokenResponse refreshAccessToken(String token){
        RefreshToken oldRefreshToken = findByToken(token);

        verifyExpiration(oldRefreshToken);

        User user = oldRefreshToken.getUser();

        //old refresh token deleted
        refreshTokenRepository.delete(oldRefreshToken);

        //new refresh token is created
        RefreshToken newRefreshToken = createRefreshToken(user);
        //new access token is created
        String  accessToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());

        return new RefreshTokenResponse(accessToken, newRefreshToken.getToken());
    }


    //background work for logout
    public void logout(String token){
        RefreshToken refreshToken = findByToken(token);
        refreshTokenRepository.delete(refreshToken);
    }
}
