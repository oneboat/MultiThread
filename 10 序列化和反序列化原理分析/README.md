### 1、什么是序列化，Java是如何实现序列化的

- 序列化就是一种用来处理对象流的机制，所谓对象流也就是将对象的内容进行流化。可以对流化后的对象进行读写操作，也可将流化后的对象传输于网络之间。
- 将需要被序列化的类实现Serializable接口，然后使用一个输出流(如：FileOutputStream)来构造一个ObjectOutputStream(对象流)对象，接着，使用ObjectOutputStream对象的writeObject(Object obj)方法就可以将参数为obj的对象写出(即保存其状态)，要恢复的话则用输入流。

### 2、如果一个子类实现了序列化，父类没有实现，那么父类中的成员变量能否被序列化？
- 不能。相同的方法会被重写，变量没有重写之说，如果子类声明了跟父类一样的变量，那意味着子类将有两个相同名称的变量。一个存放在子类实例对象中，一个存放在父类子对象中。父类的private变量，也会被继承并且初始化在子类父对象中，只不过对外不可见。所以父类中的成员变量自然也不能被序列化了。

### 3、你有了解过哪些序列化技术？以及他们之间的差异性？
- java原生的序列化方式，不支持跨语言
- xml序列化，可读性好，跨语言，但是序列化后的文件比较大，性能低
- json序列化，有Jackson\Gson\FastJson等方式。可读性较好，序列化后体积小,速度较快，多用于提供对外接口等情况
- protostuff，跨语言,序列化后体积很小,速度快,但是需要Schema，比较麻烦，对于数据大小敏感，传输效率高的模块可以采用protobuf库
- hession，这是一种跨语言的二进制序列化协议，相对于java默认的序列化机制来说，具有更好的性能和易用性
- kryo，跨语言支持较困难,序列化后体积小,速度较快


### 4、transient是干嘛的？
- transient是用来表示一个域不是该对象串行化的一部分。当一个对象被串行化的时候，transient型变量的值不包括在串行化的表示中，然而非transient型的变量是被包括进去的。


### 5、有什么方法能够绕过transient的机制。这个实现机制的原理是什么？

- 可以使用writeObject和readObject绕过transient的机制，实现原理是通过反射，因为ObjectOutputStream使用了反射来寻找是否声明了这两个方法。因为ObjectOutputStream使用getPrivateMethod，所以这两个方法被声明为priate以至于供ObjectOutputStream来使用。

### 6、serializable的安全性如何保证？
- serializable的安全性是通过对比serialVersionUID来实现的。serialVersionUID不一致会抛出异常，不能加载对象。

### 7.有没有了解过protobuf，它的序列化实现原理是什么？

- protobuf是Google提供一个具有高效的协议数据交换格式工具库，但相比于Json，Protobuf有更高的转化效率，时间效率和空间效率都是JSON的3-5倍，Protobuf对于不同的字段类型采用不同的编码方式和数据存储方式对消息字段进行序列化，以确保得到高效紧凑的数据压缩。
- Protobuf中用到了编码和压缩的方式实现序列化；Protobuf会在.proto文件中定义好类的路径以及类种属性的顺序。Protobuf会将字符编码成ASCII码，编码后，如果数值超过一个字节的大小则进行压缩。
存储采用TAG|length|value的形式，其中tag根据属性的顺序<<3后与类型取或运算，计算tag的值。Length为value的长度，可有可无；value为经过编码和压缩后的值。


### 7、serialVersionUID的作用是什么？如果不设置serialVersionUID,有没有问题？

- serialVersionUID是序列化的版本号，如果不设置serialVersionUID，在序列化的时候会自动生成一个唯一编码，不设置时存在的问题是当再添加serialVersionUID后，会出现版本不一致异常；如果设置了serialVersionUID，修改了类，也可以加载最新的，不存在版本不一致的异常。



