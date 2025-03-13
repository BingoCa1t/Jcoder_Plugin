## 只经过了简单的测试，成功提交了作业并获取到了返回的测试结果
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