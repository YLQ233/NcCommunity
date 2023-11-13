# 项目功能

## 一、登录

### 流程

验证验证码
验证账号密码
登录成功后：
生成凭证并LoginTicket对象按JSON存到Redis中
Cookie中存ticket字符串，用以将来在拦截器中通过cookie验证凭证对象
登出则将Redis中登陆凭证状态改为1

### 登陆凭证—会话管理

在无状态的http协议下，多个请求保持会话。
将ticket返回，下次带cookie请求。

#### Session方案

1、粘性session：ip绑定服务器，难以负载均衡
2、同步Session：服务器之间同步session，增加服务器开销。
3、Session服务器：专门的服务器存Session，挂掉不安全。

#### 数据库方案

登陆凭证存放到Mysql中，把请求的cookie中的登录凭证并与mysql中的作比对。
频繁访问数据库性能差。

| **id** | **user_id** | **ticket** | **status**     | **expired** |
| ------ | ----------- | ---------- | -------------- | ----------- |
|        |             |            | 0-登录；1-登出 |             |

#### Redis方案√

| **key**       | **value**                       |
| ------------- | ------------------------------- |
| ticket:随机数 | Loginticket对象(Json字符串形式) |

### 验证码

Redis存验证码原因：频繁刷新，时效短，不能放session。

| **key**       | **value**  |
| ------------- | ---------- |
| kaptcha:owner | 验证码数字 |

#### 流程

页面js发送请求getKaptcha：
将临时凭证owner随cookie发送给用户
将正确验证码存入Redis
验证码图片输出到浏览器

#### 未登录用户匹配验证码问题

生成随机数owner，存到cookie中随login页面响应给客户端。
登录请求带cookie中的随机owner,再从redis中取值。并与用户输入的验证码进行比对。

### Redis优化缓存用户信息

登陆后请求都要根据登录凭证查询用户信息，访问的频率非常高
查询User时，先查redis，没有就初始化。getCache(); initCache();
若用户信息有更改：
若”删除缓存，下一次初始化时更新存入缓存“，则会出bug

## 二、登录验证—拦截器

### 流程

配置拦截路径（在WebMvcConfig注册拦截器）
**preHandle：**
拦截请求验证登录用户：通过cookie查ticket是否有效
本次请求保有用户信息：查询User并保存到本次请求的线程hostHolder
**postHandle：**
模板渲染前要有用户信息。因此在模板前，将hostHolder中的user存到ModeAndView里
**afterCompletion：**
请求结束，清除hostHolder中登录用户数据

### ThreadLocal<T>

**项目中作用**
存放登录用户对象的容器，能线程隔离。

#### 定义

ThreadLocal提供一个线程（Thread）局部变量副本，每个线程对该变量都有自己的副本。

- 内部类TreadLocalMap，实现线程隔离的机制，键（ThreadLocal）值（副本）对方式存储每个线程的副本

定义在Thread中，由threadLocal维护。threadLocal接口方法基于该数据结构。

- set(T value)：将此线程局部变量的当前线程副本中的值设置为指定值。

```java
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);//取map
    if (map != null)
        map.set(this, value);//有map则直接set
    else
        createMap(t, value);//没有map则新建map再set
}
```

- get()：返回此线程局部变量的当前线程副本中的值。

```java
public T get() {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);//取map
    if (map != null) {
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value;//有Entry则返回get的值
            return result;
        }
    }
    return setInitialValue();//没有Entry则返回init的值
}
```

- initialValue()：返回此线程局部变量的当前线程的“初始值”null。

一般需要子类重写，用以返回非null自定义的值。

- remove()：移除此线程局部变量当前线程的值。

可以不显式调用：一个线程结束后，它的局部变量就会被gc。
**ps**：

- ThreadLocal实例本身不存储值，仅作为值的key。
- Thread有一个threadLocalMap，一个threadLocalMap含多个threadLocal
- ThreadLocal与线程同步机制：threadlocal—创建多个副本；线程同步机制—多个线程共享同一个变量
- 泛型T为引用类型时，每个局部变量初始化时都指向同一个对象，此时threadLocal隔离失效。

