package xyz.kail.demo.spring.context.common;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import xyz.kail.demo.spring.context.common.service.PropertiesService;

@ComponentScan
public class AppMain {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppMain.class);

        PropertiesService propertiesService = context.getBean(PropertiesService.class);

        propertiesService.print();
    }

}
