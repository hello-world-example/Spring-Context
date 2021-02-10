# ConfigurableApplicationContext.refresh



## 继承关系

- **ConfigurableApplicationContext** (org.springframework.**context**)
  - **ApplicationContext** (org.springframework.**context**)
    - EnvironmentCapable (org.springframework.**core**.env)
    - **ListableBeanFactory** (org.springframework.**beans**.factory)
      - **BeanFactory** (org.springframework.**beans**.factory)
    - **HierarchicalBeanFactory** (org.springframework.**beans**.factory)
      - **BeanFactory** (org.springframework.**beans**.factory)
    - MessageSource (org.springframework.**context**)
    - ApplicationEventPublisher (org.springframework.**context**)
    - ResourcePatternResolver (org.springframework.**core**.io.support)
      - ResourceLoader (org.springframework.**core**.io)
  - Lifecycle (org.springframework.**context**)
  - Closeable (java.io)
    - AutoCloseable (java.lang)

## 实现关系

- ConfigurableApplicationContext (org.springframework.context)
  - ..
  - **AbstractApplicationContext** (org.springframework.context.support)
    - **GenericApplicationContext** (org.springframework.context.support)
      - GenericXmlApplicationContext (org.springframework.context.support)
      - StaticApplicationContext (org.springframework.context.support)
      - GenericWebApplicationContext (org.springframework.web.context.support)
      - **AnnotationConfigApplicationContext** (org.springframework.context.annotation)
        - ..
    - **AbstractRefreshableApplicationContext** (org.springframework.context.support)
      - AbstractRefreshableConfigApplicationContext (org.springframework.context.support)
        - AbstractXmlApplicationContext (org.springframework.context.support)
          - **FileSystemXmlApplicationContext** (org.springframework.context.support)
          - **ClassPathXmlApplicationContext** (org.springframework.context.support)

## AbstractApplicationContext

`ConfigurableApplicationContext` 最重要的实现是 `AbstractApplicationContext`，其最重要的方法又是 `refresh` 方法，是 **Spring 启动过程的主要流程**

## ❤ refresh()

```java
public void refresh() throws BeansException, IllegalStateException {
  // refresh 和 destroy 的时候用到，确保不会同时执行
  synchronized (this.startupShutdownMonitor) {
    // 准备工作，例如 『初始化环境』『重置 监听器和事件』..
    prepareRefresh();

    // 通知子类刷新 BeanFactory，从 xml 或 注解中扫描 bean，完成对 bean 的注册
    //1. destroyBeans 销毁 beans
    //2. 扫描注册 Bean ：AbstractRefreshableApplicationContext#refreshBeanFactory#loadBeanDefinitions 
    ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

    // 对 beanFactory 做一些设置，例如 类加载器、spel解析器、BeanPostProcessor、默认 Bean (Env) 注册等
    prepareBeanFactory(beanFactory);

    try {
      // 没有实现，子类（web模块）根据自己的功能 增加 BeanPostProcessor
      postProcessBeanFactory(beanFactory);

      // 执行 BeanFactoryPostProcessor 后置处理器，『此时 bean 还没有实例化』
      invokeBeanFactoryPostProcessors(beanFactory);

      // 将所有的 bean 的后置处理器排好序，但不会马上用，bean 实例化之后会用到
      registerBeanPostProcessors(beanFactory);

      // 国际化服务
      initMessageSource();

      // 初始化事件广播器，SimpleApplicationEventMulticaster
      initApplicationEventMulticaster();

      /// 没有实现，子类实现用于通知
      onRefresh();

      // 注册一部分特殊的事件监听器，剩下的只是准备好名字，等 Bean 实例化完成后再注册
      registerListeners();

      //  通过 getBean 的方式初始化 单例 Bean
      finishBeanFactoryInitialization(beanFactory);

      // 生命周期监听器的回调，广播通知 
      finishRefresh();
      
    } catch (BeansException ex) {
      ...

      // Destroy already created singletons to avoid dangling resources.
      // 刷新失败后的处理，主要是将一些保存环境信息的集合做清理
      destroyBeans();

      // applicationContext是否已经激活的标志，设置为false
      cancelRefresh(ex);

      throw ex;
    }

    finally {
      // Reset common introspection caches in Spring's core, since we
      // might not ever need metadata for singleton beans anymore...
      resetCommonCaches();
    }
  }
}
```

