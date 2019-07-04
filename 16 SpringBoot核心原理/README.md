## 1、基于你对springboot的理解描述一下什么是springboot？
答： springboot 框架是为了能够帮助使用 spring 框架的开发
者快速高效的构建一个基于 spirng 框架以及 spring 生态
体系的应用解决方案。它是对“约定优于配置”这个理念下
的一个最佳实践。因此它是一个服务于框架的框架，服务
的范围是简化配置文件。

## 2、约定优于配置指的是什么？
答：

- 约定优于配置，也称作按约定编程，是一种软件设计范式，旨在减少软件开发人员需做决定的数量，获得简单的好处，而又不失灵活性。

- 比如平时架构师搭建项目就是限制软件开发随便写代码，制定出一套规范，让开发人员按统一的要求进行开发编码测试之类的，这样就加强了开发效率与审查代码效率。
- 例如maven的目录结构，默认有resources文件夹存放配置文件，默认打包方式为jar；例如springcloud默认提供application.properties/yml 文件和bootstrap.properties/yml



## 3、@SpringBootApplication由哪几个注解组成，这几个注解分别表示什么作用？
答： @SpringBootApplication：由@Configuration @EnableAutoConfiguration @ComponentScan这三个注解整合在一起，用于启动 springboot 应用，使用更加便捷。

- @Configuration：意味着它其实也是一个 IoC容器的配置类，它是 JavaConfig形式的基于Spring IOC容器的配置类使用的一种注解。
- @EnableAutoConfiguration：主 要 作 用 其 实 就 是 帮 助springboot 应用把所有符合条件的@Configuration 配置都加载到当前 SpringBoot 创建并使用的 IoC 容器中。
- @ComponentScan：是扫描指定路径下的标识了需要装配的类，自动装配到 spring 的 Ioc 容器中。


## 4、springboot自动装配的实现原理？
答：

- EnableAutoConfiguration它会通过 import 导入第三方提供的 bean 的配置类：AutoConfigurationImportSelector，它是基于 ImportSelector 来实现基于动态 bean 的加载功能。
- 本质上来说，其实 EnableAutoConfiguration 会帮助springboot 应用把所有符合@Configuration 配置都加载到当前 SpringBoot 创建的 IoC 容器，而这里面借助了Spring 框架提供的一个工具类 SpringFactoriesLoader 的支持。以及用到了 Spring 提供的条件注解@Conditional，选择性的针对需要加载的 bean 进行条件过滤


## 5、spring中的spi机制的原理是什么？

答：SPI的全名为Service Provider Interface.这个是针对厂商或者插件的。java spi就是提供为某个接口寻找服务实现的机制

- SpringFactoriesLoader 的作用是从classpath/META-INF/spring.factories 文件中，根据 key 来加载对应的类到 spring IoC 容器中。
- 很多的@Configuration 其实是依托于其他的框架来加载的，如果当前的 classpath 环境下没有相关联的依赖，则意味着这些类没必要进行加载，所以，通过这种条件过滤可以有效的减少@configuration 类的数量从而降低
SpringBoot 的启动时间。

