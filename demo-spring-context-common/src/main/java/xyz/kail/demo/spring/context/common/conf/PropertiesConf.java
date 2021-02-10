package xyz.kail.demo.spring.context.common.conf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

import java.io.IOException;

@Component
@Configuration
public class PropertiesConf {


    @Bean
    public PropertiesUtil propertiesUtil() {
        return new PropertiesUtil();
    }

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException {
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resourcePatternResolver.getResources("classpath:*.properties");

        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setLocations(resources);
        return configurer;
    }


    @Slf4j
    public static class PropertiesUtil implements EmbeddedValueResolverAware {

        private StringValueResolver resolver;

        public String get(String key) {
            try {
                return resolver.resolveStringValue("${" + key + "}");
            } catch (IllegalArgumentException e) {
                log.error("", e);
            }
            return null;
        }

        @Override
        public void setEmbeddedValueResolver(StringValueResolver resolver) {
            this.resolver = resolver;
        }
    }

}
