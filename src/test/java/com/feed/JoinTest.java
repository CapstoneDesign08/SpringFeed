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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AppConfig.class)
@WebIntegrationTest(randomPort = true)
public class JoinTest {

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

        conn = DriverManager.getConnection(connectionURL, username, password);
        stmt = conn.createStatement();

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

    @Test // 회원가입으로 제대로 이동하는지
    public void moveToJoinTest() throws Exception {
        try {
            String baseURL = "http://localhost:" + port;
            driver.get(baseURL);

            driver.findElement(By.className("joinBtn")).click();

            assertEquals("주소가 제대로 호출되지 않았습니다.", "http://localhost:" + port + "/join", driver.getCurrentUrl());
        }
        catch (NoSuchElementException e) {
            throw new NoSuchElementException("html이 제대로 호출되지 않았습니다.");
        }
    }

    @Test // 회원가입에서 '뒤로'를 누르면 제대로 이동하는지
    public void joinBackBtnTest() throws Exception {
        try {
            String baseURL = "http://localhost:" + port + "/join";
            driver.get(baseURL);

            driver.findElement(By.className("joinBackBtn")).click();

            assertEquals("주소가 제대로 호출되지 않았습니다.", "http://localhost:" + port + "/", driver.getCurrentUrl());
        }
        catch (NoSuchElementException e) {
            throw new NoSuchElementException("html이 제대로 호출되지 않았습니다.");
        }
    }

    @Test // 값을 입력하고 '확인'를 눌렀을 때 주소 이동 확인
    public void joinTest() throws Exception {
        String query;
        try {
            String baseURL = "http://localhost:" + port + "/join";
            driver.get(baseURL);

            driver.findElement(By.name("id")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            assertEquals("주소가 제대로 호출되지 않았습니다.", "http://localhost:" + port + "/", driver.getCurrentUrl());
        }
        catch (NoSuchElementException e) {
            throw new NoSuchElementException("html이 제대로 호출되지 않았습니다.");
        }
        finally {
            query = "TRUNCATE TABLE post;";
            stmt.executeUpdate(query);
        }
    }

    @Test // 값을 입력하고 '가입하기'를 눌렀을 때 DB의 값을 확인
    public void joinDBTest() throws Exception {
        String query;
        try {
            String baseURL = "http://localhost:" + port + "/join";
            driver.get(baseURL);

            driver.findElement(By.name("id")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            query = "SELECT * FROM usr WHERE id='TEST_ID';";
            ResultSet rs = stmt.executeQuery(query);

            rs.next();
            String test_id = rs.getString(1);
            String test_ps = rs.getString(2);
            assertEquals("DB에 아이디가 제대로 들어가지 않았습니다.", "TEST_ID", test_id);
            assertEquals("DB에 패스워드가 제대로 들어가지 않았습니다.", "TEST_PW", test_ps);
        }
        catch (NoSuchElementException e) {
            throw new NoSuchElementException("html이 제대로 호출되지 않았습니다.");
        }
        finally {
            query = "TRUNCATE TABLE post;";
            stmt.executeUpdate(query);
        }
    }

    @After
    public void tearDown() {
        driver.quit();
    }
}
