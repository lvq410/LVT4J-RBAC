# 概述

LVT4J-RBAC是以RBAC(role-based access control,基于角色的权限控制)为核心思想制作的权限管理系统。

#特点
LVT4J-RBAC的使用方式不是高度集成到需要权限控制的项目中，而是建立一个权限控制的中心服务，其他项目通过引入一个客户端或者调用中心服务的RESTfull接口来查询和控制用户的权限。
#名词约定
##产品(product)
任何需要使用LVT4J-RBAC来进行权限控制的项目即称为产品
##用户(user)
受权限约束的产品的使用者即为用户。在LVT4J-RBAC中，用户是全局的，与产品无关。即用户既可以使用产品A，又可以使用产品B，但在这两个产品下的权限不同。
##配置项(param)
由于一个用户可以访问多个产品，因此用户在不同产品下可能会需要不同的属性。如产品‘转账系统’会需要用户提供转账账号，而产品‘邮件系统’会需要用户提供邮箱地址，此处的‘转账账号’及‘邮箱地址’表现为用户的属性，这个属性对于不同的产品，称为产品下用户的配置项。
##访问项(access)
对于一个WEB产品，用户可访问的一个或一批URI称为访问项。在LVT4J-RBAC授权中心中，用正则来表示。
##授权项(permission)
授权项表现为用户可以在产品中做哪些事。如在一个财务系统中，用户A和用户B都可以进入一个访问项‘订单列表’，但是用户A可以导出订单，可以看到订单的金额，而用户B不能导出订单，不能看到订单金额。此处的‘导出订单’及‘看到订单的金额’即为授权项。
##角色(role)
一个角色是一批访问项和授权项的集合。如在一个财务部门中，所有员工都有访问项‘查看订单列表’，访问项‘查看金额统计’，授权项‘导出订单’。此时可以创建一个角色‘财会人员’来包含以上的访问项和授权项。这样当部门人员转出时，直接移除其角色‘财会人员’，当部门有新员工转入时，为其分配角色‘财会人员’，即可方便批量调整用户权限。
##游客(visitor)
有时有些产品会希望未登陆用户，或未在授权中心注册的用户也可以使用一部分功能，此时这些用户被称为游客。

#使用方法
##启动授权中心
**TODO**
##接入授权中心
注意LVT4J-RBAC并不负责处理计算用户ID，因此查询用户权限需要各项目自己提供用户的ID
###引入RBAC-Client的jar包
####加入client的jar依赖
**TODO**
####使用Spring的`HandlerInterceptor`
RBAC-Client可使用`org.springframework.web.servlet.HandlerInterceptor`的方式来拦截用户访问和注入用户权限信息。要使用RBAC-Client，需要继承`com.lvt4j.rbac.RbacInterceptor`类，并实现其方法`getUserId`，同时配置文件中声明其产品ID。
<pre>
public class ExampleInterceptor extends RbacInterceptor {
    @Override
    protected String getUserId(HttpServletRequest request,
            HttpServletResponse response) {
        return null;//返回用户的ID，若用户未登录，返回null即可
    }
}
</pre>
Spring配置
<pre>
&lt;mvc:interceptor&gt;
    &lt;mvc:mapping path="/**"/&gt;
    &lt;bean class="ExampleInterceptor"&gt;
        &lt;!-- 在授权中心注册的产品ID,会使用该产品下信息进行验权 --&gt;
        &lt;property name="proId" value="exampleProId"/&gt;
    &lt;/bean&gt;
&lt;/mvc:interceptor&gt;

&lt;!-- 除了proId必填外，还提供了以下可选配置 --&gt;

&lt;!-- 最大为多少用户缓存权限,默认1000个 --&gt;
&lt;property name="cacheCapacity" value="1000"/&gt;
&lt;!-- 与授权中心同步使用的协议,默认http --&gt;
&lt;property name="rbacCenterProtocol" value="http"/&gt;
&lt;!-- 授权中心服务地址,[host](:[port])形式,默认127.0.0.1:80 --&gt;
&lt;property name="rbacCenterAddr" value="127.0.0.1:80"/&gt;
&lt;!-- 与授权中心服务同步时间间隔,单位分钟,默认5分钟 --&gt;
&lt;property name="rbacCenterSyncInterval" value="5"/&gt;
&lt;!-- 与授权中心同步超时时间,单位毫秒,默认200ms --&gt;
&lt;property name="rbacCenterSyncTimeout" value="200"/&gt;
</pre>



####或者使用Javax.servlet的`Filter`
RBAC-Client可使用`javax.servlet.Filter`的方式来拦截用户访问和注入用户权限信息。要使用RBAC-Client，需要继承`com.lvt4j.rbac.RbacFilter`类，并实现其方法`getUserId`。
<pre>
public class ExampleFilter extends RbacFilter {
    @Override
    protected String getUserId(HttpServletRequest request,
            HttpServletResponse response) {
        return null;//返回用户的ID，若用户未登录，返回null即可
    }
}
</pre>
web.xml配置
<pre>
&lt;filter&gt;
    &lt;filter-name&gt;rbacfilter&lt;/filter-name&gt;
    &lt;display-name&gt;rbacfilter&lt;/display-name&gt;
    &lt;filter-class&gt;ExampleFilter&lt;/filter-class&gt;
    &lt;!-- 在授权中心注册的产品ID,会使用该产品下信息进行验权 --&gt;
    &lt;init-param&gt;
        &lt;param-name&gt;proId&lt;/param-name&gt;
        &lt;param-value&gt;exampleProId&lt;/param-value&gt;
    &lt;/init-param&gt;