## prepareRefresh() ❤

```java
protected void prepareRefresh() {
  this.startupDate = System.currentTimeMillis();
  // 设置标志位
  this.closed.set(false);
  this.active.set(true);

  .. // 打印调试日志

  // 『❤扩展❤』默认没有实现， 在 Web 环境下用来保存 ServletContext
  // WebApplicationContextUtils.initServletPropertySources(..,servletContext,servletConfig);
  initPropertySources();

  // 创建 Environment 并校验 
  // spring-core: StandardEnvironment 加载 System.getProperties() 和 System.getenv()
  // spring-web: StandardServletEnvironment 保存 ServletContext
  getEnvironment().validateRequiredProperties();

  // 重置 事件监听器和事件，保留 earlyApplicationListeners
  ...
}
```

- TODO 详见：
  - spring-core： Environment link
  - spring-core： ConfigurableEnvironment link



## obtainFreshBeanFactory() ❤

通知子类刷新 BeanFactory，**从 xml 或 注解中扫描 bean，完成对 bean 的注册**

```java
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
  // 抽象方法没有实现， 在子类 AbstractRefreshableApplicationContext 中有实现
  refreshBeanFactory();
  // 没有实现
  // GenericApplicationContext DefaultListableBeanFactory
  // AbstractRefreshableApplicationContext DefaultListableBeanFactory(getInternalParentBeanFactory())
  return getBeanFactory();
}
```

### Abs..RefreshableA..C...refreshBeanFactory()

```java
    // AbstractRefreshableApplicationContext

    protected final void refreshBeanFactory() throws BeansException {
        // BeanFactory 存在
        if (hasBeanFactory()) {
            // 则销毁 beans，最后销毁流程详细说明
            destroyBeans();
            // beanFactory 变量置空
            closeBeanFactory();
        }

        try {
            // ❤❤❤ DefaultListableBeanFactory ❤❤❤
            DefaultListableBeanFactory beanFactory = createBeanFactory();
            beanFactory.setSerializationId(getId());
            // beanFactory.setAllowBeanDefinitionOverriding
            // beanFactory.setAllowCircularReferences
            customizeBeanFactory(beanFactory);
            // 『❤❤❤扩展❤❤❤』扫描注册 Bean
            loadBeanDefinitions(beanFactory);
            synchronized (this.beanFactoryMonitor) {
                this.beanFactory = beanFactory;
            }
        }
        catch (IOException ex) {
            throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
        }
    }
```

**AbstractRefreshableApplicationContext.createBeanFactory()**

```java
protected DefaultListableBeanFactory createBeanFactory() {
  return new DefaultListableBeanFactory(getInternalParentBeanFactory());
}
```

**?? AbstractApplicationContext.getInternalParentBeanFactory()** 

```java
protected BeanFactory getInternalParentBeanFactory() {
  return 
    getParent() instanceof ConfigurableApplicationContext
    ? ((ConfigurableApplicationContext) getParent()).getBeanFactory() 
    : getParent();
}
```



## prepareBeanFactory

