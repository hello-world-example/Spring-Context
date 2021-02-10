package xyz.kail.demo.spring.context.event.event;

import org.springframework.context.ApplicationEvent;

public class DemoEvent extends ApplicationEvent {

    public DemoEvent(Object source) {
        super(source);
    }

}
