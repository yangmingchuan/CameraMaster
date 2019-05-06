package camera.cn.cameramaster.server;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.yanzhenjie.andserver.annotation.Addition;
import com.yanzhenjie.andserver.annotation.CookieValue;
import com.yanzhenjie.andserver.annotation.FormPart;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PathVariable;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestBody;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.ResponseBody;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.cookie.Cookie;
import com.yanzhenjie.andserver.http.multipart.MultipartFile;
import com.yanzhenjie.andserver.http.session.Session;
import com.yanzhenjie.andserver.util.MediaType;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.List;

import camera.cn.cameramaster.server.model.UserInfo;
import camera.cn.cameramaster.server.util.FileUtils;


/**
 * 测试控制器
 *
 * @packageName: ymc.cn.servertest
 * @fileName: TestController
 * @date: 2019/4/23  16:15
 * @author: ymc
 * @QQ:745612618
 */

@RestController
@RequestMapping(path = "/test")
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TestController {

    private static final String TAG = "TestController";

    private static final String LOGIN_ATTRIBUTE = "USER.LOGIN.SIGN";

    @ResponseBody
    @GetMapping("/take")
    public void takeCamera() {
        EventBus.getDefault().post(new AnyEventType());
    }

    @GetMapping(path = "/get/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String info(@PathVariable(name = "userId") String userId) {
        return userId;
    }

    @PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String login(HttpRequest request, HttpResponse response, @RequestParam(name = "account",required = false
    ) String account, @RequestParam(name = "password",required = false) String password) {
        Session session = request.getValidSession();
        session.setAttribute(LOGIN_ATTRIBUTE, true);

        Cookie cookie = new Cookie("account", account + "=" + password);
        response.addCookie(cookie);
        return "login successful";
    }

    @Addition(stringType = "login", booleanType = true)
    @GetMapping(path = "/userInfo", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    UserInfo userInfo(@CookieValue("account") String account) {
        Log.e(TAG, "Account: " + account);
        UserInfo userInfo = new UserInfo();
        userInfo.setmUserId("123");
        userInfo.setmUserName("AndServer");
        return userInfo;
    }

    @PostMapping(path = "/upload", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String upload(@RequestParam(name = "header") MultipartFile file) throws IOException {
        File localFile = FileUtils.createRandomFile(file);
        file.transferTo(localFile);
        return localFile.getAbsolutePath();
    }

    @GetMapping(path = "/consume", consumes = {"application/json", "!application/xml"})
    String consume() {
        return "Consume is successful";
    }

    @GetMapping(path = "/produce", produces = {"application/json; charset=utf-8"})
    String produce() {
        return "Produce is successful";
    }

    @GetMapping(path = "/include", params = {"name=123"})
    String include(@RequestParam(name = "name") String name) {
        return name;
    }

    @GetMapping(path = "/exclude", params = "name!=123")
    String exclude() {
        return "Exclude is successful.";
    }

    @GetMapping(path = {"/mustKey", "/getName"}, params = "name")
    String getMustKey(@RequestParam(name = "name") String name) {
        return name;
    }

    @PostMapping(path = {"/mustKey", "/postName"}, params = "name")
    String postMustKey(@RequestParam(name = "name") String name) {
        return name;
    }

    @GetMapping(path = "/noName", params = "!name")
    String noName() {
        return "NoName is successful.";
    }

    @PostMapping(path = "/formPart")
    String forPart(@FormPart(name = "user") UserInfo userInfo) {
        return JSON.toJSONString(userInfo);
    }

    @PostMapping(path = "/jsonBody")
    String jsonBody(@RequestBody UserInfo userInfo) {
        return JSON.toJSONString(userInfo);
    }

    @PostMapping(path = "/listBody")
    String jsonBody(@RequestBody List<UserInfo> infoList) {
        return JSON.toJSONString(infoList);
    }
}
