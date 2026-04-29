package com.ttcs.menshop.oauth;

import com.ttcs.menshop.exception.AccountLockedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        String error = "google_login_failed";

        Throwable cause = exception.getCause();

        if (cause instanceof AccountLockedException) {
            error = "account_locked";
        }

        response.sendRedirect("http://localhost:3000/dang-nhap?error=" + error);
    }
}
