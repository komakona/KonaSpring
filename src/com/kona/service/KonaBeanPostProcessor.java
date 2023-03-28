package com.kona.service;

import com.kona.spring.BeanPostProcessor;
import com.kona.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class KonaBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessorBeforeInitialization(String beanName, Object instance) {
        if("userService".equals(beanName)){
            System.out.println("进入初始化前置方法！");
        }

        return null;
    }

    @Override
    public Object postProcessorAfternitialization(String beanName, Object instance) {

        System.out.println("进入初始化后置方法！");

        if("userService".equals(beanName)){
            Object proxyInstance = Proxy.newProxyInstance(instance.getClass().getClassLoader(), instance.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("进入切面！");
                    return method.invoke(instance, args);
                }
            });

            return proxyInstance;

        }
        return instance;
    }
}
