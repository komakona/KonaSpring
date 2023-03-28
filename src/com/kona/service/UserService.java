package com.kona.service;

import com.kona.spring.*;

@Component
public class UserService implements BeanNameAware, InitializingBean,UserInterface {

    @Autowired
    private OrderService orderService;

    public String getBeanName() {
        return beanName;
    }

    private String beanName;

    public void getOrderService(){
        System.out.println(orderService);
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }


    @Override
    public void afterPropertiesSet() {
        System.out.println("进来了初始化方法！");
    }
}
