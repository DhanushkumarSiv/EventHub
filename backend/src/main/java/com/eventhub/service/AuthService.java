package com.eventhub.service;

import com.eventhub.dto.request.LoginRequest;
import com.eventhub.dto.request.RegisterRequest;
import com.eventhub.dto.response.AuthResponse;
import com.eventhub.dto.response.UserSummaryResponse;
import com.eventhub.security.UserPrincipal;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserSummaryResponse getCurrentUser(UserPrincipal principal);
}
