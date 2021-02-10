# &lt;context: load-time-weaver/&gt;

标签解析器： `org.springframework.context.config.LoadTimeWeaverBeanDefinitionParser` , @since 2.5

## 主要功能

在Java 语言中，从织入切面的方式上来看，存在三种织入方式：编译期织入、类加载期织入 和 运行期织入。

- **编译期织入** 是指在Java编译期，采用特殊的编译器，将切面织入到Java类中
- **类加载期织入** 则指通过 **特殊的类加载器** 或 **javaagent 机制**，在类字节码加载到JVM时，织入切面
- **运行期织入** 则是采用CGLib工具或JDK动态代理进行切面的织入。

load-time-weaver 就是 采用 **类加载期织入** 的方式。

- 如果程序运行在 Web 容器中，可能是通过 Web 容器 ClassLoader 对外提供的接口，在类加载到内存的时候修改类的字节码文件
- 如果 是在 Spring 没有支持 的 LTW Web 容器中、或者非 Web 容器，可以通过 Spring 提供 Agent `spring-instrument` 通过 `-javaagent` 的方式启动来支持 **类加载期织入**
- 还可以通过 指定自定义的 ClassLoader 在获取类字节码的时候 织入 代码。

 

## 使用方式

```xml
<context:load-time-weaver 
        aspectj-weaving="on|off|autodetect" 
        weaver-class="默认：org.springframework.context.weaving.DefaultContextLoadTimeWeaver"
/>
```



## LoadTimeWeaverBeanDefinitionParser.doParse 伪代码

```java
if( aspectj-weaving == on || (aspectj-weaving == autodetect && exist("META-INF/aop.xml")) ){
    // internalAspectJWeavingEnabler，需要 org.aspectj:aspectjweaver 依赖
    if( "org.springframework.context.config.internalAspectJWeavingEnabler" 还没有注册过 ){
        注册 "org.springframework.context.weaving.AspectJWeavingEnabler"
        名字 "org.springframework.context.config.internalAspectJWeavingEnabler"
    }
    
    // 同 <context:spring-configured/>，需要 org.springframework:spring-aspects 依赖，间接依赖 org.aspectj:aspectjweaver
    if( "org.springframework.beans.factory.aspectj.AnnotationBeanConfigurerAspect" 有这个类){
        // internalBeanConfigurerAspect
		if( "org.springframework.context.config.internalBeanConfigurerAspect" 还没有注册过 ){
            注册 "org.springframework.beans.factory.aspectj.AnnotationBeanConfigurerAspect"
            名字 "org.springframework.context.config.internalBeanConfigurerAspect"
  		}
    }
}
```



## DefaultContextLoadTimeWeaver

`DefaultContextLoadTimeWeaver` 是 `LoadTimeWeaver` 接口各种实现 的适配器。

**LoadTimeWeaver 实现 适配过程 伪代码**

```java
LoadTimeWeaver serverSpecificLoadTimeWeaver = {
    获取 Web 容器 相关的 LoadTimeWeaver，从 Spring 4.0 开始，支持：
        Oracle WebLogic 10
        GlassFish 3
        Tomcat 6, 7 and 8
        JBoss AS 5, 6 and 7
        IBM WebSphere 7 and 8
}
if(null != serverSpecificLoadTimeWeaver){
    // 部分 Web 容器，支持 LTW
    使用 Web 容器 相关的 LoadTimeWeaver
} else if( 是否已 -javaagent:spring-instrument.jar 启动 ) {
    // 传统的 javaagent 的方式 修改字节码
    使用 InstrumentationLoadTimeWeaver
} else {
    // 通过反射自定义 ClassLoader， 在 ClassLoader 加载类的时候，可以获取字节码 进行修改
    使用 ReflectiveLoadTimeWeaver(自定义 ClassLoader ， 如 SimpleInstrumentableClassLoader )
}
```



- ClassLoader (java.lang)
  - DecoratingClassLoader (org.springframework.core)
    - OverridingClassLoader (org.springframework.core)
      - SimpleInstrumentableClassLoader (org.springframework.instrument.classloading)



## Read More

- [Chapter 5. Load-Time Weaving](http://www.eclipse.org/aspectj/doc/released/devguide/ltw.html)

- [6.8.4. 在Spring应用中使用AspectJ加载时织入（LTW）](http://shouce.jb51.net/spring/aop.html#aop-aj-ltw)