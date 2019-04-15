package com.wei.demo.aspect;

import com.wei.demo.annotation.CountVisitNum;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * @author weiwenfeng
 * @date 2019/4/15
 *
 * 统计接口访问次数
 */
@Aspect
@Component
public class CountVisitNumAspect {

    private static final Logger logger = LoggerFactory.getLogger(CountVisitNumAspect.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Before("execution(* com.wei.demo.controller ..*(..) )")
    public void countVisitNum(JoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CountVisitNum countVisitNum = method.getAnnotation(CountVisitNum.class);
        if (null != countVisitNum) {
            String key = countVisitNum.key();
            RedisAtomicInteger entityIdCounter = new RedisAtomicInteger(
                    "interface:visit:" + key, redisTemplate.getConnectionFactory());

            String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS");

            logger.info(date + " 累计访问次数：" + entityIdCounter.incrementAndGet());
        }
    }
}
