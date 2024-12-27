package com.olelllka.stories_service.configuration;

import com.olelllka.stories_service.service.SHA256;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Component
public class SHA256KeyGenerator implements KeyGenerator {


    @Override
    public Object generate(Object target, Method method, Object... params) {
        Object obj = Arrays.stream(params).findFirst().orElse("withoutParams");
        String key = obj.toString();
        return SHA256.generate(key);
    }
}
