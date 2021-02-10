package xyz.kail.demo.spring.context.event.event;

import org.springframework.context.ApplicationEvent;

public class StatusEvent extends ApplicationEvent {

    private Integer status;

    public StatusEvent(Integer status, Object source) {
        super(source);
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }
}
