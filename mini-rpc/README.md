Mini-RPC
========

Usage
-----

```java
//api
public interface IUserService{
    List<User> list();
}
```

```java
//provider impl
@Rpc                        //new annotation
public class UserService{
    //@Resource
    //private IUserDao userDao;
    
    public List<User> list(){
        return new ArrayList<>();
    }
}

//provider bootstrap
public class Provider{
    public static void main(String[] args){
        Context context = new ClasspathContext("com.youthlin.example");//your package
        //...
        System.in.read();//press any key and Enter to exit
        System.exit(0);
    }
}
```

```java
//consumer
public class Consumer{
    public static void main(String[] args){
        Context context = new ClasspathContext( "com.youthlin.example");
        IUserService userService = context.getBean(IUserService.class);
        System.out.println(userService.list());//invoke remote provider's implementation
    }
}

```
