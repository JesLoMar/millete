package com.puntomartinez.millete.users.domain.ports.in;

import com.puntomartinez.millete.users.domain.model.User;
import java.util.UUID;

public interface GetUserDataUseCase {
    User getUserById(UUID id);
}