package com.kona.spring;

import com.sun.xml.internal.ws.util.StringUtils;

import java.beans.Introspector;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class KonaApplicationContext {

    private Class configClass;

    private ConcurrentHashMap<String, BeanDefintion> beanDefinitionConcurrentHashMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();

    private ArrayList<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public KonaApplicationContext(Class configClass) {
        this.configClass = configClass;

        //扫描
        if(configClass.isAnnotationPresent(ComponentScan.class)){
            ComponentScan componentScan = (ComponentScan) configClass.getAnnotation(ComponentScan.class);

            String packagePath = componentScan.value();
            packagePath = packagePath.replace(".", "/");

            ClassLoader classLoader = KonaApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(packagePath);

            File file = new File(resource.getFile());

            if(file.isDirectory()){

                File[] files = file.listFiles();

                for(File f : files){
                    String fileName =  f.getAbsolutePath();

                    if(fileName.endsWith(".class")){

                        String classPath = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                        classPath = classPath.replace("/", ".");

                        try {
                            Class<?> beanClass = classLoader.loadClass(classPath);

                            if(beanClass.isAnnotationPresent(Component.class)){

                                if(BeanPostProcessor.class.isAssignableFrom(beanClass)){
                                    BeanPostProcessor instance = null;

                                    instance = (BeanPostProcessor) beanClass.newInstance();

                                    beanPostProcessorList.add(instance);
                                }
//                                beanClass.get

                                Component componentAnnotation = beanClass.getAnnotation(Component.class);
                                String componentValue = componentAnnotation.value();
                                if("".equals(componentValue)){
                                    componentValue = Introspector.decapitalize(beanClass.getSimpleName());
                                }

                                BeanDefintion beanDefintion = new BeanDefintion();
                                beanDefintion.setType(beanClass);
                                if(beanClass.isAnnotationPresent(Scope.class)){
                                    Scope scopeAnnotation = beanClass.getAnnotation(Scope.class);
                                    beanDefintion.setScope(scopeAnnotation.value());
                                }else {
                                    beanDefintion.setScope("singleton");
                                }
                                beanDefinitionConcurrentHashMap.put(componentValue, beanDefintion);

                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }   catch (InstantiationException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            }
            
        }

        //创建
        for (String beanName : beanDefinitionConcurrentHashMap.keySet()) {

            BeanDefintion beanDefintion = beanDefinitionConcurrentHashMap.get(beanName);
            if("singleton".equalsIgnoreCase(beanDefintion.getScope())){
                Object bean = createBean(beanName, beanDefintion);
                singletonObjects.put(beanName, bean);
            }

        }

    }

    private Object createBean (String beanName, BeanDefintion beanDefintion){

        Class clazz = beanDefintion.getType();
        try {

            Object instance = clazz.getConstructor().newInstance();

            //依赖注入
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                if(declaredField.isAnnotationPresent(Autowired.class)){
                    declaredField.setAccessible(true);
                    declaredField.set(instance, getBean(declaredField.getName()));
                }
            }

            //aware
            if(instance instanceof BeanNameAware){
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            //初始化前
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.postProcessorBeforeInitialization(beanName, instance);
            }

            //初始化 和aware不同的是spring调用aware时会把容器中的某些东西给到bean，初始化的时候只是调用方法，不给任何东西
            if(instance instanceof InitializingBean){
                ((InitializingBean) instance).afterPropertiesSet();
            }

            //初始化后
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessorAfternitialization(beanName, instance);
            }

            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getBean(String beanName){

        BeanDefintion beanDefintion = beanDefinitionConcurrentHashMap.get(beanName);

        if(null == beanDefintion){
            throw new NullPointerException();
        }else {

            if("singleton".equalsIgnoreCase(beanDefintion.getScope())){
                Object bean = singletonObjects.get(beanName);
                if(null == bean){
                    bean = createBean(beanName, beanDefintion);
                    singletonObjects.put(beanName, bean);
                }
                return bean;
            }else {
                return createBean(beanName, beanDefintion);
            }

        }

    }

}
