import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
            JCoderPlugin.SubmitCodeAndGetFeedback("P001","123",null);
            JCoderPlugin.KeepCookiesAlive();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

    }
}