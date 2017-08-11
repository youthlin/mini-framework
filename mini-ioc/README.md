Mini-IoC
========

Usage
-----

```java
public interface IUserDao{
    //...
}

@Bean //means this class should register to the IoC container
public class UserDao implements IUserDao{
    //implement
}

@Bean
public class UserService{
    @Bean //means this field should injected
    private IUserDao userDao;
    //...service method
}

public class App{
    public static void main(String[] args){
        Context context = new ClasspathContext(); //will auto scan 
        UserService userService = context.getBean(UserService.class);
        //...
    }
}
```
