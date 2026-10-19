package kz.iitu.springlab.aspect;

import kz.iitu.springlab.audit.Audited;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@Order(1)
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        if (audited.logArguments()) {
            log.info("[AUDIT] start {} at {} args={}", audited.action(), LocalDateTime.now(),
                    ArgumentMasker.maskedArgs(pjp));
        } else {
            log.info("[AUDIT] start {} at {}", audited.action(), LocalDateTime.now());
        }
        try {
            Object result = pjp.proceed();
            log.info("[AUDIT] success {}", audited.action());
            return result;
        } catch (Throwable ex) {
            log.warn("[AUDIT] failure {}: {}", audited.action(), ex.getMessage());
            throw ex;
        }
    }
}
