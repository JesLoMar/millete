package com.puntomartinez.millete.users.domain.ports.in;

import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.model.UserSession;

public record AuthenticationResult(
        User user,
        UserSession session,
        String jwt
) {
}