```java
protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
  // 告诉 beanfactory 上下文的类加载器
  beanFactory.setBeanClassLoader(getClassLoader());
  // 设置解析器，用于解析 bean 的定义中出现的 Spel 表达式
  beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
  // 设置自定义的转换器，用作 字符串和对象 之间的类型转换（如：xml 中的配置）
  beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

  // 增加后置处理器 ApplicationContextAwareProcessor
  // 如果 bean 实现以下接口，将在 BeanPostProcessor 流程中 注入指定的对象
  // EnvironmentAware
  // EmbeddedValueResolverAware
  // ResourceLoaderAware 
  // ApplicationEventPublisherAware
  // MessageSourceAware 
  // ApplicationContextAware
  beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
  // bean 在初始化的时候，如果 bean 是以下接口的实现，则不进行注入
  beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
  beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
  beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
  beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
  beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
  beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);

  // 针对这些类型的 @Autowired，获取的类型实例
  beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
  beanFactory.registerResolvableDependency(ResourceLoader.class, this);
  beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
  beanFactory.registerResolvableDependency(ApplicationContext.class, this);

  // @since 4.3.4
  // 注册 ApplicationListener 事件监听器
  beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

  // 部署一个 bean 的后置处理器 ApplicationContextAwareProcessor，用于 AOP "静态"代理相关的处理
  if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
    beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
    beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
  }

  // 注册 Environment (ConfigurableApplicationContext.environment)
  if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
    beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
  }
  // 注册 SystemProperties (ConfigurableApplicationContext.systemProperties)
  if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
    beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
  }
  // SystemEnvironment (ConfigurableApplicationContext.systemEnvironment)
  if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
    beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
  }
}
```

## postProcessBeanFactory

- **AbstractApplicationContext** (.**context**.support)
  - GenericApplicationContext (.context.support)
    - StaticApplicationContext (.context.support)
      - **StaticWebApplicationContext** (.**web**.context.support)
    - **GenericWebApplicationContext** (.**web**.context.support)
      - **ServletWebServerApplicationContext** (.**boot.web**.servlet.context)
        - **AnnotationConfigServletWebServerApplicationContext** (.**boot.web**.servlet.context)
      - **AnnotationConfigServletWebApplicationContext** (**.boot.web**.servlet.context)
    - GenericReactiveWebApplicationContext (.boot.web.reactive.context)
      - ReactiveWebServerApplicationContext (.boot.web.reactive.context)
        - **AnnotationConfigReactiveWebServerApplicationContext** (**.boot.web.**reactive.context)
  - AbstractRefreshableApplicationContext (.context.support)
    - AbstractRefreshableConfigApplicationContext (.context.support)
      - **AbstractRefreshableWebApplicationContext** (.**web**.context.support)

### StaticWebApplicationContext:spring-web

```java
protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
  // ServletContextAware/ServletConfigAware 初始化 bean 的时候，注入指定的对象
  beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext, this.servletConfig));
  beanFactory.ignoreDependencyInterface(ServletContextAware.class);
  beanFactory.ignoreDependencyInterface(ServletConfigAware.class);

  WebApplicationContextUtils.registerWebApplicationScopes(beanFactory, this.servletContext);
  WebApplicationContextUtils.registerEnvironmentBeans(beanFactory, this.servletContext, this.servletConfig);
}
```

### GenericWebApplicationContext:spring-web

```java
protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
  if (this.servletContext != null) {
    // ServletContextAware/ServletConfigAware 初始化 bean 的时候，注入指定的对象
    beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext));
    beanFactory.ignoreDependencyInterface(ServletContextAware.class);
  }
  WebApplicationContextUtils.registerWebApplicationScopes(beanFactory, this.servletContext);
  WebApplicationContextUtils.registerEnvironmentBeans(beanFactory, this.servletContext);
}
```

### AbstractRefreshableWebApplicationContext:spring-web

```java
protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
  beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext, this.servletConfig));
  beanFactory.ignoreDependencyInterface(ServletContextAware.class);
  beanFactory.ignoreDependencyInterface(ServletConfigAware.class);

  WebApplicationContextUtils.registerWebApplicationScopes(beanFactory, this.servletContext);
  WebApplicationContextUtils.registerEnvironmentBeans(beanFactory, this.servletContext, this.servletConfig);
}
```



## invokeBeanFactoryPostProcessors ❤

