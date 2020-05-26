[TOC]

# 概述
LVT4J-RBAC是以RBAC(role-based access control,基于角色的权限控制)为核心思想制作的权限管理系统。适用于在一个封闭的企业环境内有多个项目产品需要进行人员权限管理时的情况。
# 特点
LVT4J-RBAC的使用方式不是高度集成到需要权限控制的项目中，而是建立一个权限控制的中心服务，其他项目通过引入一个客户端或者调用中心服务的RESTfull接口来查询和控制用户的权限。
# 名词约定
## 产品(product)
任何需要使用LVT4J-RBAC来进行权限控制的项目即称为产品
## 用户(user)
受权限约束的产品的使用者即为用户。在LVT4J-RBAC中，用户是全局的，与产品无关。即用户既可以使用产品A，又可以使用产品B，但在这两个产品下的权限不同。
## 配置项(param)
由于一个用户可以访问多个产品，因此用户在不同产品下可能会需要不同的属性。如产品‘转账系统’会需要用户提供转账账号，而产品‘邮件系统’会需要用户提供邮箱地址，此处的‘转账账号’及‘邮箱地址’表现为用户的属性，这个属性对于不同的产品，称为产品下用户的配置项。
## 访问项(access)
对于一个WEB产品，用户可访问的一个或一批URI称为访问项。在LVT4J-RBAC授权中心中，用正则来表示。
## 授权项(permission)
授权项表现为用户可以在产品中做哪些事。如在一个财务系统中，用户A和用户B都可以进入一个访问项‘订单列表’，但是用户A可以导出订单，可以看到订单的金额，而用户B不能导出订单，不能看到订单金额。此处的‘导出订单’及‘看到订单的金额’即为授权项。
## 角色(role)
一个角色是一批访问项和授权项的集合。如在一个财务部门中，所有员工都有访问项‘查看订单列表’，访问项‘查看金额统计’，授权项‘导出订单’。此时可以创建一个角色‘财会人员’来包含以上的访问项和授权项。这样当部门人员转出时，直接移除其角色‘财会人员’，当部门有新员工转入时，为其分配角色‘财会人员’，即可方便批量调整用户权限。
## 游客(visitor)
有时有些产品会希望未登陆用户，或未在授权中心注册的用户也可以使用一部分功能，此时这些用户被称为游客。

# 使用方法
## 启动授权中心
### 容器方式启动
```
docker run -p 80:80 lvq410/rbac:latest
```
### 源代码启动
依赖:JAVA1.8+、gradle
```shell
git clone https://github.com/lvq410/LVT4J-RBAC.git rbac
cd ./rbac/RBAC-Center
sh ./run/start.sh
```
### 数据库模式
内置两种数据库模式：SQLite（默认）和H2

SQLite模式只能单例部署，若要达成HA，需要使用H2模式

#### H2模式的集群部署
H2数据库模式提供一个master-slave模式的集群部署方式。

其中master节点包含数据库，只能部署一个。slave节点可以部署多个，连接并使用master节点的数据库。

master节点部署配置
```
db.type=h2 #数据库类型
db.folder=./ #数据库文件位置
db.h2.master=true #是master
```
salve节点部署配置
```
db.type=h2 #数据库类型
db.folder=./ #master节点的数据库文件位置
db.h2.master=false #是slave
db.h2.master.host=master-host #master节点地址
```
#### 备份
默认启动备份，相关配置参考`配置说明`

### 配置说明
采用Spring配置方式，可以用环境变量方式（如db_type=h2）修改
```
db.type=sqlite/h2 #数据库类型
db.folder=./ #数据库文件夹

db.backup.folder=./backup #数据库备份文件夹
db.backup.cron=0 0 0 * * * #数据库定时备份cron
db.backup.max=10 #备份文件最多保留数量

db.h2.master=true #是否h2 master节点
db.h2.web.port=8082 #master节点时的数据库web管理端口
db.h2.tcp.port=9123 #master节点时的数据库tcp端口
db.h2.master.host=localhost #slave节点时要连接的master节点的地址
db.h2.master.tcp.port=${db.h2.tcp.port} #slave节点时要连接的master节点的tcp端口

admin.userId=pwd #管理员账户密码
```

### 管理员账户密码管理
Http Basic Auth验权模式。可以配置在Spring配置里，也可以配置在./config/admin.properties里。admin.properties支持实时修改生效。有相同账号时，以Spring配置的为准。支持配置多个管理员账号。H2集群模式时，要注意各个节点的配置尽量一致，否则做同一域名的反向代理时会有问题。

