package nmng108.microtube.processor.configuration;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@Aspect
@Slf4j
public class AopConfiguration {
    /**
     * An example of AOP feature usage.
     *
     * In reality, there's no need to always print log for every caught exception, especially relating to validation.
     * Instead, this would be better if we only put log to necessary handlers.
     */
    @Before("execution(* nmng108.microtube.processor.exception.handler.*.*(..))")
    public void beforeExceptionHandler(JoinPoint joinPoint) {
        Exception e = (Exception) joinPoint.getArgs()[0];
        log.info("(Handler method: {} - Exception: {}) {}", joinPoint.getSignature().toShortString(), e.getClass().getCanonicalName(), e.getMessage());
    }

    // Second style, separate Pointcut and Advice declaration
//    @Before(value = "beforeExceptionHandler(e)", argNames = "e")
//    public void before(Exception e) {
//        log.info("This log is from AOP");
//        log.info("({}) {}", e.getClass().getCanonicalName(), e.getMessage());
//    }

//    @Pointcut("@annotation(org.springframework.web.bind.annotation.ExceptionHandler) && args(e,..)")
//    private void beforeExceptionHandler(Exception e) {}

}
