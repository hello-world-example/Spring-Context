package xyz.kail.demo.spring.context.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import xyz.kail.demo.spring.context.event.event.DemoEvent;

/**
 * @since 4.2
 */
@Slf4j
@Component
public class DemoListener {

    @EventListener
    public void demoEventHandler(DemoEvent event) {
        log.info("demoEventHandler, source:{}", event.getSource());
    }
}
