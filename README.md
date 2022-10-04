# **service-filter组件**
## **概述**
service-filter组件主要提供了Dubbo service的参数校验、异常拦截器以及提供了trace log生成。

## **使用说明**
### **1.添加maven二方库**

|<dependency><br>`    `<groupId>com.github.acticfox</groupId><br>`    `<artifactId>service-filter</artifactId><br>`    `<version>1.0.0-SNAPSHOT</version><br></dependency>|
| :- |

### **2.application.properties修改**
添加配置

spring.dubbo.provider.filter=RpcServiceFilter
## **3.接口定义**
例如接口定义：

|public interface HelloService {<br> <br>`    `@RpcValidator<br>`    `ResultDTO<String> hello(@NotBlank(message = "name不能为空") String name);<br> <br>}|
| :- |
hello方法的参数name不能为空，调用该接口时会产生此校验。


|@RpcValidator<br>public interface StudentService {<br> <br>`    `@RpcValidator<br>`    `ResultDTO<?> saveStudent(@Valid Student student);<br> <br>`    `ResultDTO<?> updateStudent(Student student);<br> <br>`    `ResultDTO<?> deleteStudent(String idNo);<br> <br>`    `ResultDTO<Student> queryStudent(String idNo);<br> <br>}<br> <br>public class Student extends BaseObject {<br> <br>`    `private Long id;<br> <br>`    `@NotBlank(message = "name不能为空")<br>`    `private String name;<br> <br>`    `@NotBlank(message = "studentNo不能为空")<br>`    `private String studentNo;<br> <br>`    `@FieldType(value = FieldTypeEnum.IntEnum)<br>`    `private Sex sex;<br> <br>`    `private int idType;<br> <br>`    `private String idNo;<br> <br>`    `private int score;<br> <br>`    `private Date createTime;<br> <br>`    `private Date updateTime;<br>`    `....... <br>}|
| :- |
StudentService接口中saveStudent方法时会对Student对象的属性进行校验，其中name、studentNo不能为空。

其中的参数校验支持hibernate-validator校验，可以使用其中的注解。

RpcValidator注解支持Class、Method级别，RpcValidator的validation属性默认为true，添加RpcValidator表示Interface、Method支持校验，如果修改validation属性为false，表示不支持参数校验。

### **4.日志配置**
log4j日志配置：

|log4j.logger.rpcTraceLogger=INFO,RPC-TRACE-APPENDER <br>log4j.additivity.rpcTraceLogger = false<br>log4j.appender.RPC-TRACE-APPENDER=org.apache.log4j.DailyRollingFileAp pender<br>log4j.appender.RPC-TRACE-APPENDER.File=trace.log<br>log4j.appender.RPC-TRACE-APPENDER.Threshold=INFO<br>log4j.appender.RPC-TRACE-APPENDER.encoding=UTF-8<br>log4j.appender.RPC-TRACE-APPENDER.layout=org.apache.log4j.PatternLayout<br>log4j.appender.RPC-TRACE-APPENDER.layout.ConversionPattern=%d %-5p %c {2} - %m%n|
| :- |

logback配置

|<property name="APP\_NAME" value="xxx-provider-github-com" /><br>`    `<property name="LOG\_PATH" value="${user.home}/${APP\_NAME}/logs" /><br>`    `<property name="LOG\_FILE" value="${LOG\_PATH}/application.log" /><br>`    `<property name="TRACE\_LOG\_FILE" value="${LOG\_PATH}/trace.log" /><br>`    `<property name="FILE\_LOG\_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5p %c{2} - %m%n" /><br>     <br>`    `<appender name="RPCTRACE\_LOG"<br>`        `class="ch.qos.logback.core.rolling.RollingFileAppender"><br>`        `<file>${TRACE\_LOG\_FILE}</file><br>`        `<encoder><br>`            `<pattern>${FILE\_LOG\_PATTERN}</pattern><br>`        `</encoder><br>`        `<rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy"><br>`            `<fileNamePattern>${TRACE\_LOG\_FILE}.%d{yyyy-MM-dd}.%i.log</fileNamePattern><br>`            `<maxHistory>7</maxHistory><br>`        `</rollingPolicy><br>`    `</appender>|
| :- |

## **接入FAQ**
#### **1.如何使⽤service-filter对请求参数或者返回结果链路⽇志进⾏脱敏**
![](Aspose.Words.6bd0736a-8135-4a9e-86fa-cfe73b0b9da9.001.png)

期望AuthInfoPerson作为参数或者返回结果rpc链路⽇志进⾏脱敏，具体Class定义如上。

rpc链路⽇志通过fastjson进⾏序列化，如果要进⾏脱敏⾸先在class上定义注解@JSONType。AuthInfoPerson的domainName、realName、identityCard使⽤了SecurityField注解，domainName定义@SecurityField的ignore=true表示rpc链路⽇志不会打印该字段，realName定义@SecurityField的fieldType = SecurityFieldType.REALNAME表示使⽤掩码对姓名进⾏处理，identityCard定义的@SecurityField的fieldType = SecurityFieldType.IDNO表示使⽤掩码对身份证机械能掩码处理，注解SecurityField定义了6种常⻅的掩码处理，包括MOBILE(⼿机),EMAIL(邮箱),IDNO(身份证件号),REALNAME(姓名),TELEPHONE(电话),BANKCARDI(银⾏卡),另外如果fieldType=NONE表示不会进⾏掩码处理。

注意：如果按照如上使⽤⽅式进⾏脱敏，具体rpclog⽇志⾥相关字段没有脱敏，很有可能是fastjson的版本使⽤不正确。因为脱敏使⽤的fastjson提供的ContextValueFilter，1.2.35以上版本才可以正常使⽤。
#### **2.如何使⽤service-filter对某个服务service、method忽略打印链路⽇志**
请在具体Dubbo服务service接口上定义注解@IgnoreRpcLog，表示该service所有method都不会打印具体rpc链路⽇志。

也可以在具体method上定义注解@IgnoreRpcLog，表示该⽅法不会打印rpc链路⽇志。

通常分⻚查询返回结果集数据⽐较多或者其他返回结果⽐较多场景可以选择性不打印rpc链路⽇志。


