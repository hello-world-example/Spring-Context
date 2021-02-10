package xyz.kail.demo.spring.context.event.event;

import org.springframework.context.ApplicationEvent;

public class ConditionEvent extends ApplicationEvent {

    public ConditionEvent(Object source) {
        super(source);
    }
}
