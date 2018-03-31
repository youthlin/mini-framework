Mini-MVC
========

Usage
-----

```java
@Dao
public interface IUserDao{
    //... need MyBatis mapper xml file
    List<User> list();
}

@Service
public class UserService{
    @Resource
    private IUserDao userDao;
    
    public List<User> listUsers(){
        return userDao.list();
    }
    //...service method
}

@Controller
public class UserController{
    @Resource
    private UserService userService;
    public String index(Map<String,Object> map){
        map.put("userList", userService.listUsers());
        return "list";
    }
}

```
```html
<!doctype html>
<html  lang="zh-CN" xmlns:th="http://www.thymeleaf.org/">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta http-equiv="X-UA-Compatible" content="ie=edge">
<title>User List</title>
</head>
<body>
    <table>
        <tr  th:each="user : ${userList}">
            <td th:text="${user.id}">1</td>
            <td th:text="${user.name}">Lin</td>
        </tr>
    </table>
</body>
</html>
```

## 更新记录
- 1.1.1 版本已支持文件上传, 只需在 Controller 方法参数中写上 Part 类型的形参即可自动注入。