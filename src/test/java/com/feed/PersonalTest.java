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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AppConfig.class)
@WebIntegrationTest(randomPort = true)
public class PersonalTest {

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

    @Test  //"/feed"주소에서 다른사용자의 아이디 클릭시 "/{userId}" 주소로 이동하는가
    public void personalMoveToPageTest() throws Exception {
        String query;
        try{
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID', 0, 0, 0, false ,'TEST_PW');";
            stmt.executeUpdate(query);
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID2', 0, 0, 0, false ,'TEST_PW2');";
            stmt.executeUpdate(query);
            query = "Insert Into post(id, user_id, content) VALUES (1, 'TEST_ID2' ,'TEST_CONTENT');";
            stmt.executeUpdate(query);

            String baseURL = "http://localhost:" + port + "/login";
            driver.get(baseURL);
            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            driver.findElement(By.className("feedPostingUserId")).click();
            assertEquals("주소가 제대로 호출되지 않았습니다.", "http://localhost:" + port + "/TEST_ID2", driver.getCurrentUrl());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("$html이 제대로 호출되지 않았습니다.$");
        } finally {
            query = "TRUNCATE TABLE user;";
            stmt.executeUpdate(query);
            query = "TRUNCATE TABLE post;";
            stmt.executeUpdate(query);
        }
    }

    @Test //MyPage.html의 "/{userId}"주소에서 다른 사용자의 아이디 클릭시 PersonalPage.html을 띄우고 "/{userId}" 주소로 이동하는가
    public void personalPageTest() throws Exception {
        String query;
        try{
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID', 0, 0, 0, false ,'TEST_PW');";
            stmt.executeUpdate(query);
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID2', 0, 0, 1, false ,'TEST_PW');";
            stmt.executeUpdate(query);
            query = "Insert Into follow(id, follower, following) VALUES (1, 'TEST_ID' ,'TEST_ID2');";
            stmt.executeUpdate(query);
            query = "Insert Into post(id, user_id, content) VALUES (1, 'TEST_ID2' ,'TEST_CONTENT');";
            stmt.executeUpdate(query);

            String baseURL = "http://localhost:" + port + "/login";
            driver.get(baseURL);
            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            baseURL = "http://localhost:" + port + "/TEST_ID";
            driver.get(baseURL);

            driver.findElement(By.className("myPagePostingUserId")).click();
            assertEquals("주소가 제대로 호출되지 않았습니다.", "http://localhost:" + port + "/TEST_ID2", driver.getCurrentUrl());
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

    @Test //상대방 페이지에서 팔로잉 팔로워 수가 맞게 뜨는가
    public void personalPageFollowMatching() throws Exception {
        String query;
        try {
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID', 0, 0, 0, false ,'TEST_PW');";
            stmt.executeUpdate(query);
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID2', 99, 77, 0, false ,'TEST_PW2');";
            stmt.executeUpdate(query);

            String baseURL = "http://localhost:" + port + "/login";
            driver.get(baseURL);
            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            baseURL = "http://localhost:" + port + "/TEST_ID2";
            driver.get(baseURL);

            WebElement p = driver.findElement(By.className("personalFollowing"));
            assertEquals("$상대방 페이지의 팔로잉 수가 다릅니다.$", "99", p.getText());
            p = driver.findElement(By.className("personalFollower"));
            assertEquals("$상대방 페이지의 팔로워 수가 다릅니다.$", "77", p.getText());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("$html이 제대로 호출되지 않았습니다.$");
        } finally {
            query = "TRUNCATE TABLE user;";
            stmt.executeUpdate(query);
        }
    }

    @Test //상대방 페이지에서 홈으로 버튼 클릭 시 "/feed"로 이동하는 가
    public void personalPageFeed() throws Exception {
        String query;
        try {
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID', 0, 0, 0, false ,'TEST_PW');";
            stmt.executeUpdate(query);
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID2', 0, 0, 0, false ,'TEST_PW2');";
            stmt.executeUpdate(query);

            String baseURL = "http://localhost:" + port + "/login";
            driver.get(baseURL);
            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            baseURL = "http://localhost:" + port + "/TEST_ID2";
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

    @Test //마이 페이지에서 자신이 한 포스팅과 팔로잉 중인 포스팅이 뜨는가
    public void personalPosting() throws Exception {
        String query;
        try {
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID', 0, 0, 1, false ,'TEST_PW');";
            stmt.executeUpdate(query);
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID2', 0, 0, 2, false ,'TEST_PW2');";
            stmt.executeUpdate(query);
            query = "Insert Into post(id, user_id, content) VALUES (1, 'TEST_ID' ,'TEST_CONTENT1');";
            stmt.executeUpdate(query);
            query = "Insert Into post(id, user_id, content) VALUES (2, 'TEST_ID2' ,'TEST_CONTENT2');";
            stmt.executeUpdate(query);
            query = "Insert Into post(id, user_id, content) VALUES (3, 'TEST_ID2' ,'TEST_CONTENT3');";
            stmt.executeUpdate(query);

            String baseURL = "http://localhost:" + port + "/login";
            driver.get(baseURL);
            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            baseURL = "http://localhost:" + port + "/TEST_ID2";
            driver.get(baseURL);

            List<WebElement> div = driver.findElements(By.className("personalPosting"));
            assertEquals("$상대방 포스트가 모두 뜨지 않았습니다.$", 2, div.size());

            WebElement p = driver.findElement(By.className("personalPostingNo"));
            assertEquals("$상대방 포스팅 수가 맞지 않습니다.$", "2", p.getText());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("$html이 제대로 호출되지 않았습니다.$");
        } finally {
            query = "TRUNCATE TABLE user;";
            stmt.executeUpdate(query);
            query = "TRUNCATE TABLE post;";
            stmt.executeUpdate(query);
        }
    }

    @After
    public void tearDown() {
        driver.quit();
    }
}
