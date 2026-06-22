package com.example.attendance.handler;

import com.example.attendance.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器 —— 这就是 AOP 在实际项目中最常见的应用
 *
 * 原理：
 * 以前你每个 Controller 方法里都要 try-catch，100 个方法写 100 遍。
 * Spring 的 @RestControllerAdvice 可以在所有 Controller 外面包一层"拦截网"，
 * 任何 Controller 抛出的异常，先经过这里，统一处理、统一返回格式。
 * 业务代码里只需要 throw，不用管怎么包装错误信息。
 *
 * 面试话术：
 * "我用 @RestControllerAdvice + @ExceptionHandler 做了全局异常处理，
 * 业务代码只管抛异常，错误信息的组装和返回格式由这一个类统一负责。
 * 这本质就是 AOP——把横切的异常处理逻辑从业务代码里抽出来。"
 */
@RestControllerAdvice   // = @ControllerAdvice + @ResponseBody（返回 JSON）
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 忽略静态资源 404（如 favicon.ico），不打印日志
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFound(NoResourceFoundException e) {
        // 静默忽略，不打印任何日志
    }

    /**
     * 拦截所有 RuntimeException
     * 比如你在 Service 里写的 throw new RuntimeException("未找到记录")
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 拦截参数校验失败异常（@Valid 校验不通过时自动抛出）
     * 比如 @NotBlank 校验失败，把具体哪个字段错了告诉前端
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", msg);
        return Result.error(msg);
    }

    /**
     * 兜底：拦截所有没被上面捕获的异常
     * 防止用户看到 500 错误页面
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统内部错误，请联系管理员");
    }
}
