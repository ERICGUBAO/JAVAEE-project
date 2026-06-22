package com.example.attendance.aspect;

import com.example.attendance.annotation.OpLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 操作日志切面 —— AOP 的核心实现
 *
 * 原理图解（以删除学生为例）：
 *
 *   用户点击"删除"
 *       │
 *       ▼
 *   ┌─────────────────────────────┐
 *   │  OpLogAspect（切面）          │  ← 拦截
 *   │  ① 记录：谁在什么时间调了哪个方法、参数是什么
 *   │  ② joinPoint.proceed()  →  执行真正的 Controller 方法
 *   │  ③ 记录：方法执行完了，耗时多少 ms
 *   └─────────────────────────────┘
 *       │
 *       ▼
 *   StudentController.delete()     ← 业务代码，一行日志都不用写
 *
 * 关键术语：
 *   - 切面（Aspect）    = OpLogAspect 这个类
 *   - 切入点（Pointcut） = 所有贴了 @OpLog 注解的方法
 *   - 通知（Advice）     = @Around 注解的方法，在方法前后执行
 *   - 连接点（JoinPoint）= 被拦截的具体方法
 */
@Aspect       // ← 告诉 Spring：这是个切面类
@Component    // ← 交给 Spring 管理
public class OpLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OpLogAspect.class);

    /**
     * @Around 环绕通知：方法执行前和执行后各插一脚
     *
     * "@annotation(opLog)" 的意思是：
     *   拦截所有贴了 @OpLog 注解的方法，把注解对象传给参数 opLog
     */
    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint joinPoint, OpLog opLog) throws Throwable {

        // ===== 方法执行前 =====
        String className = joinPoint.getTarget().getClass().getSimpleName();  // 哪个类
        String methodName = joinPoint.getSignature().getName();               // 哪个方法
        Object[] args = joinPoint.getArgs();                                  // 入参

        log.info("【{}】开始执行 → {}.{}()  参数: {}",
                opLog.value(), className, methodName, Arrays.toString(args));

        long startTime = System.currentTimeMillis();

        // ===== 执行真正的业务方法 =====
        Object result;
        try {
            result = joinPoint.proceed();   // ← 这一行就是调 StudentController.delete()
        } catch (Throwable e) {
            // ===== 方法抛异常 =====
            log.error("【{}】执行失败 → {}.{}()  异常: {}  耗时: {}ms",
                    opLog.value(), className, methodName, e.getMessage(),
                    System.currentTimeMillis() - startTime);
            throw e;  // 继续往外抛，交给 GlobalExceptionHandler 处理
        }

        // ===== 方法执行后 =====
        long elapsed = System.currentTimeMillis() - startTime;
        log.info("【{}】执行成功 → {}.{}()  耗时: {}ms  返回: {}",
                opLog.value(), className, methodName, elapsed, result);

        return result;
    }
}