- 此时 Bean 已经在 `obtainFreshBeanFactory` 完成扫描的注册，但是还没有实例化
- 不要在自己扩展的 `BeanFactoryPostProcessor` 中调用会触发 Bean 实例化的方法，如：
  - `getBean`
  - `getBeansOfType`
- 处理 Bean 的实例应在 `BeanPostProcessor` 中进行
-  `BeanFactoryPostProcessor` 主要用来 **修改 `BeanDefinition`** 或 **注册更多的 `BeanDefinition`**

```java
protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
    PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());

    // Detect a LoadTimeWeaver and prepare for weaving, if found in the meantime
    // (e.g. through an @Bean method registered by ConfigurationClassPostProcessor)
    if (beanFactory.getTempClassLoader() == null && beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
        beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
        beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
    }
}

```



## registerBeanPostProcessors

```java
protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
  PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this);
}
```

### registerBeanPostProcessors？？？

```java
public static void registerBeanPostProcessors(
  ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

  String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

  // Register BeanPostProcessorChecker that logs an info message when
  // a bean is created during BeanPostProcessor instantiation, i.e. when
  // a bean is not eligible for getting processed by all BeanPostProcessors.
  int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
  beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

  // Separate between BeanPostProcessors that implement PriorityOrdered,
  // Ordered, and the rest.
  List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
  List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
  List<String> orderedPostProcessorNames = new ArrayList<>();
  List<String> nonOrderedPostProcessorNames = new ArrayList<>();
  for (String ppName : postProcessorNames) {
    if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
      BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
      priorityOrderedPostProcessors.add(pp);
      if (pp instanceof MergedBeanDefinitionPostProcessor) {
        internalPostProcessors.add(pp);
      }
    }
    else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
      orderedPostProcessorNames.add(ppName);
    }
    else {
      nonOrderedPostProcessorNames.add(ppName);
    }
  }

  // First, register the BeanPostProcessors that implement PriorityOrdered.
  sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
  registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

  // Next, register the BeanPostProcessors that implement Ordered.
  List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
  for (String ppName : orderedPostProcessorNames) {
    BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
    orderedPostProcessors.add(pp);
    if (pp instanceof MergedBeanDefinitionPostProcessor) {
      internalPostProcessors.add(pp);
    }
  }
  sortPostProcessors(orderedPostProcessors, beanFactory);
  registerBeanPostProcessors(beanFactory, orderedPostProcessors);

  // Now, register all regular BeanPostProcessors.
  List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
  for (String ppName : nonOrderedPostProcessorNames) {
    BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
    nonOrderedPostProcessors.add(pp);
    if (pp instanceof MergedBeanDefinitionPostProcessor) {
      internalPostProcessors.add(pp);
    }
  }
  registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

  // Finally, re-register all internal BeanPostProcessors.
  sortPostProcessors(internalPostProcessors, beanFactory);
  registerBeanPostProcessors(beanFactory, internalPostProcessors);

  // Re-register post-processor for detecting inner beans as ApplicationListeners,
  // moving it to the end of the processor chain (for picking up proxies etc).
  beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
}
```



## ~~initMessageSource~~

```java
protected void initMessageSource() {
  ConfigurableListableBeanFactory beanFactory = getBeanFactory();

  if (beanFactory.containsLocalBean(MESSAGE_SOURCE_BEAN_NAME)) {
    // 如果已经注册，并且是 HierarchicalMessageSource hms，
    ...
    hms.setParentMessageSource(getInternalParentMessageSource())
    ...
  } else {
    // 如果没有注册，使用空的 MessageSource 
    DelegatingMessageSource dms = new DelegatingMessageSource();
    dms.setParentMessageSource(getInternalParentMessageSource());
    this.messageSource = dms;
    beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
    ...
    }
  }
}
```

## ~~initApplicationEventMulticaster~~

