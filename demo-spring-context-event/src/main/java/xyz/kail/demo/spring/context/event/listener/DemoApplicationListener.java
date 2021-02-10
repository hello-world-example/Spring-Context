package xyz.kail.demo.spring.context.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import xyz.kail.demo.spring.context.event.event.DemoEvent;

@Slf4j
@Component
public class DemoApplicationListener implements ApplicationListener<DemoEvent> {

    @Override
    public void onApplicationEvent(DemoEvent event) {
        log.info("onApplicationEvent, source:{}", event.getSource());
    }
}
