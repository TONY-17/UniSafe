package com.backend.escort.security.jwt;

import com.backend.escort.security.service.UserDetailsImp;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class JWT {
    private static final Logger logger = LoggerFactory.getLogger(JWT.class);

    // The following values come from application.properties
    @Value("${escort.app.secret}")
    private String secret;

    @Value("${escort.app.expiration}")
    private int expiration;

    @Value("${escort.app.cookie}")
    private String cookie;

    public String getJWT(HttpServletRequest req){
        Cookie c = WebUtils.getCookie(req,cookie);
        if(c != null){
            return c.getValue();
        }else{
            return null;
        }
    }

    public ResponseCookie generateJWT(UserDetailsImp user) {
        String jwt = generateTokeFromEmail(user.getEmail());
        return ResponseCookie.from(cookie, jwt).path("/api").maxAge(24 * 60 * 60).httpOnly(true).build();
    }

    public ResponseCookie clean() {
        return ResponseCookie.from(String.valueOf(ResponseCookie.from(cookie, null).path("/api").build()), null).path("/api").build();
    }

    public String getEmailFromCookie(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }


    public String generateTokeFromEmail(String email){
        Date exp = new Date(new Date().getTime() + expiration);
        return Jwts.builder()
                .setIssuedAt(new Date())
                .setSubject(email)
                .setExpiration(
                      exp
                )
                .signWith(SignatureAlgorithm.HS512,secret)
                .compact();
    }


    public boolean checkToken(String token){
        try {
            Jwts.parser().setSigningKey(
                    secret
            ).setSigningKey(
                    token
            );
            return true;
        }catch (SignatureException signatureException){
            logger.error(
                    "INVALID SIGNATURE: ", signatureException.getMessage()
            );
        }
        return false;
    }


}
