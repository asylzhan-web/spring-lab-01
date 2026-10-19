package kz.iitu.springlab.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// Variant 4: argument masking. For methods with @Sensitive parameters it logs the arguments with those values replaced by ***
@Aspect
@Component
@Order(0)
public class MaskingAspect {

    private static final Logger log = LoggerFactory.getLogger(MaskingAspect.class);

    @Before("kz.iitu.springlab.aspect.Pointcuts.serviceOperation()")
    public void mask(JoinPoint jp) {
        if (ArgumentMasker.hasSensitive(jp)) {
            log.info("[MASK] -> {} args={}", jp.getSignature().toShortString(), ArgumentMasker.maskedArgs(jp));
        }
    }
}