![image.png](https://cdn.nlark.com/yuque/0/2023/png/35542265/1688372272894-637197c7-15c1-48f3-995c-fbec0a3a395b.png#averageHue=%23f4f4f4&clientId=u2d509ff9-22a1-4&from=paste&height=299&id=ude8f455d&originHeight=353&originWidth=780&originalType=url&ratio=1.5&rotation=0&showTitle=false&size=67255&status=done&style=none&taskId=u1237885c-facd-43ca-a99a-3521e0dbec1&title=&width=660)

### Redis与ThreadLocal缓存用户信息不同

redis是跨不同的请求的，而使用ThreadLocal具体针对的是一次请求。在这次请求中去存储用户信息，方便程序的开发，比如说请求了帖子详情页面，去做评论或者回复，就可以直接从threadLocal中取到用户的信息，进行编码。

## 三、过滤敏感词—Trie树

占空间大，但时间效率高O(len).
数据结构：
每个节点代表一个字符（根节点无字符），标记是否单词结尾
每个节点全部子节点用Map<Character,TrieNode>存，且对应字符不重复
操作：
添加/查询字符子节点
设置/查询是否单词结尾
**初始化：**加载keywords文件每行单词读出成String，按字符添加进树。
**过滤：**双指针

## 四、发帖、评论与私信

### 内容防注入

HtmlUtils.htmlEscape();：对于发布的内容标题，按字面不转义存字符串，防止xss注入

### Comment表设计

将对不同目标的评论统一到一张表

| **id** | **user_id****发评论者** | **entity_type****评论对象类型** | **entity_id****评论对象id** | **target_id****评论对象作者** | **content** | **status** | **create_time** |
| ------ | ----------------------- | ------------------------------- | --------------------------- | ----------------------------- | ----------- | ---------- | --------------- |
|        |                         |                                 |                             |                               |             |            |                 |

### 事务

添加评论 和 更新评论数量 作为一个事务。@Transactional(isolation = ..., propagation = ...)
[事务隔离级别选择](https://zhuanlan.zhihu.com/p/59061106)——结论：互联网选择RC：快，二丢和不可重复读可接受。

### Message表设计

| **id** | **form_id** | **to_id** | **conversion_id****111_112(id小的在前)** | **content** | **status****0未读 1已读 2删除** | **create_time** |
| ------ | ----------- | --------- | ---------------------------------------- | ----------- | ------------------------------- | --------------- |
|        |             |           |                                          |             |                                 |                 |

### 复杂sql—查询私信分组列表

即查询每组对话的最新消息：

```sql
-- List<Message> selectConversations(int userId, int offset, int limit);
<select id="selectConversations" resultType="Message">
select <include refid="selectFields"></include> from message
where id in (
          select max(id) --每组最新的一条
  				from message
          where status != 2 and from_id != 1 --非系统通知 && 未删除
          and (#{userId} = from_id or #{userId} = to_id) --是发送或接收方
          group by conversation_id ) --每组私信
order by id desc --组间从新到旧排序
limit #{offset}, #{limit}
</select>
```

## 五、日志 异常处理

### 日志

aop实现对service层所有的业务方法记录日志（拦截器主要针对的是controller）
@Aspect——切面类
@Pointcut("execution(* com.nc.nccommunity.service.*.*(..))")——定义切点
@Before("切点方法名")——前置通知方法

### 异常处理

@ControllerAdvice (annotations = Controller.class)——全局配置类注解
@ExceptionHandler ——异常处理方法注解，Controller异常后调用该方法处理捕获到的异常

## 六、点赞关注

### 点赞

**点赞**

| **Key**                         | **Value**   |
| ------------------------------- | ----------- |
| like:entity:entityType:entityId | set(userId) |

用set：isMember判断点赞/取消赞；size得到实体赞的数量
我收到的赞：
点赞时同样需要记录点赞实体的用户id
**某个用户收到的赞**

| **Key**          | **Value** |
| ---------------- | --------- |
| like:user:userId | int       |

### 关注、取消关注

| **Key**                    | **Value** |
| -------------------------- | --------- |
| follower:userId:entityType | Zset      |

entityType用于将来扩展业务，关注人外的其他实体
zset按关注时间排序
关注/粉丝数——zCard
关注/粉丝列表——range/reverseRange

### Redis事务

赞、赞数量增加 作为一个事务
关注、粉丝增加 作为一个事务

```java
redisTemplate.execute(new SessionCallback() {
    @Override
    public Object execute(RedisOperations operations) throws DataAccessException {
        operations.multi();//开启事务
        ...事务操作
        return operations.exec();//执行事务
    }
});
```

## 七、消息队列—实现系统通知 和 ES数据同步

评论、点赞、关注相关的系统通知，频繁但不需要立即发送。

```java
//生产者: 生产事件时，传入事件对象
public void fireEvent(Event event){
	template.send(event.getTopic(), JSONObject.toJSONString(event));
}
//消费者: 从ConsumerRecord中获取数据record.value()
@KafkaListener(topics = {"...",...})
public void handleMessage(ConsumerRecord record) {
    ...
}
```

增删帖事件用于同步ES数据。

## 八、数据统计

### UV独立访客

统计排重后大概的的用户IP数，拦截器实现

| **Key**              | **Value** |
| -------------------- | --------- |
| uv:date              |           |
| uv:startData:endData | ip        |

HyperLogLog性能好，且存储空间小，不精确的大概统计
统计结果——opsForHyperLogLog().size(redisKey)
计入——opsForHyperLogLog().add(redisKey, ip)
结果加和——opsForHyperLogLog().union(redisKey, key数组);

### DAU日活跃用户

| **Key**               | **Value**              |
| --------------------- | ---------------------- |
| dau:date              |                        |
| dau:startDate:endDate | 第userId位：true/false |

Bitmap，性能好，精确统计，适合存储大量连续布尔值
bitmap以bytes[]形式的key访问
计入——opsForValue().setBit(redisKey, userId即偏移量, 布尔值);
结果OR运算

```java
Object obj = redisTemplate.execute(new RedisCallback() {
	@Override
	public Object doInRedis(RedisConnection connection) throws DataAccessException {
		connection.bitOp(RedisStringCommands.BitOperation.OR,//运算符选择
				redisKey.getBytes(),//保存结果的key
				list.toArray(new byte[0][0]));//参与运算的key
		return connection.bitCount(redisKey.getBytes());
	}
});
return (long)obj;
```
