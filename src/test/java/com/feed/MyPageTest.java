package com.feed;

import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.*;
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
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AppConfig.class)
@WebIntegrationTest(randomPort = true)
public class MyPageTest {

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

    @Test //"/Feed"에서 자신의 이름을 클릭했을 때 "/{userId}" 로 이동하는가
    public void moveToMyPage() throws Exception {
        String query;
        try {
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID', 0, 0, 0, false ,'TEST_PW');";
            stmt.executeUpdate(query);

            String baseURL = "http://localhost:" + port + "/login";
            driver.get(baseURL);
            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            driver.findElement(By.className("feedUserId")).click();

            assertEquals("$주소가 제대로 호출되지 않았습니다.$", "http://localhost:" + port + "/TEST_ID", driver.getCurrentUrl());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("$html이 제대로 호출되지 않았습니다.$");
        } finally {
            query = "TRUNCATE TABLE user;";
            stmt.executeUpdate(query);
        }
    }

    @Test //"/Feed"에서 자신의 이름을 클릭했을 때 아이디가 일치 하는가
    public void myPageIdMatching() throws Exception {
        String query;
        try {
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID', 0, 0, 0, false ,'TEST_PW');";
            stmt.executeUpdate(query);

            String baseURL = "http://localhost:" + port + "/login";
            driver.get(baseURL);
            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            driver.findElement(By.className("feedUserId")).click();

            WebElement p = driver.findElement(By.className("myPageUserId"));
            assertEquals("$로그인 된 아이디와 일치하지 않습니다.$", "TEST_ID", p.getText());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("$html이 제대로 호출되지 않았습니다.$");
        } finally {
            query = "TRUNCATE TABLE user;";
            stmt.executeUpdate(query);
        }
    }

    @Test //마이페이지에서 팔로잉 팔로워 수가 맞게 뜨는가
    public void myPageFollowMatching() throws Exception {
        String query;
        try {
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID', 99, 77, 0, false ,'TEST_PW');";
            stmt.executeUpdate(query);

            String baseURL = "http://localhost:" + port + "/login";
            driver.get(baseURL);
            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            baseURL = "http://localhost:" + port + "/TEST_ID";
            driver.get(baseURL);

            WebElement p = driver.findElement(By.className("myPageFollowing"));
            assertEquals("$마이페이지의 팔로잉 수가 다릅니다.$", "99", p.getText());
            p = driver.findElement(By.className("myPageFollower"));
            assertEquals("$마이페이지의 팔로워 수가 다릅니다.$", "77", p.getText());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("$html이 제대로 호출되지 않았습니다.$");
        } finally {
            query = "TRUNCATE TABLE user;";
            stmt.executeUpdate(query);
        }
    }

    @Test //홈으로 버튼 클릭 시 "/feed"로 이동하는 가
    public void myPageFeed() throws Exception {
        String query;
        try {
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID', 0, 0, 0, false ,'TEST_PW');";
            stmt.executeUpdate(query);

            String baseURL = "http://localhost:" + port + "/login";
            driver.get(baseURL);
            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            baseURL = "http://localhost:" + port + "/TEST_ID";
            driver.get(baseURL);

            driver.findElement(By.className("feedBtn")).click();
            assertEquals("$호출된 주소가 다릅니다.$", "http://localhost:" + port + "/feed", driver.getCurrentUrl());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("$html이 제대로 호출되지 않았습니다.$");
        } finally {
            query = "TRUNCATE TABLE user;";
            stmt.executeUpdate(query);
        }
    }

    @Test //로그아웃 버튼 클릭 시 "/login"로 이동하는 가
    public void myPageLogout() throws Exception {
        String query;
        try {
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID', 0, 0, 0, false ,'TEST_PW');";
            stmt.executeUpdate(query);

            String baseURL = "http://localhost:" + port + "/login";
            driver.get(baseURL);
            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            baseURL = "http://localhost:" + port + "/TEST_ID";
            driver.get(baseURL);

            driver.findElement(By.className("logoutBtn")).click();
            assertEquals("$호출된 주소가 다릅니다.$", "http://localhost:" + port + "/login", driver.getCurrentUrl());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("$html이 제대로 호출되지 않았습니다.$");
        } finally {
            query = "TRUNCATE TABLE user;";
            stmt.executeUpdate(query);
        }
    }

    @Test //마이 페이지에서 자신이 한 포스팅과 팔로잉 중인 포스팅이 뜨는가
    public void myPagePosting() throws Exception {
        String query;
        try {
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID', 0, 0, 1, false ,'TEST_PW');";
            stmt.executeUpdate(query);
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID2', 0, 0, 2, false ,'TEST_PW2');";
            stmt.executeUpdate(query);
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID3', 0, 0, 3, false ,'TEST_PW3');";
            stmt.executeUpdate(query);
            query = "Insert Into post(id, user_id, content) VALUES (1, 'TEST_ID' ,'TEST_CONTENT1');";
            stmt.executeUpdate(query);
            query = "Insert Into post(id, user_id, content) VALUES (2, 'TEST_ID2' ,'TEST_CONTENT2');";
            stmt.executeUpdate(query);
            query = "Insert Into post(id, user_id, content) VALUES (3, 'TEST_ID2' ,'TEST_CONTENT3');";
            stmt.executeUpdate(query);
            query = "Insert Into post(id, user_id, content) VALUES (4, 'TEST_ID3' ,'TEST_CONTENT4');";
            stmt.executeUpdate(query);
            query = "Insert Into post(id, user_id, content) VALUES (5, 'TEST_ID3' ,'TEST_CONTENT5');";
            stmt.executeUpdate(query);
            query = "Insert Into post(id, user_id, content) VALUES (6, 'TEST_ID3' ,'TEST_CONTENT6');";
            stmt.executeUpdate(query);
            query = "Insert Into follow(id, follower, following) VALUES (1, 'TEST_ID' ,'TEST_ID3');";
            stmt.executeUpdate(query);

            String baseURL = "http://localhost:" + port + "/login";
            driver.get(baseURL);
            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            baseURL = "http://localhost:" + port + "/TEST_ID";
            driver.get(baseURL);

            List<WebElement> div = driver.findElements(By.className("myPagePosting"));
            assertEquals("$내 포스트와 팔로잉 중인 포스트가 모두 뜨지 않았습니다.$", 4, div.size());

            WebElement p = driver.findElement(By.className("myPagePostingNo"));
            assertEquals("$포스팅 수가 맞지 않습니다.$", "1", p.getText());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("$html이 제대로 호출되지 않았습니다.$");
        } finally {
            query = "TRUNCATE TABLE user;";
            stmt.executeUpdate(query);
            query = "TRUNCATE TABLE post;";
            stmt.executeUpdate(query);
            query = "TRUNCATE TABLE follow;";
            stmt.executeUpdate(query);
        }
    }

    @After
    public void tearDown() {
        driver.quit();
    }
}
