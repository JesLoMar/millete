package com.puntomartinez.millete.groupgoals.infrastructure.in.controller;

import com.puntomartinez.millete.groupgoals.domain.ports.in.ResetGoalContributionsUseCase;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GroupGoalResetEndpointTest {
    @Mock private ResetGoalContributionsUseCase resetUseCase;
    @InjectMocks private GroupGoalController controller;

    @Test
    void acceptsDeleteOfGoalMovements() throws Exception {
        var goalId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var user = new JwtUser(userId, "admin", "admin@example.test");
        var authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());

        MockMvcBuilders.standaloneSetup(controller).build()
                .perform(delete("/api/v1/goals/{goalId}/contributions", goalId)
                        .principal(authentication))
                .andExpect(status().isNoContent());
        verify(resetUseCase).resetContributions(goalId, userId);
    }
}
