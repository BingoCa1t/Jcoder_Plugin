## 只经过了简单的测试，成功提交了作业并获取到了返回的测试结果
## 基本原理
- JCoder使用两个参数进行身份认证：JCoderID和csrftoken，每次登录均会返回这两个参数，储存在Cookie里
- JCoder每次POST请求的Header里有两个主要参数：Cookies和x-csrftoken
1. Cookies就是JCoderID和csrftoken
2. x-csrftoken就是cookies里面的csrftoken
3. 其实csrftoken本意是一个反跨域请求的保护机制，网站用隐藏在HTML或JS里的token，阻止爬虫伪造请求与网站交互。但是这个OJ为了省事，直接把csrftoken放在Cookies里了......难绷
- JCoder后端接口在/api里，直接构造POST请求即可完成与网站交互
- 每个接口POST需要body里的参数不尽相同，用F12分析每个POST请求，然后再程序里仿照即可
## 方法
- 主体部分在静态类JCoderPlugin里，封装好了GetCookies()、Initializing()、SubmitCodeAndGetFeedback()、KeepCookiesAlive()四个方法
- GetCookies() 使用Selenium弹出浏览器界面，每隔1s查询是否获取到JCoderID和csrftoken，若用户登录成功，成功获取Cookie则关闭浏览器。
- Initializing() 加载用户课程、活动的Assignment及其题目
- SubmitCodeAndGetFeedback() 返回CodeResult类型，提供两个重载，可以传入代码字符串或代码文件路径。
- KeepCookiesAlive() 每隔500000ms向服务器发起一次请求，维持Cookie有效性
- IDEA插件模板看不懂思密达，所以只写了功能实现，并没有图形界面awa
## 类
- JCoderPlugin 封装程序主体静态方法
- Course 存储课程信息
- Assignment 存储每次作业信息
- Problem 存储每道题的信息
- TestCaseResult 存储每个测试点的结果
- CodeResult 存储每次提交的返回结果
- C#里有一个叫struct的类型，可以理解成只有变量没有方法的class，不过java里貌似没有这个类型（？