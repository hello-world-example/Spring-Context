package xyz.kail.demo.spring.context.common.beandefinition;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;

public class BeanDefinitionBuilderMain {

    public static void main(String[] args) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(BeanDefinitionBuilderMain.class);

    }

}
