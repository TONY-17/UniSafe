package com.backend.escort.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class EntryPoint implements AuthenticationEntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(EntryPoint.class);
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse res, AuthenticationException auth) throws IOException {
        logger.error(
                "USER NOT AUTHORIZED ERROR: {}",
                auth.getMessage()
        );
        res.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType(
                MediaType.APPLICATION_JSON_VALUE);

        final Map<String,Object> map = new HashMap<>();
        map.put("STATUS CODE", HttpServletResponse.SC_UNAUTHORIZED);
        map.put("MESSAGE", auth.getMessage());
        map.put("ROUTE", request.getServletPath());
        map.put("ERROR", "NOT AUTHORIZED");

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(
                res.getOutputStream(),
                map
        );
    }

    public Map<String,Object> getMap(HttpServletRequest request, HttpServletResponse res, AuthenticationException auth){
        Map<String,Object> map = new HashMap<>();
        map.put("STATUS CODE", HttpServletResponse.SC_UNAUTHORIZED);
        map.put("MESSAGE", auth.getMessage());
        map.put("ROUTE", request.getServletPath());
        map.put("ERROR", "NOT AUTHORIZED");
        return map;
    }

}
