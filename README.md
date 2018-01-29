Mini-framework
==============
[![Build Status](https://travis-ci.org/YouthLin/mini-framework.svg?branch=master)](https://travis-ci.org/YouthLin/mini-framework)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.youthlin/mini-framework/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.youthlin/mini-framework)

```xml
<properties>
    <mini-framework.version>1.1.0</mini-framework.version>
</properties>
```
Mini-IoC
--------
A Spring-style simple Ioc container.

```xml
<dependency>
    <groupId>com.youthlin</groupId>
    <artifactId>mini-ioc</artifactId>
    <version>${mini-framework.version}</version>
</dependency>

```
examples: https://github.com/YouthLin/examples/tree/master/example-my-ioc

### JUnit Supports
```java
@RunWith(MiniRunner.class)      //Use MiniRunner to run Test
@Scan("com.youthlin.examples")  //Scan packages
public class MyServiceTest{
    @Resource
    private IHelloService helloService;
    @Test
    public void test(){
        helloService.sayHello("JUnit");
    }
}

```


Mini-MVC
--------
A Spring MVC style simple MVC framework, which supports MyBatis3, Thymeleaf, etc.

```xml
<dependency>
    <groupId>com.youthlin</groupId>
    <artifactId>mini-mvc</artifactId>
    <version>${mini-framework.version}</version>
</dependency>
```

examples: https://github.com/YouthLin/examples/tree/master/example-mini-mvc

Mini-RPC
--------
A Mini RPC framework, which supports callback, async, etc.
```xml
<dependency>
    <groupId>com.youthlin</groupId>
    <artifactId>mini-rpc</artifactId>
    <version>${mini-framework.version}</version>
</dependency>
```
examples: https://github.com/YouthLin/examples   
example-rpc-api/provider/consumer

Mini-AOP
--------
A Mini AOP framework.