```java
protected void initApplicationEventMulticaster() {
     ConfigurableListableBeanFactory beanFactory = getBeanFactory();
     if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
       // 如果有则获取
       this.applicationEventMulticaster = beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
       ...
		 	   } else {
       // 没有则创建 SimpleApplicationEventMulticaster
       this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
       beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
       ...
     }
}
```



## ~~onRefresh~~

实例化 Bean 之前通知子类做一些事情，把大部分仍然与 `MessageSource` 国际化有关

```java
protected void onRefresh() throws BeansException {
  // For subclasses: do nothing by default.
}
```



```java
// org.springframework.web.context.support.AbstractRefreshableWebApplicationContext
protected void onRefresh() {
  this.themeSource = UiApplicationContextUtils.initThemeSource(this);
}

//StaticWebApplicationContext
protected void onRefresh() {
  this.themeSource = UiApplicationContextUtils.initThemeSource(this);
}

// GenericWebApplicationContext
protected void onRefresh() {
  this.themeSource = UiApplicationContextUtils.initThemeSource(this);
}

// org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext 
// extends GenericWebApplicationContext
protected void onRefresh() {
  super.onRefresh();
  try {
    createWebServer();
  } catch (Throwable ex) {
    throw new ApplicationContextException("Unable to start web server", ex);
  }
}
```

## registerListeners()

```java
protected void registerListeners() {
  // 注册的都是特殊的事件监听器，而并非配置中的 Listener
  for (ApplicationListener<?> listener : getApplicationListeners()) {
    getApplicationEventMulticaster().addApplicationListener(listener);
  }

  // 根据接口类型找出所有监听器的名称，先注册 ApplicationListener 的名称，而非实例，此时 Bean 还没有初始化
  // 之后 postProcessor 会根据名字初始化 Bean 的实例
  String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
  for (String listenerBeanName : listenerBeanNames) {
    getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
  }

  // 发布 早期 application events  事件
  Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
  this.earlyApplicationEvents = null;
  if (earlyEventsToProcess != null) {
    for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
      getApplicationEventMulticaster().multicastEvent(earlyEvent);
    }
  }
}
```

## finishBeanFactoryInitialization ❤

```java
protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
    // 实例化 ConversionService，并保存在 ApplicationContext 中
    if (beanFactory.containsBean("conversionService") && beanFactory.isTypeMatch("conversionService", ConversionService.class)) {
        beanFactory.setConversionService(beanFactory.getBean("conversionService", ConversionService.class));
   	 }

    // @since 4.3
    // 增加属性解析器
    if (!beanFactory.hasEmbeddedValueResolver()) {
        // @since 3.0
        beanFactory.addEmbeddedValueResolver(strVal -> getEnvironment().resolvePlaceholders(strVal));
   	 }

    // 实例化 LoadTimeWeaverAware 接口的 bean，用于 ApsectJ 的类加载期织入的处理
    String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
    for (String weaverAwareName : weaverAwareNames) {
        getBean(weaverAwareName);
   	 }
    // load-time weaving 时会用到，这里去掉  临时 ClassLoader
    beanFactory.setTempClassLoader(null);

    // 冻结加载的 Bean  Name ( DefaultListableBeanFactory )
    // this.configurationFrozen = true;
	   // this.frozenBeanDefinitionNames = StringUtils.toStringArray(this.beanDefinitionNames);
    beanFactory.freezeConfiguration();

    // 「❤❤❤」实例化所有非懒加载的 单例Bean「❤❤❤」
    beanFactory.preInstantiateSingletons();
}
```

### preInstantiateSingletons