&lt;/filter&gt;
&lt;filter-mapping&gt;
    &lt;filter-name&gt;rbacfilter&lt;/filter-name&gt;
    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
&lt;/filter-mapping&gt;

&lt;!-- 与Spring配置类似，除了proId必填外，还提供了以下可选配置 --&gt;

&lt;!-- 最大为多少用户缓存权限,默认1000个 --&gt;
&lt;init-param&gt;
    &lt;param-name&gt;cacheCapacity&lt;/param-name&gt;
    &lt;param-value&gt;1000&lt;/param-value&gt;
&lt;/init-param&gt;
&lt;!-- 与授权中心同步使用的协议,默认http --&gt;
&lt;init-param&gt;
    &lt;param-name&gt;rbacCenterProtocol&lt;/param-name&gt;
    &lt;param-value&gt;http&lt;/param-value&gt;
&lt;/init-param&gt;
&lt;!-- 授权中心服务地址,[host](:[port])形式,默认127.0.0.1:80 --&gt;
&lt;init-param&gt;
    &lt;param-name&gt;rbacCenterAddr&lt;/param-name&gt;
    &lt;param-value&gt;127.0.0.1:80&lt;/param-value&gt;
&lt;/init-param&gt;
&lt;!-- 与授权中心服务同步时间间隔,单位分钟,默认5分钟 --&gt;
&lt;init-param&gt;
    &lt;param-name&gt;rbacCenterSyncInterval&lt;/param-name&gt;
    &lt;param-value&gt;5&lt;/param-value&gt;
&lt;/init-param&gt;
&lt;!-- 与授权中心同步超时时间,单位毫秒,默认200ms --&gt;
&lt;init-param&gt;
    &lt;param-name&gt;rbacCenterSyncTimeout&lt;/param-name&gt;
    &lt;param-value&gt;200&lt;/param-value&gt;
&lt;/init-param&gt;
</pre>
####用户权限POJO
使用以上两种方式后，若用户通过验证，会向`request`的`attribute`里写入`key`为`UserAuth.ReqAttr(rbac)`的用户权限POJO，该POJO提供以下属性及方法
<pre>
/** 用户ID */
public String userId;
/** 用户在授权中心是否存在 */
public boolean exist;
/** 用户的所有配置项 */
public Map&lt;String, String&gt; params;
/** 用户的所有角色 */
public Set&lt;String&gt; roles;
/** 用户的所有访问项 */
public Set&lt;String&gt; accesses;
/** 用户的所有授权项 */
public Set&lt;String&gt; permissions;

/** 用户是否有权限访问指定uri */
public boolean allowAccess(String uri) {}
/** 用户是否有指定授权项的权限 */
public boolean permit(String permissionId) {}
</pre>
#####获取方法

####更多控制
以上两种方式默认游客可以访问产品，以及在用户无权访问某URI时会返回比较简单的提示信息。若要改变该规则，参考如下。
#####未登录
要改变游客默认可访问产品的规则及自定义不能访问时的提示信息时，重写`com.lvt4j.rbac.RbacInterceptor`或`com.lvt4j.rbac.RbacFilter`的`onNotLogin()`方法
<pre>
//游客可继续访问返回true,拦截返回false
protected boolean onNotLogin(HttpServletRequest request,
        HttpServletResponse response) throws IOException {
    //如要限制游客不能访问或自定义提示信息
    //则返回false并向response中写入提示信息
    return true;
}
</pre>
#####无权访问
要自定义用户无权访问时的提示信息时，重写`com.lvt4j.rbac.RbacInterceptor`或`com.lvt4j.rbac.RbacFilter`的`onNotAllowAccess()`方法
<pre>
protected boolean onNotAllowAccess(HttpServletRequest request,
        HttpServletResponse response) throws IOException {
    //向response中写入自定义提示信息
    return false;
 }
</pre>
#####授权项
对当前请求用户的授权项控制需要在各业务代码里来实现，通过调用RBAC-Client写入在`request`的`attribute`里的参数`UserAuth.ReqAttr`
<pre>
//如判断用户是否有转账权限
UserAuth
if(request.getAttribute(UserAuth.ReqAttr).permit("transfer_accounts")){
	//有权限的处理
}
</pre>
###使用RESTful-API
授权中心提供了一些接口来查询和验证用户权限
####获取用户权限
[RBAC-Center-Host]/api/user/auth?proId=&userId=
####验证用户是否可访问
[RBAC-Center-Host]/api/user/allowAccess?proId=&userId=&uri=
####验证用户是否有某访问项
[RBAC-Center-Host]/api/user/permit?proId=&userId=&permissionId=
