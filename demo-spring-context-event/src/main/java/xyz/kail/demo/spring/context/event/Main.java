package xyz.kail.demo.spring.context.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.event.TransactionalEventListener;
import xyz.kail.demo.spring.context.event.config.AppConfig;
import xyz.kail.demo.spring.context.event.event.ConditionEvent;
import xyz.kail.demo.spring.context.event.event.DemoEvent;
import xyz.kail.demo.spring.context.event.event.StatusEvent;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Configuration
@ComponentScan(basePackageClasses = Main.class)
public class Main {

    public static void main(String[] args) throws InterruptedException {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Main.class);

        // 发布事件
        context.publishEvent(new DemoEvent("demo:" + new Date()));

        // 发布 - 状态事件
        context.publishEvent(new StatusEvent(300, "status:" + new Date()));
        context.publishEvent(new StatusEvent(400, "status:" + new Date()));
        context.publishEvent(new StatusEvent(500, "status:" + new Date()));

        // 发布 - 条件事件
        context.publishEvent(new ConditionEvent("condition:" + new Date()));

        // 针对事务的事件处理
        final Class<TransactionalEventListener> transactionalEventListenerClass = TransactionalEventListener.class;

        TimeUnit.SECONDS.sleep(10);

    }

}