```java
public void preInstantiateSingletons() throws BeansException {
  //...
  List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);
  // 触发初始化 所有的 non-lazy singleton beans
  for (String beanName : beanNames) {
    // ❤❤❤❤❤
    RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
    //  非抽象 && 单例 && 非懒加载
    if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
      // 
      if (isFactoryBean(beanName)) {
        // &beanName
        Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
        if (bean instanceof FactoryBean) {
          ...
          // FactoryBean 是否是 SmartFactoryBean.isEagerInit
          // 如果 isEagerInit 通过 getBean 来触发 Bean 的实例化
          // ❤❤❤❤❤ FactoryBean 并不会被初始化
        }
      }  //  isFactoryBean(beanName)
      else {
        // ❤❤❤❤❤ 通过 getBean 来触发 Bean 的实例化
        getBean(beanName);
      }
    } //  !bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()
  }

  // @since 4.1 SmartInitializingSingleton#afterSingletonsInstantiated
  // 标记接口，在单例 Bean 初始化之后调用
  for (String beanName : beanNames) {
    Object singletonInstance = getSingleton(beanName);
    if (singletonInstance instanceof SmartInitializingSingleton) {
      final SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
      ... 
      // smartSingleton.afterSingletonsInstantiated();
    }
  }
}
```

## finishRefresh

applicationContext 刷新完成后的处理，例如生命周期监听器的回调，广播通知等

```java
protected void finishRefresh() {
    //  @since 5.0 
    // 清除在资源加载过程中的缓存
    clearResourceCaches();

    // 检查是否已经配置了生命周期处理器，如果没有就 new DefaultLifecycleProcessor
    initLifecycleProcessor();

    // 找到所有实现了 Lifecycle 接口的 Bean，按照每个 bean设置的生命周期阶段进行分组，
    // 再依次调用每个分组中每个 Bean 的 start 方法
    getLifecycleProcessor().onRefresh();

    // 发布 ContextRefreshedEvent 事件，交给广播器去广播
    publishEvent(new ContextRefreshedEvent(this));

    // 如果 Environment 中有 spring.liveBeansView.mbeanDomain  配置
    // 就在 MBeanServer 上的注册 LiveBeansView（ConfigurableApplicationContext）
    LiveBeansView.registerApplicationContext(this);
}
```

### initLifecycleProcessor

```java
protected void initLifecycleProcessor() {
  ConfigurableListableBeanFactory beanFactory = getBeanFactory();
  // lifecycleProcessor
  if (beanFactory.containsLocalBean(LIFECYCLE_PROCESSOR_BEAN_NAME)) {
     this.lifecycleProcessor = beanFactory.getBean(LIFECYCLE_PROCESSOR_BEAN_NAME, LifecycleProcessor.class);
     ...
   } else {
    DefaultLifecycleProcessor defaultProcessor = new DefaultLifecycleProcessor();
    defaultProcessor.setBeanFactory(beanFactory);
    this.lifecycleProcessor = defaultProcessor;
    beanFactory.registerSingleton(LIFECYCLE_PROCESSOR_BEAN_NAME, this.lifecycleProcessor);
     ...
   }
}
```



## destroyBeans()

```java
protected void destroyBeans() {
  getBeanFactory().destroySingletons();
}

// DefaultSingletonBeanRegistry
public void destroySingletons() {
 ...
  synchronized (this.singletonObjects) {
    this.singletonsCurrentlyInDestruction = true;
   }

  String[] disposableBeanNames;
  synchronized (this.disposableBeans) {
    disposableBeanNames = StringUtils.toStringArray(this.disposableBeans.keySet());
  }
  for (int i = disposableBeanNames.length - 1; i >= 0; i--) {
    destroySingleton(disposableBeanNames[i]);
  }

  this.containedBeanMap.clear();
  this.dependentBeanMap.clear();
  this.dependenciesForBeanMap.clear();

  clearSingletonCache();
}

// DefaultSingletonBeanRegistry (org.springframework.beans.factory.support)
// |--FactoryBeanRegistrySupport (org.springframework.beans.factory.support)
// |--|--AbstractBeanFactory (org.springframework.beans.factory.support)
// |--|--|--AbstractAutowireCapableBeanFactory (org.springframework.beans.factory.support)
// |--|--|--|--DefaultListableBeanFactory (org.springframework.beans.factory.support)
public void destroySingletons() {
  super.destroySingletons();
  updateManualSingletonNames(Set::clear, set -> !set.isEmpty());
  clearByTypeCache();
}
```

