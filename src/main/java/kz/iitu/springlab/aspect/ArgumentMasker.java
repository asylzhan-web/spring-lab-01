package kz.iitu.springlab.aspect;

import kz.iitu.springlab.audit.Sensitive;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

// Variant 4: replaces the values of @Sensitive parameters with "***"
final class ArgumentMasker {

    static final String MASK = "***";

    private ArgumentMasker() {
    }

    static List<Object> maskedArgs(JoinPoint jp) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        Annotation[][] annotations = signature.getMethod().getParameterAnnotations();
        Object[] args = jp.getArgs();
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            result.add(isSensitive(annotations[i]) ? MASK : args[i]);
        }
        return result;
    }

    static boolean hasSensitive(JoinPoint jp) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        for (Annotation[] parameter : signature.getMethod().getParameterAnnotations()) {
            if (isSensitive(parameter)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSensitive(Annotation[] parameterAnnotations) {
        for (Annotation a : parameterAnnotations) {
            if (a instanceof Sensitive) {
                return true;
            }
        }
        return false;
    }
}
