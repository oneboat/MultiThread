## 1、Dubbo的服务远程调用，如何实现异步通信
1. NIO Future主动获取结果，返回结果放在RpcContext中
由于RpcContext是单例模式，所以每次调用完后，需要保存一个Future实例；

	Future<Foo> fooFuture = RpcContext.getContext().getFuture();

2. 通过回调（Callback）参数。Callback并不是dubbo内部类或接口，而是由应用自定义的、实现了Serializable的接口；

		<dubbo:service ..>
		         <dubbo:method name="method1">
		            <dubbo:argument index="1" callback="true" />  #标识第二个参数是callback类型
		         </dubbo:method>
		      </dubbo:service>

			<dubbo:reference ...>
		         <dubbo:method name="method1" async="true">
		      </dubbo:reference>

3. 事件通知（推荐），事件通知允许 Consumer 端在调用之前、调用正常返回之后或调用出现异常时，触发 oninvoke、onreturn、onthrow 三个事件。

		<bean id="notify" class="com.alibaba.dubbo.callback.implicit.NofifyImpl" />
		      <dubbo:reference >
		<dubbo:method name="method1" async="true" onreturn="notify.onreturn" onthrow="notify.onthrow" />
		      </dubbo:reference>


## 2、Dubbo配置的优先级别是什么样的？
- 方法>接口>全局
- 消费端>服务端
- 系统属性>扩展配置>Spring/API>本地文件配置，例如
Java运行时虚拟机参数 （-Ddubbo.protocol.port=20880）
优先于 dubbo.xml || application.properties
（<dubbo:protocol port="30880"/>）
优先于 dubbo.properties（Dubbo公共属性配置文件）（dubbo.protocol.port=20880）

- 默认情况下，外部配置的优先级最高，也就是意味着配置中心上的配置会覆盖本地的配置。当然我们也可以调整优先级dubbo.config-center.highest-priority=false

## 3、Dubbo默认的重试次数是多少？
不配置，默认重试2次 
不算第一个调用，一共会调用三次

## 4、Dubbo2.7有哪些新特性？
1. 服务治理规则增强：
	- 更丰富的服务治理规则，新增应用级别条件路由、Tag路由等；
	- 治理规则与注册中心解耦，增加对Apollo等第三方专业配置中心的支持，更易于扩展；
	- 新增应用级别的动态配置规则；
2. 外部化配置。支持读取托管在远程的集中式配置中心的dubbo.properties，实现应用配置的集中式管控。
3. 更精炼的注册中心URL，进一步减轻注册中心存储和同步压力，初步实现地址和配置的职责分离。
4. 新增服务元数据中心，负责存储包括服务静态化配置、服务定义（如方法签名）等数据，默认提供Zookeeper, Redis支持。此功能也是OPS实现服务测试、Mock等治理能力的基础。
5. 新增Protobuf序列化协议扩展和ExpiringCache缓存策略扩展。



## 5、Dubbo实现了哪几种负载均衡算法？分别是如何实现的？
默认提供了 4 中负载均衡实现，roundrobin/random/leastactive/ consistenthash。

- RoundRobinLoadBalance：加权轮询算法。对轮询过程进行加权，以调控每台服务器的负载。经过加权后，每台服务器能够得到的请求数比例，接近或等于他们的权重比。
- RandomLoadBalance：随机轮询算法，根据权重值进行随机负载。在一个截面上碰撞的概率高，但调用量越大分布越均匀，而且按概率使用权重后也比较均匀，有利于动态调整提供者权重。
- LeastActiveLoadBalance：最少活跃调用数算法，活跃调用数越小，表明该服务提供者效率越高，单位时间内可处理更多的请求这个是比较科学的负载均衡算法。每个服务提供者对应一个活跃数 active。初始情况下，所有服务提供者活跃数均为 0。每收到一个请求，活跃数加 1，完成请求后则将活跃数减 1。在服务运行一段时间后，性能好的服务提供者处理请求的速度更快，因此活跃数下降的也越快，此时这样的服务提供者能够优先获取到新的服务请求。
- ConsistentHashLoadBalance：hash 一致性算法，相同参数的请求总是发到同一提供者当某一台提供者挂时，原本发往该提供者的请求，基于虚拟节点，平摊到其它提供者，不会引起剧烈变动。


## 6、Dubbu服务提供者的接口要做升级，如何实现对老版本的兼容？

当一个接口实现，出现不兼容升级时，可以用版本号过渡，版本号不同的服务相互间不引用。在低压力时间段，先升级一半提供者为新版本再将所有消费者升级为新版本然后将剩下的一半提供者升级为新版本。

    <dubbo:service interface="com.foo.BarService" version="1.0.0" />
    <dubbo:service interface="com.foo.BarService" version="2.0.0" />
    <dubbo:reference id="barService" interface="com.foo.BarService" version="1.0.0" />
    <dubbo:reference id="barService" interface="com.foo.BarService" version="2.0.0" />