### destroySingleton

```java
public void destroySingleton(String beanName) {
    // Remove a registered singleton of the given name, if any.
    removeSingleton(beanName);

    // Destroy the corresponding DisposableBean instance.
    DisposableBean disposableBean;
    synchronized (this.disposableBeans) {
      disposableBean = (DisposableBean) this.disposableBeans.remove(beanName);
    }
    destroyBean(beanName, disposableBean);
}
```

### destroyBean

```java
protected void destroyBean(String beanName, @Nullable DisposableBean bean) {
  // Trigger destruction of dependent beans first...
  Set<String> dependencies;
  synchronized (this.dependentBeanMap) {
    // Within full synchronization in order to guarantee a disconnected Set
    dependencies = this.dependentBeanMap.remove(beanName);
  }
  if (dependencies != null) {
    if (logger.isTraceEnabled()) {
      logger.trace("Retrieved dependent beans for bean '" + beanName + "': " + dependencies);
    }
    for (String dependentBeanName : dependencies) {
      destroySingleton(dependentBeanName);
    }
  }

  // Actually destroy the bean now...
  if (bean != null) {
    try {
      // ❤❤❤ ❤❤❤
      bean.destroy();
    }
    catch (Throwable ex) {
      if (logger.isWarnEnabled()) {
        logger.warn("Destruction of bean with name '" + beanName + "' threw an exception", ex);
      }
    }
  }

  // Trigger destruction of contained beans...
  Set<String> containedBeans;
  synchronized (this.containedBeanMap) {
    // Within full synchronization in order to guarantee a disconnected Set
    containedBeans = this.containedBeanMap.remove(beanName);
  }
  if (containedBeans != null) {
    for (String containedBeanName : containedBeans) {
      destroySingleton(containedBeanName);
    }
  }

  // Remove destroyed bean from other beans' dependencies.
  synchronized (this.dependentBeanMap) {
    for (Iterator<Map.Entry<String, Set<String>>> it = this.dependentBeanMap.entrySet().iterator(); it.hasNext();) {
      Map.Entry<String, Set<String>> entry = it.next();
      Set<String> dependenciesToClean = entry.getValue();
      dependenciesToClean.remove(beanName);
      if (dependenciesToClean.isEmpty()) {
        it.remove();
      }
    }
  }

  // Remove destroyed bean's prepared dependency information.
  this.dependenciesForBeanMap.remove(beanName);
}
```



## cancelRefresh(ex)

```java
protected void cancelRefresh(BeansException ex) {
    this.active.set(false);
}
```



## resetCommonCaches()

```java
protected void resetCommonCaches() {
    ReflectionUtils.clearCache();
    AnnotationUtils.clearCache();
    ResolvableType.clearCache();
    CachedIntrospectionResults.clearClassLoader(getClassLoader());
}
```



## 小结

`refresh` 的过程中有几个关键流程

- `prepareRefresh` 初始化 **`Environment`** 
- `obtainFreshBeanFactory` 通知子类 **扫描解析Bean**
- `invokeBeanFactoryPostProcessors` 调用 **`BeanFactoryPostProcessor`**， 可对扫描到的 `BeanDefinition` 进行修改或定义
- 最后 `finishBeanFactoryInitialization` 通过 **`getBean` 的方式实例化单例的 Bean** 



## Read More

- [Spring 初始化源码学习三部曲之一：AbstractApplicationContext 构造方法](https://blog.csdn.net/boling_cavalry/article/details/80957707)
- [Spring 初始化源码学习三部曲之二：setConfigLocations 方法](https://blog.csdn.net/boling_cavalry/article/details/80958832)
- [Spring 初始化源码学习三部曲之三：AbstractApplicationContext.refresh 方法](https://blog.csdn.net/boling_cavalry/article/details/81045637)