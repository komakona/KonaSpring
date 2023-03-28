package com.kona.spring;

public interface BeanPostProcessor {

    public Object postProcessorBeforeInitialization(String beanName, Object instance);
    public Object postProcessorAfternitialization(String beanName, Object instance);
}
