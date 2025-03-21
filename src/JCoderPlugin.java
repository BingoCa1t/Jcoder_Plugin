import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JCoderPlugin
{
    /// 轮询用户是否已完成登录，获取到Cookie
    private static final Lock lock = new ReentrantLock();

    /// 全局Cookies
    public static Dictionary<String, String> Cookies = new Hashtable<String, String>();

    /// Cookie字符串，用来生成报头
    public static String cookie_string;

    /// 存储用户所有Course
    public static List<Course> courses = new ArrayList<>();

    /// 生成报头，参数referer暂时无用，传入null即可。报头含浏览器标识，x-csrftoken和Cookie字符串
    public static Map<String, String> GetPostHeadersPara(String referer, String csrftoken, String cookie)
    {
        Map<String, String> map = new HashMap<>();
        /*map.put("accept","*");
        map.put("accept-language","zh-CN,zh;q=0.9");
        map.put("content-type","application/x-www-form-urlencoded");
        map.put("origin","https://oj.cse.sustech.edu.cn");
        map.put("priority","u=1,i");
        map.put("referer",referer);
        map.put("sec-ch-ua","\"Chromium\";v=\"134\", \"Not:A-Brand\";v=\"24\", \"Google Chrome\";v=\"134\"");;
        map.put("sec-ch-ua-mobile","Windows");
        map.put("sec-ch-ua-platform","\"Windows\"");
        map.put("sec-fetch-dest","empty");
        map.put("sec-fetch-mode","cors");
        map.put("sec-fetch-site","same-origin");*/
        map.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36");
        map.put("x-csrftoken", csrftoken);
        map.put("Cookie", cookie);
        return map;
    }
    //"https://oj.cse.sustech.edu.cn/api/union/my_courses_list/"
    //"page=1&offset=20"

    /// 发送POST请求
    public static HttpResponse<String> SendPostRequest(String url, String body) throws IOException, InterruptedException
    {
        HttpClient httpClient = HttpClient.newHttpClient();
        // 创建一个HTTP请求 指定URI
        Map<String, String> headBody = GetPostHeadersPara(null, Cookies.get("csrftoken"), cookie_string);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .version(HttpClient.Version.HTTP_1_1)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("user-agent", headBody.get("user-agent"))
                .setHeader("x-csrftoken", headBody.get("x-csrftoken"))
                .setHeader("Cookie", cookie_string)
                //.headers("Content-Type", "application/x-www-form-urlencoded", "user-agent", headBody.get("user-agent"), "x-csrftoken", headBody.get("x-csrftoken"), "Cookie", Cookies.toString())
                .POST(HttpRequest.BodyPublishers.ofString(body))// 使用 URI 创建请求
                .build();
        // 发送 HTTP 请求并获取响应
        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    /// 把所有json里的value转成字符串
    public static void AllJSONValueToString(JSONObject jsonObject)
    {
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext())
        { // 遍历所有的key
            String key = keys.next();
            Object value = jsonObject.get(key); // 获取对应的value
            String stringValue = String.valueOf(value); // 将value转换成字符串
            jsonObject.put(key, stringValue);
        }
    }

    /// 使用selenium弹出浏览器窗口让用户登录，获取JCoderID和csrftoken。每秒钟轮询用户是否登录成功。
    public static void GetCookies()
    {
        WebDriver driver = new ChromeDriver();
        // region 浏览器交互获取cookie
        // 打开oj登录页面
        driver.get("https://oj.cse.sustech.edu.cn/home");
        // 提示用户手动登录
        System.out.println("请在浏览器中登录账号，登录完成后按回车键继续...");
        // 获取登录后的Cookie
        Set<Cookie> cookies;
        lock.lock(); // 获取锁
        try
        {
            boolean isJcoderIDGet = false;
            boolean isCsrftokenGet = false;
            // 这里放置你的轮询逻辑
            while (true)
            {
                // 执行一些操作
                try
                {
                    Thread.sleep(1000);
                    cookies = driver.manage().getCookies();// 每秒检查一次条件
                    for (Cookie cookie : cookies)
                    {
                        if (cookie.getName().equals("csrftoken")) isCsrftokenGet = true;
                        if (cookie.getName().equals("JCoderID")) isJcoderIDGet = true;
                    }
                    if (isCsrftokenGet && isJcoderIDGet)
                    {
                        break;
                    }
                } catch (InterruptedException e)
                {
                    //Thread.currentThread().interrupt(); // 恢复中断状态
                }
            }
        } finally
        {
            lock.unlock(); // 释放锁
        }
        System.out.println("获取Cookie成功！");
        System.out.println("获取到的Cookie:");
        StringBuilder cookieString = new StringBuilder();
        for (Cookie cookie : cookies)
        {

            System.out.println(cookie.getName() + "=" + cookie.getValue());
            cookieString.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
            //JCoderID和csrftoken
            Cookies.put(cookie.getName(), cookie.getValue());
        }
        cookie_string = cookieString.toString();
        driver.close();
        // endregion
    }
    /// 初始化，获取用户的Course和其活动的Assignment
    public static void Initializing()
    {
        // 创建ChromeDriver实例
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();  // 等待用户输入回车
        try
        {
            //获取课程列表
            /* {"status": 200, "list": [{"id": 52, "course_id": "CS109-25S", "description": "Java 2025 Spring",
             "avatar": "https://www.sustech.edu.cn/uploads/logo1.png", "tags": [], "course_name": "CS109 2025 Spring",
             "join_rule": 1, "member_number": 309, "is_public": false, "organization__name": null}], "type": "my_courses", "total": 1} */
            //这里的offset似乎不取0都可以，不明白这个参数的含义
            //课程的page不太可能超出一页，故取page=1
            HttpResponse<String> responseCourse = SendPostRequest("https://oj.cse.sustech.edu.cn/api/union/my_courses_list/", "page=1&offset=20");
            JSONObject json_course = new JSONObject(responseCourse.body());
            JSONArray json_class_list = json_course.getJSONArray("list");
            for (int i = 0; i < json_class_list.length(); i++)
            {
                Course course = new Course();
                course.course_id = json_class_list.getJSONObject(i).getString("course_id");
                course.course_description = json_class_list.getJSONObject(i).getString("description");
                course.course_name = json_class_list.getJSONObject(i).getString("course_name");
                courses.add(course);
            }
            System.out.println(responseCourse.body());
            //获取每个Assignment列表(考虑到均为第一次使用此OJ的CS109同学，故暂不考虑有多门课程，至于冗余加载学完的课程的问题，后续如有其他课程也使用此OJ也许会更新）
            for (Course course : courses)
            {
                //https://oj.cse.sustech.edu.cn/api/course/homeworks/list/
                String url = "https://oj.cse.sustech.edu.cn/api/course/homeworks/list/";
                HttpResponse<String> responseAssignment = SendPostRequest(url, "page=1&offset=20&courseId=" + course.course_id + "&category=0");
                JSONObject json_assignment = new JSONObject(responseAssignment.body());
                JSONArray json_assignment_list = json_assignment.getJSONArray("list");
                /*{
                 "status": 200,
                 "total": 2,
                 "list": [
                     {
                          "state": 2,
                           "homeworkId": 302,
                            "homeworkName": "Assignment2",
                            "groupId": "",
                           "subTitle": "CS109 2025Spring",
                         "description": "",
                          "problemsCount": 3,
                           "visible": true,
                           "nextDate": "2025-03-23 23:55:00"
        },
        {
            "state": 4,
            "homeworkId": 297,
            "homeworkName": "Assignment 1",
            "groupId": "",
            "subTitle": "CS109 2025Spring",
            "description": "",
            "problemsCount": 4,
            "visible": true,
            "nextDate": "2025-03-11 23:55:00"
        }
    ],
    "creatable": false
}
                  */
                for (int i = 0; i < json_assignment_list.length(); i++)
                {
                    AllJSONValueToString(json_assignment_list.getJSONObject(i));
                    Assignment assignment = new Assignment();
                    assignment.state = json_assignment_list.getJSONObject(i).getString("state");
                    assignment.HomeworkID = json_assignment_list.getJSONObject(i).getString("homeworkId");
                    assignment.HomeworkName = json_assignment_list.getJSONObject(i).getString("homeworkName");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    assignment.nextDate = sdf.parse(json_assignment_list.getJSONObject(i).getString("nextDate").split(" ")[0]);
                    course.Assignments.add(assignment);
                    //猜测state参数：2应该是In Progress，3是Countdown，4是Closed，那1应该是未发布？只请求仍在开放时间内的Assignment。
                    if (assignment.state.equals("2") || assignment.state.equals("3"))
                    {
                        //Url:https://oj.cse.sustech.edu.cn/api/homework/problems/info/
                        //POST:homeworkId=302&courseId=CS109-25S

                        String url2 = "https://oj.cse.sustech.edu.cn/api/homework/problems/list/";
                        HttpResponse<String> responseProblem = SendPostRequest(url2, "homeworkId=" + assignment.HomeworkID + "&courseId=" + course.course_id);
                        /*{
    "status": 200,
    "list": [
        {
            "problemId": "2025spring_2_1",
            "problemName": "Missing Records",
            "category": 0
        },
        {
            "problemId": "2025spring_2_2",
            "problemName": "Data Repair",
            "category": 0
        },
        {
            "problemId": "2025spring_2_3",
            "problemName": "Two-Dimensional Data Recovery",
            "category": 0
        }
    ]
}*/
                        JSONObject json_problem = new JSONObject(responseProblem.body());
                        JSONArray json_problem_list = json_problem.getJSONArray("list");
                        for (int j = 0; j < json_problem_list.length(); j++)
                        {
                            AllJSONValueToString(json_problem_list.getJSONObject(j));
                            Problem problem = new Problem();
                            problem.ProblemId = json_problem_list.getJSONObject(j).getString("problemId");
                            problem.ProblemName = json_problem_list.getJSONObject(j).getString("problemName");
                            //POST:problemId=2025spring_2_1&homeworkId=302&courseId=CS109-25S
                            String url3 = "https://oj.cse.sustech.edu.cn/api/homework/problems/info/";
                            HttpResponse<String> responseProblemInfo = SendPostRequest(url3, "problemId=" + problem.ProblemId + "&homeworkId=" + assignment.HomeworkID + "&courseId=" + course.course_id);
                            /*
                            * {
    "status": 200,
    "content": "MarkDown"
	"problemType": "Java Only",
    "timeLimit": {
        "Java": 5000
    },
    "memoryLimit": {
        "Java": 128
    },
    "ioMode": 0,
    "difficulty": 2,
    "publicTags": [],
    "privateTags": []
}*/
                            JSONObject json_problemInfo = new JSONObject(responseProblemInfo.body());
                            AllJSONValueToString(json_problemInfo);
                            problem.ProblemContent = json_problemInfo.getString("content");
                            problem.ProblemType = json_problemInfo.getString("problemType");
                            problem.Difficulty = json_problemInfo.getString("difficulty");
                            problem.IOMode = json_problemInfo.getString("ioMode");
                            problem.TimeLimit = json_problemInfo.getString("timeLimit") + "ms";
                            problem.MemoryLimit = json_problemInfo.getString("memoryLimit") + "Mb";
                            assignment.Problems.add(problem);
                            System.out.println(problem.ProblemId);
                        }
                    }
                }
            }
            //System.out.println(courses.getFirst().);
            //获取Problem列表
            //获取题目
        } catch (IOException | InterruptedException | ParseException e)
        {
            System.out.println(e.getMessage());
        } finally
        {
        }
    }

    /// 提交代码，此重载需提供代码String文本，其中codeType因为都是java，调用时传入null即可
    public static CodeResult SubmitCodeAndGetFeedback(String homeworkId, String problemId, String courseId, String code, String codeType) throws IOException, InterruptedException
    {
        JSONObject codeJsonObject = new JSONObject();
        codeJsonObject.put("Main", code);
        String body = "homeworkId=" + homeworkId + "&problemId=" + problemId + "&courseId=" + courseId + "&language=2&subGroup=false" + "&files=" + java.net.URLEncoder.encode(codeJsonObject.toString());
        //problemId=P001&language=2&files=%7B%22Main%22%3A%22212%22%7D
        HttpResponse<String> res = SendPostRequest("https://oj.cse.sustech.edu.cn/api/homework/submit/objective/", body);

        JSONObject json_res = new JSONObject(res.body());
        JCoderPlugin.AllJSONValueToString(json_res);
        String recordId = json_res.getString("recordId");
        while(true)
        {
            HttpResponse<String> responseRecord = JCoderPlugin.SendPostRequest("https://oj.cse.sustech.edu.cn/api/record/result/", "recordId=" + recordId + "&courseId=" + courseId + "&homeworkId=" + homeworkId);
            JSONObject json_record = new JSONObject(responseRecord.body());
            System.out.println(responseRecord.body());

            JSONArray json_record_list = json_record.getJSONArray("resultList");
            //JCoderPlugin.AllJSONValueToString(json_record);
            CodeResult cres = new CodeResult(json_record.getString("problemId"));
            cres.resultState = json_record.getString("resultState");
            if(cres.resultState.equals("JG"))
            {
                Thread.sleep(1000);
                continue;
            }
            cres.score = json_record.getInt("score");
            for (int i = 0; i < json_record_list.length(); i++)
            {
                TestCaseResult tres = new TestCaseResult();
                JSONObject obj = json_record_list.getJSONObject(i);
                tres.title = obj.getString("title");
                tres.memory = obj.getInt("memory");
                tres.time = obj.getInt("time");
                tres.message = obj.getString("message");
                tres.state = obj.getString("state");
                tres.score = obj.getInt("score");
                cres.testCaseResultList.add(tres);
            }
            return cres;
        }
    }
    /// 提交代码，此重载需提供代码文件路径，其中codeType因为都是java，调用时传入null即可
    public static CodeResult SubmitCodeAndGetFeedback(String homeworkId, String problemId, String courseId, Path codePath, String codeType)
    {
        try
        {
            StringBuilder codeStringBuilder = new StringBuilder();
            List<String> lines = Files.readAllLines(codePath);
            for (String line : lines)
            {
                codeStringBuilder.append(line+"\r\n");
            }
            return SubmitCodeAndGetFeedback(homeworkId, problemId, courseId, codeStringBuilder.toString(), null);
        } catch (IOException | InterruptedException e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }
    /// 每500000ms像服务器发送一个查询，保持Cookies不变
    public static void KeepCookiesAlive() throws IOException, InterruptedException
    {
        Thread t = new Thread(() ->
        {
            for (Course course : courses)
            {
                //https://oj.cse.sustech.edu.cn/api/course/homeworks/list/
                String url = "https://oj.cse.sustech.edu.cn/api/course/homeworks/list/";
                try
                {
                    HttpResponse<String> responseAssignment = SendPostRequest(url, "page=1&offset=20&courseId=" + course.course_id + "&category=0");
                    Thread.sleep(500000);
                } catch (IOException e)
                {
                    System.out.println(e.getMessage());
                } catch (InterruptedException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        });
        t.start();
    }
}
