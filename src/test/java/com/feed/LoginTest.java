package com.feed;

import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AppConfig.class)
@WebIntegrationTest(randomPort = true)
public class LoginTest {

    @Value("${local.server.port}")
    private int port;

    static WebDriver driver;

    static Properties pro;
    static String connectionURL;
    static String username;
    static String password;

    static Connection conn;
    static Statement stmt;

    @Before
    public void setUp() throws Exception {
        File path = new File("");
        System.out.println(path.getAbsolutePath());

        pro = new Properties();
        pro.load(new FileInputStream(path.getAbsolutePath() + "/src/main/resources/application.properties"));
        connectionURL = pro.getProperty("spring.datasource.url");
        username = pro.getProperty("spring.datasource.username");
        password = pro.getProperty("spring.datasource.password");

        try {
            conn = DriverManager.getConnection(connectionURL, username, password);
            stmt = conn.createStatement();
        }
        catch (SQLException e) {
            throw new SQLException("$DB가 연결 되지 않았습니다.$");
        }

        Capabilities caps = new DesiredCapabilities();
        ((DesiredCapabilities) caps).setJavascriptEnabled(true);
        ((DesiredCapabilities) caps).setCapability("takesScreenshot", true);
        if (SystemUtils.IS_OS_WINDOWS) {
            ((DesiredCapabilities) caps).setCapability(
                    PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                    path.getAbsolutePath() + "/src/test/resources/phantomjs-2.1.1-windows/bin/phantomjs.exe"
            );
        } else if(SystemUtils.IS_OS_LINUX) {
            ((DesiredCapabilities) caps).setCapability(
                    PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                    path.getAbsolutePath() + "/src/test/resources/phantomjs-2.1.1-linux-x86_64/bin/phantomjs"
            );
        }

        driver = new PhantomJSDriver(caps);
    }

    @Test // 존재하지않는 아이디 입력시 ErrorPage를 제대로 띄우는가
    public void loginWrongID() throws Exception{
        try{
            String baseURL = "http://localhost:" + port+ "/login";
            driver.get(baseURL);

            driver.findElement(By.name("userId")).sendKeys("TEST_WRONGID");
            driver.findElement(By.name("password")).sendKeys("TEST_WRONGPW");
            driver.findElement(By.tagName("form")).submit();

            assertEquals("$에러페이지가 뜨지 않았습니다.$", "Error", driver.getTitle());

        } catch (NoSuchElementException e){
            throw new NoSuchElementException("$html이 제대로 호출되지 않았습니다.$");
        }
    }

    @Test // 틀린 비밀번호 입력시 ErrorPage를 제대로 띄우는가
    public void loginWrongPW() throws Exception{
        String query;
        try{
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID',0,0,0, false ,'TEST_PW');";
            stmt.executeUpdate(query);

            String baseURL = "http://localhost:" + port+ "/login";
            driver.get(baseURL);

            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_WRONGPW");
            driver.findElement(By.tagName("form")).submit();

            assertEquals("$에러페이지가 호출되지 않았습니다.$", "Error", driver.getTitle());
        } catch (NoSuchElementException e){
            throw new NoSuchElementException("$html이 제대로 호출되지 않았습니다.$");
        } finally {
            query = "TRUNCATE TABLE user;";
            stmt.executeUpdate(query);
        }
    }

    @Test // 값을 입력하고 '로그인'을 눌렀을 때 값이 맞는 경우 로그인 확인 isEnabled가 true로 바뀌었는가
    public void loginTest() throws Exception {
        String query;
        try {
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID',0,0,0, false ,'TEST_PW');";
            stmt.executeUpdate(query);

            String baseURL = "http://localhost:" + port+ "/login";
            driver.get(baseURL);

            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            query = "SELECT * FROM user WHERE user_id='TEST_ID';";
            ResultSet rs = stmt.executeQuery(query);

            rs.next();
            assertEquals("$isEnable이 true로 바뀌지 않았습니다.$", "1", rs.getString(4));
            assertEquals("$/feed가 호출되지 않았습니다.$", "http://localhost:" + port + "/feed", driver.getCurrentUrl());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("$html이 제대로 호출되지 않았습니다.$");
        } finally {
            query = "TRUNCATE TABLE user;";
            stmt.executeUpdate(query);
        }
    }

    @Test // 이미 로그인되어있는 계정에 중복 로그인시 ErrorPage를 제대로 띄우는가
    public void loginOverlap() throws Exception{
        String query;
        try{
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID',0,0,0, true ,'TEST_PW');";
            stmt.executeUpdate(query);

            String baseURL = "http://localhost:" + port+ "/login";
            driver.get(baseURL);

            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            assertEquals("$에러페이지가 호출되지 않았습니다.$", "Error", driver.getTitle());

        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("$html이 제대로 호출되지 않았습니다.$");
        } finally {
            query = "TRUNCATE TABLE user;";
            stmt.executeUpdate(query);
        }
    }

    @After
    public void tearDown() {
        driver.quit();
    }
}
