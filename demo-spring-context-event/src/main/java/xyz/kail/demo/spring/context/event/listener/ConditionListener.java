package xyz.kail.demo.spring.context.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import xyz.kail.demo.spring.context.event.event.ConditionEvent;

/**
 * @since 4.2
 */
@Slf4j
@Component
@Deprecated
public class ConditionListener {

    @EventListener(condition = "@conditionListener.test(#event)")
    public void statusEventHandler(ConditionEvent event) {
        log.info("status500EventHandler, source:{}", event.getSource());
    }

    /**
     * 针对事件进行一些复杂的逻辑判断
     */
    public boolean test(ConditionEvent event) {
        return null != event && null != event.getSource();
    }

}
