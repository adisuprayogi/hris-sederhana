package com.hris.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug-login")
public class LoginDebugController {

    @PostMapping("/preview")
    public Map<String, Object> previewLogin(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        result.put("email", email);
        result.put("email_length", email != null ? email.length() : 0);
        result.put("email_hex", email != null ? bytesToHex(email.getBytes()) : null);
        result.put("password", password != null ? "(hidden)" : null);
        result.put("password_length", password != null ? password.length() : 0);
        result.put("password_hex", password != null ? bytesToHex(password.getBytes()) : null);
        result.put("content_type", request.getContentType());
        result.put("character_encoding", request.getCharacterEncoding());
        
        return result;
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
