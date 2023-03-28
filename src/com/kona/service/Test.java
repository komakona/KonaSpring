package com.kona.service;

import com.kona.service.AppConfig;
import com.kona.service.UserService;
import com.kona.spring.KonaApplicationContext;

public class Test {
    public static void main(String[] args) {

        KonaApplicationContext konaApplicationContext = new KonaApplicationContext(AppConfig.class);

//        UserService userService = (UserService) konaApplicationContext.getBean("userService");
//
//        userService.getOrderService();
//
//        System.out.println(userService);
//
//        System.out.println(userService.getBeanName());
//        System.out.println(konaApplicationContext.getBean("userService"));
//        System.out.println(konaApplicationContext.getBean("userService"));
//        System.out.println(konaApplicationContext.getBean("userService"));

        UserInterface userService = (UserInterface) konaApplicationContext.getBean("userService");
        System.out.println(userService);
        userService.getOrderService();

    }
}
