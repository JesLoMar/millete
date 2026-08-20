package com.puntomartinez.millete.users.application.services;
import com.puntomartinez.millete.users.domain.exception.AccountLockedException;
import com.puntomartinez.millete.users.domain.model.UserLoginSecurity;
import com.puntomartinez.millete.users.domain.ports.out.LoginSecurityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
@Service
public class AccountLockService {
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;
    private final LoginSecurityRepository loginSecurityRepository;
    public AccountLockService(LoginSecurityRepository loginSecurityRepository) {
        this.loginSecurityRepository = loginSecurityRepository;
    }
    @Transactional
    public void checkLockStatus(UUID userId) {
        loginSecurityRepository.findByUserId(userId).ifPresent(security -> {
            boolean hadBlock = security.getBlockedUntil() != null;
            if (security.isBlocked()) {
                throw new AccountLockedException(
                        security.getBlockedUntil(),
                        calculateRemainingMinutes(security.getBlockedUntil()));
            }
            if (hadBlock) {
                loginSecurityRepository.save(security);
            }
        });
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = AccountLockedException.class)
    public void handleFailedLogin(UUID userId) {
        UserLoginSecurity security = getOrCreate(userId);
        security.registerFailedAttempt(MAX_ATTEMPTS, LOCK_DURATION_MINUTES);
        security = loginSecurityRepository.save(security);
        if (security.isBlocked()) {
            throw new AccountLockedException(
                    security.getBlockedUntil(),
                    calculateRemainingMinutes(security.getBlockedUntil()));
        }
    }
    @Transactional
    public void handleSuccessfulLogin(UUID userId) {
        loginSecurityRepository.findByUserId(userId).ifPresent(security -> {
            boolean dirty = security.getFailedAttempts() > 0 || security.getBlockedUntil() != null;
            if (dirty) {
                security.resetAttempts();
                loginSecurityRepository.save(security);
            }
        });
    }
    private UserLoginSecurity getOrCreate(UUID userId) {
        return loginSecurityRepository.findByUserId(userId).orElseGet(() -> {
            UserLoginSecurity security = new UserLoginSecurity();
            security.setUserId(userId);
            security.setFailedAttempts(0);
            security.setCreatedAt(LocalDateTime.now());
            security.setModifiedAt(LocalDateTime.now());
            return security;
        });
    }
    private long calculateRemainingMinutes(LocalDateTime blockedUntil) {
        if (blockedUntil == null) {
            return LOCK_DURATION_MINUTES;
        }
        long seconds = Duration.between(LocalDateTime.now(), blockedUntil).getSeconds();
        return Math.max(0, (seconds + 59) / 60);
    }
}