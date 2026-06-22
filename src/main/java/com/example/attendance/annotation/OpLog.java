package com.example.attendance.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解 —— 贴在 Controller 方法上，自动记录操作日志
 *
 * 用法示例：
 *   @OpLog("删除学生")
 *   @GetMapping("/student/delete/{id}")
 *   public String delete(@PathVariable Integer id) { ... }
 *
 * 原理：
 * 这个注解本身不干活，它只是一个"标记"。
 * 真正干活的是 OpLogAspect（切面类），它会扫描所有贴了 @OpLog 的方法，
 * 在方法执行前后自动插入日志记录代码。
 * 业务代码完全不需要知道日志的存在 —— 这就是 AOP 的核心价值：解耦。
 */
@Target(ElementType.METHOD)    // 只能贴在方法上
@Retention(RetentionPolicy.RUNTIME)  // 运行时保留，否则 Spring 读不到
public @interface OpLog {
    String value() default "";  // 操作描述，比如"新增学生"
}