## 接入授权中心
注意LVT4J-RBAC并不负责处理计算用户ID，因此查询用户权限需各项目自己提供用户的ID
### 使用RBAC-Client的jar包方式
#### 加入client的jar依赖
需要JAVA1.6+
**TODO**
#### 使用Spring的`HandlerInterceptor`
RBAC-Client可使用`org.springframework.web.servlet.HandlerInterceptor`的方式来拦截用户访问和注入用户权限信息。要使用该方法，需要继承`com.lvt4j.rbac.RbacInterceptor`类，并实现其方法`getUserId`，同时配置文件中声明其产品ID。
```java
public class ExampleInterceptor extends RbacInterceptor {
    @Override
    protected String getUserId(HttpServletRequest request,
            HttpServletResponse response) {
        return null;//返回用户的ID，若用户未登录，返回null即可
    }
}
```
Spring配置
```xml
<mvc:interceptor>
    <mvc:mapping path="/**"/>
    <bean class="ExampleInterceptor">
        <!-- 在授权中心注册的产品ID,会使用该产品下信息进行验权 -->
        <property name="proId" value="exampleProId"/>

        <!-- 除了proId必填外，还提供了以下可选配置 -->
        
        <!-- 最大为多少用户缓存权限,默认1000个 -->
        <property name="cacheCapacity" value="1000"/>
        <!-- 与授权中心同步使用的协议,默认http -->
        <property name="rbacCenterProtocol" value="http"/>
        <!-- 授权中心服务地址,[host](:[port])形式,默认127.0.0.1:80 -->
        <property name="rbacCenterAddr" value="127.0.0.1:80"/>
        <!-- 与授权中心服务同步时间间隔,单位分钟,默认5分钟 -->
        <property name="rbacCenterSyncInterval" value="5"/>
        <!-- 与授权中心同步超时时间,单位毫秒,默认200ms -->
        <property name="rbacCenterSyncTimeout" value="200"/>
    </bean>
</mvc:interceptor>
```
##### Spring Interceptor的特殊控制
基于Spring-MVC框架丰富的特性，提供了一些特殊的权限控制方式
###### 注解 `@RbacIgnore`
对于系统内某些资源，有时候不想让其纳入权限控制中，一个办法是将该资源在访问项中配置，并分配给游客。但如果这种资源很多，一个一个加会很麻烦。`@RbacIgnore`注解的作用是当该注解配置在controller(或者controller的handlemethod上)时，该controller下的所有handlemethod(或指定的handlemethod)控制的资源都不会被权限控制所限制，任何人都可以访问。
###### 注解 `@RegisteredIgnore`
对于系统内某些资源，有时只想让已在权限中心注册的用户可访问。一个办法是将该资源在访问项中配置，并挨个分配给用户。但，一个个分配会比较麻烦。`@RegisteredIgnore`注解的作用是当该注解配置在controller(或者controller的handlemethod上)时，该controller下的所有handlemethod(或指定的handlemethod)控制的资源在对于已在权限中心注册的用户都可访问，无需进行访问项，授权项等的验证操作。
###### 注解 `@PermissionNeed`
有时对于某些资源的访问想要采用不仅仅是访问项的控制方式，而是同时也要增加授权项的控制时，可以使用注解 `@PermissionNeed`。作用是配置在controller(或者controller的handlemethod上)，该controller下的所有handlemethod(或指定的handlemethod)控制的资源需要指定的授权项才能访问,若有多个授权项,满足其中一个即可。
##### 使用Spring Interceptor的权限控制流程图
![Spring Interceptor的权限控制流程图](https://raw.githubusercontent.com/lvq410/LVT4J-RBAC/master/readme/RbacInterceptor-flowchart.png)

#### 或者使用Javax.servlet的`Filter`
RBAC-Client可使用`javax.servlet.Filter`的方式来拦截用户访问和注入用户权限信息。要使用该方法，需要继承`com.lvt4j.rbac.RbacFilter`类，并实现其方法`getUserId`。
```java
public class ExampleFilter extends RbacFilter {
    @Override
    protected String getUserId(HttpServletRequest request,
            HttpServletResponse response) {
        return null;//返回用户的ID，若用户未登录，返回null即可
    }
}
```
web.xml配置
```xml
<filter>
    <filter-name>rbacfilter</filter-name>
    <display-name>rbacfilter</display-name>
    <filter-class>ExampleFilter</filter-class>
    <!-- 在授权中心注册的产品ID,会使用该产品下信息进行验权 -->
    <init-param>
        <param-name>proId</param-name>
        <param-value>exampleProId</param-value>
    </init-param>

    <!-- 与Spring配置类似，除了proId必填外，还提供了以下可选配置 -->

    <!-- 最大为多少用户缓存权限,默认1000个 -->
    <init-param>
        <param-name>cacheCapacity</param-name>
        <param-value>1000</param-value>
    </init-param>
    <!-- 与授权中心同步使用的协议,默认http -->
    <init-param>
        <param-name>rbacCenterProtocol</param-name>
        <param-value>http</param-value>
    </init-param>
    <!-- 授权中心服务地址,[host](:[port])形式,默认127.0.0.1:80 -->
    <init-param>
        <param-name>rbacCenterAddr</param-name>
        <param-value>127.0.0.1:80</param-value>
    </init-param>
    <!-- 与授权中心服务同步时间间隔,单位分钟,默认5分钟 -->
    <init-param>
        <param-name>rbacCenterSyncInterval</param-name>
        <param-value>5</param-value>
    </init-param>
    <!-- 与授权中心同步超时时间,单位毫秒,默认200ms -->
    <init-param>
        <param-name>rbacCenterSyncTimeout</param-name>
        <param-value>200</param-value>
    </init-param>
</filter>
<filter-mapping>
    <filter-name>rbacfilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```
##### 使用Javax.servlet的`Filter`的权限控制流程图
![Filter的权限控制流程图](https://raw.githubusercontent.com/lvq410/LVT4J-RBAC/master/readme/RbacFilter-flowchart.png)

#### 更多控制
以上两种方式默认游客可以访问产品，以及在用户无权访问某URI时会返回比较简单的提示信息。若要改变该规则，参考如下。
##### 未登录
要改变游客默认可访问产品的规则及自定义不能访问时的提示信息时，重写`com.lvt4j.rbac.RbacInterceptor`或`com.lvt4j.rbac.RbacFilter`的`onNotLogin()`方法
```java
//游客可继续访问返回true,拦截返回false
protected boolean onNotLogin(HttpServletRequest request,
        HttpServletResponse response) throws IOException {
    //如要限制游客不能访问或自定义提示信息
    //则返回false并向response中写入提示信息
    return true;
}
```
##### 无权访问
要自定义用户无权访问时的提示信息时，重写`com.lvt4j.rbac.RbacInterceptor`或`com.lvt4j.rbac.RbacFilter`的`onNotAllowAccess()`方法
```java
protected boolean onNotAllowAccess(HttpServletRequest request,
        HttpServletResponse response) throws IOException {
    //向response中写入自定义提示信息
    return false;
 }
```
##### 授权项
对当前请求用户的授权项控制需要在各业务代码里来实现，通过调用RBAC-Client写入在`request`的`attribute`里的参数`UserAuth.ReqAttr`
```java
//如判断用户是否有转账权限
UserAuth userAuth = request.getAttribute(UserAuth.ReqAttr);
if(userAuth.permit("transfer_accounts")){
    //有权限的处理
}
```
##### 其它
Spring Intercepter提供了其他一些处理，参见Spring Intercepter的流程图。

#### 直接使用权限client端
如果在一个非web项目中或在一个项目中需要获取多个不同项目的权限，可以直接使用权限client端
```java
//创建client端
ProductAuth4Client productAuth = new ProductAuth4Client("examplePro", "127.0.0.1:80");
//判断用户是否有权限访问指定uri
productAuth.allowAccess("userId","uri");
//判断用户是否有指定授权项的权限
productAuth.permit("userId","permissionId");
//或者获取用户权限pojo
UserAuth userAuth = productAuth.getUserAuth("userId");
//不用时销毁
productAuth.destory();
```

#### 用户权限POJO
若用户通过验证，会向`request`的`attribute`里写入`key`为`UserAuth.ReqAttr //即'rbac'`的用户权限POJO，该POJO提供以下属性及方法
```java
/** 用户ID */
public String userId;
/** 用户名称 */
public String userName;
/** 用户描述 */
public String userDes;
/** 用户在授权中心是否存在 */
public boolean exist;
/** 用户的所有配置项 */
public Map<String, String> params;
/** 用户的所有角色 */
public Set<String> roles;
/** 用户的所有访问项 */
public Set<String> accesses;
/** 用户的所有授权项 */
public Set<String> permissions;

/** 用户是否有权限访问指定uri */
public boolean allowAccess(String uri) {}
/** 用户是否有指定授权项的权限 */
public boolean permit(String permissionId) {}
```
获取方法
```java
UserAuth userAuth = request.getAttribute(UserAuth.ReqAttr);
```

### 使用RESTful-API方式
授权中心提供了一些接口来查询和验证用户权限
#### 获取用户权限
[RBAC-Center-Host]/api/user/auth?proId=&userId=
#### 验证用户是否可访问
[RBAC-Center-Host]/api/user/allowAccess?proId=&userId=&uri=
#### 验证用户是否有某访问项
[RBAC-Center-Host]/api/user/permit?proId=&userId=&permissionId=
