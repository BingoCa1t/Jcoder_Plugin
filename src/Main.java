import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.json.JSONObject;
import org.json.JSONArray;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
//import com.fasterxml.*;


public class Main
{

    public static void main(String[] args)
    {
        try
        {
            JCoderPlugin.GetCookies();
            JCoderPlugin.Initializing();
            CodeResult cres = JCoderPlugin.SubmitCodeAndGetFeedback(JCoderPlugin.courses.get(0).Assignments.get(0).HomeworkID,"2025spring_2_1",JCoderPlugin.courses.get(0).course_id,Paths.get("D:\\25spring_JavaA\\Assignment2\\src\\Main.java"), null);
            System.out.println(cres.resultState);
            System.out.println(cres.score);
            for (TestCaseResult testCaseResult : cres.testCaseResultList)
            {
                System.out.println(testCaseResult.title);
                System.out.println(testCaseResult.state);
                System.out.println(testCaseResult.message);
                System.out.println(testCaseResult.score);
                System.out.println(testCaseResult.time+"ms");
                System.out.println(testCaseResult.memory+"Mb");

            }
            JCoderPlugin.KeepCookiesAlive();
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}