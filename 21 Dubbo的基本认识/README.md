## Dubbo是什么？以及为什么要用？

dubbo是一款高性能、轻量级的开源Java RPC框架，它提供了三大核心能力：面向接口的远程方法调用，智能容错和负载均衡，以及服务自动注册和发现。

随着互联网的发展，网站应用的规模不断扩大，常规的垂直应用架构已无法应对，分布式服务架构以及流动计算架构势在必行，亟需一个治理系统确保架构有条不紊的演进。当进一步发展，服务间依赖关系变得错踪复杂，甚至分不清哪个应用要在哪个应用之前启动，架构师都不能完整的描述应用的架构关系。 这时，需要自动画出应用间的依赖关系图，以帮助架构师理清理关系。

接着，服务的调用量越来越大，服务的容量问题就暴露出来，这个服务需要多少机器支撑？什么时候该加机器？ 为了解决这些问题，第一步，要将服务现在每天的调用量，响应时间，都统计出来，作为容量规划的参考指标。其次，要可以动态调整权重，在线上，将某台机器的权重一直加大，并在加大的过程中记录响应时间的变化，直到响应时间到达阈值，记录此时的访问量，再以此访问量乘以机器数反推总容量。


## Dubbo目前支持哪些协议进行服务发布，请实现将服务以Dubbo协议以及REST协议进行发布
### dubbo支持的协议

- dubbo:   使用基于 mina 和 hessian 的 tbremoting 交互，采用单一长连接和 NIO 异步通讯，适合于小数据量大并发的服务调用，以及服务消费者机器数远大于服务提供者机器数的情况。
- rmi:   采用阻塞式短连接和 JDK 标准序列化方式。适用范围：传入传出参数数据包大小混合，消费者与提供者个数差不多，可传文件。
- hessian:   Hessian 底层采用 Http 通讯，采用 Servlet 暴露服务，Dubbo 缺省内嵌 Jetty 作为服务器实现。
- http: 基于 HTTP 表单的远程调用协议，采用 Spring 的 HttpInvoker 实现，适用范围：传入传出参数数据包大小混合，提供者比消费者个数多，可用浏览器查看，可用表单或URL传入参数，暂不支持传文件。
- webservice: 基于 Apache CXF 的 frontend-simple 和 transports-http 实现。适用场景：系统集成，跨语言调用
- thrift:对 thrift 原生协议 的扩展，在原生协议的基础上添加了一些额外的头信息，比如 service name，magic number 等。
- memcached:基于 memcached实现的 RPC 协议
- redis: 基于 Redis实现的 RPC 协议
- rest: 基于标准的Java REST API——JAX-RS 2.0实现的REST调用支持




### 实现rest协议进行发布

1. 以一个注册用户的简单服务为例说明。如果要通过URL来访问不同ID的用户资料： http://localhost:8080/users/1001
2. 首先，开发服务的接口：

		public interface UserService {    
		   User getUser(Long user);
		}


3. 实现类

	@Path("/users")
	public class UserServiceImpl implements UserService {
	       
	    @GET
    	@Path("/{id : \\d+}")
	    @Consumes({MediaType.APPLICATION_JSON_UTF_8})
	    public User getUser(@PathParam("id") Long id);
	        // query the user...
	    }	
	}

4. 配置REST Server的实现

		<dubbo:application name="user-service" />
	    <dubbo:registry id="zk" address="zookeeper://127.0.0.1:2181" timeout="40000" /> 
		<dubbo:protocol name="rest" port="8888"  server="jetty"/>
	    <dubbo:service interface="cn.tf.dubbo.UserService" ref="userService"  protocol="rest" timeout="2000" registry="zk" />
		<bean id="userService" class ="cn.tf.dubbo.UserServiceImpl" />


### 实现dubbo协议进行发布
 配置文件

	<dubbo:application name="user-service" />
 	<dubbo:protocol name="dubbo" port="20880"/>
	 <dubbo:registry id="zk" address="zookeeper://127.0.0.1:2181" timeout="40000" /> 
	 <dubbo:protocol name="dubbo" port="20880"  />
	 <dubbo:service interface="cn.tf.dubbo.UserService" ref="userService"  protocol="dubbo" registry="zk" />
	<bean id="userService" class ="cn.tf.dubbo.UserServiceImpl" />




## 在Dubbo中，zookeeper主要起到什么样的作用？请简单描述一下Dubbo服务注册发现的实现原理

zookeeper用来注册服务和负载均衡。基于注册中心的事件通知（订阅与发布），一切支持事件订阅与发布的框架都可以作为Dubbo注册中心的选型。

1. 服务提供者在暴露服务时，会向注册中心注册自己，具体就是在${service interface}/providers目录下添加 一个节点（临时），服务提供者需要与注册中心保持长连接，一旦连接断掉（重试连接）会话信息失效后，注册中心会认为该服务提供者不可用（提供者节点会被删除）。
2. 消费者在启动时，首先也会向注册中心注册自己，具体在${interface interface}/consumers目录下创建一个节点。
3. 消费者订阅${service interface}/ [ providers、configurators、routers ]三个目录，这些目录下的节点删除、新增事件都胡通知消费者，根据通知，重构服务调用器(Invoker)。




