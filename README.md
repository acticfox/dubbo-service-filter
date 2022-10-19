# **service-filter组件**
<a name="b9a0L"></a>
## **概述**
service-filter组件主要提供了Dubbo service的参数校验、异常拦截器以及提供了trace log生成。
<a name="ijS7B"></a>
## **使用说明**
<a name="IoGYq"></a>
### **1.添加maven二方库**
```xml
<dependency>
    <groupId>io.github.acticfox</groupId>
    <artifactId>service-filter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
<a name="cFW9o"></a>
### **2.application.properties修改**
添加配置<br />spring.dubbo.provider.filter=RpcProviderFilter
<a name="xyK7U"></a>
### 3.接口定义
例如接口定义：
```java
public interface HelloService {
 
    @RpcValidator
    ResultDTO<String> hello(@NotBlank(message = "name不能为空") String name);
 
}
```
hello方法的参数name不能为空，调用该接口时会产生此校验。
```java
public class Student extends BaseObject {
 
    private Long id;
 
    @NotBlank(message = "name不能为空")
    private String name;
 
    @NotBlank(message = "studentNo不能为空")
    private String studentNo;
 
    @FieldType(value = FieldTypeEnum.IntEnum)
    private Sex sex;
 
    private int idType;
 
    private String idNo;
 
    private int score;
 
    private Date createTime;
 
    private Date updateTime;
    ....... 
}

@RpcValidator
public interface StudentService {
 
    @RpcValidator
    ResultDTO<?> saveStudent(@Valid Student student);
 
    ResultDTO<?> updateStudent(Student student);
 
    ResultDTO<?> deleteStudent(String idNo);
 
    ResultDTO<Student> queryStudent(String idNo);
 
}
```

StudentService接口中saveStudent方法时会对Student对象的属性进行校验，其中name、studentNo不能为空。<br />其中的参数校验支持hibernate-validator校验，可以使用其中的注解。<br />RpcValidator注解支持Class、Method级别，RpcValidator的validation属性默认为true，添加RpcValidator表示Interface、Method支持校验，如果修改validation属性为false，表示不支持参数校验。
<a name="EVgU3"></a>
### **4.日志配置**
log4j日志配置：
```properties
log4j.logger.rpcTraceLogger=INFO,RPC-TRACE-APPENDER 
log4j.additivity.rpcTraceLogger = false
log4j.appender.RPC-TRACE-APPENDER=org.apache.log4j.DailyRollingFileAp pender
log4j.appender.RPC-TRACE-APPENDER.File=trace.log
log4j.appender.RPC-TRACE-APPENDER.Threshold=INFO
log4j.appender.RPC-TRACE-APPENDER.encoding=UTF-8
log4j.appender.RPC-TRACE-APPENDER.layout=org.apache.log4j.PatternLayout
log4j.appender.RPC-TRACE-APPENDER.layout.ConversionPattern=%d %-5p %c {2} - %m%n
```

logback配置
```xml
<property name="APP_NAME" value="xxx-provider-zhichubao-com" />
<property name="LOG_PATH" value="${user.home}/${APP_NAME}/logs" />
<property name="LOG_FILE" value="${LOG_PATH}/application.log" />
<property name="TRACE_LOG_FILE" value="${LOG_PATH}/trace.log" />
<property name="FILE_LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5p %c{2} - %m%n" />
         
<appender name="RPCTRACE_LOG"
        class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${TRACE_LOG_FILE}</file>
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${TRACE_LOG_FILE}.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxHistory>7</maxHistory>
        </rollingPolicy>
</appender>    
```

<a name="AyVBk"></a>
## **接入FAQ**
<a name="lbaDh"></a>
#### **1.如何使⽤service-filter对请求参数或者返回结果链路⽇志进⾏脱敏**
![1666169157767.jpg](https://cdn.nlark.com/yuque/0/2022/jpeg/2014122/1666169184522-734a0081-ef50-42e3-9627-a405c3826ff1.jpeg#clientId=u92d87807-52d3-4&crop=0&crop=0&crop=1&crop=1&from=ui&id=uf19d172e&margin=%5Bobject%20Object%5D&name=1666169157767.jpg&originHeight=1000&originWidth=882&originalType=binary&ratio=1&rotation=0&showTitle=false&size=81827&status=done&style=none&taskId=u6debbc2c-fd2b-4d35-9eb5-9e8785ba686&title=)<br />期望AuthInfoPerson作为参数或者返回结果rpc链路⽇志进⾏脱敏，具体Class定义如上。<br />rpc链路⽇志通过fastjson进⾏序列化，如果要进⾏脱敏⾸先在class上定义注解@JSONType。AuthInfoPerson的domainName、realName、identityCard使⽤了SecurityField注解。<br />domainName定义@SecurityField的ignore=true表示rpc链路⽇志不会打印该字段;<br />realName定义@SecurityField的fieldType = SecurityFieldType.REALNAME表示使⽤掩码对姓名进⾏处理;<br />identityCard定义的@SecurityField的fieldType = SecurityFieldType.IDNO表示使⽤掩码对身份证机械能掩码处理，注解SecurityField定义了6种常⻅的掩码处理，包括MOBILE(⼿机),EMAIL(邮箱),IDNO(身份证件号),REALNAME(姓名),TELEPHONE(电话),BANKCARDI(银⾏卡),另外如果fieldType=NONE表示不会进⾏掩码处理。<br />注意：如果按照如上使⽤⽅式进⾏脱敏，具体rpclog⽇志⾥相关字段没有脱敏，很有可能是fastjson的版本使⽤不正确。因为脱敏使⽤的fastjson提供的ContextValueFilter，1.2.35以上版本才可以正常使⽤。
<a name="CSAcm"></a>
#### **2.如何使⽤service-filter对某个服务service、method忽略打印链路⽇志**
请在具体Dubbo服务service接口上定义注解@IgnoreRpcLog，表示该service所有method都不会打印具体rpc链路⽇志。<br />也可以在具体method上定义注解@IgnoreRpcLog，表示该⽅法不会打印rpc链路⽇志。<br />通常分⻚查询返回结果集数据⽐较多或者其他返回结果⽐较多场景可以选择性不打印rpc链路⽇志。


