package com.feed;

import org.apache.commons.lang3.SystemUtils;
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
import java.sql.*;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AppConfig.class)
@WebIntegrationTest(randomPort = true)
public class FeedTest {

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

    @Test  //로그아웃 버튼 클릭시 제대로 로그아웃이 이루어지고 로그인페이지로 이동하는가
    public void feedLogOutBtn() throws Exception{
        String query;
        try {
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID', 0, 0, 0, false ,'TEST_PW');";
            stmt.executeUpdate(query);

            String baseURL = "http://localhost:" + port + "/login";
            driver.get(baseURL);

            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            driver.findElement(By.className("logoutBtn")).click();

            assertEquals("$주소가 제대로 호출되지 않았습니다.$", "http://localhost:" + port + "/login", driver.getCurrentUrl());
        } catch (NoSuchElementException e){
            throw new NoSuchElementException("$html이 제대로 호출되지 않았습니다.$");
        } finally {
            query = "TRUNCATE TABLE user;";
            stmt.executeUpdate(query);
        }
    }

    @Test // 로그아웃후 isEnabled가 false로 바뀌었나
    public void feedLogout() throws Exception{
        String query;
        try{
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID', 0, 0, 0, false ,'TEST_PW');";
            stmt.executeUpdate(query);

            String baseURL = "http://localhost:" + port + "/login";
            driver.get(baseURL);

            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            driver.findElement(By.className("logoutBtn")).click();

            query = "SELECT * FROM user WHERE user_id='TEST_ID';";
            ResultSet rs = stmt.executeQuery(query);

            rs.next();
            assertEquals("$isEnabled의 값이 false로 바뀌지 않았습니다.$", "0", rs.getString(4));
        } catch (NoSuchElementException e){
            throw new NoSuchElementException("$html이 제대로 호출되지 않았습니다.$");
        } finally {
            query = "TRUNCATE TABLE user;";
            stmt.executeUpdate(query);
        }
    }

    @Test // 글을 작성하면 제대로 보여지는가
    public void feedPost() throws Exception{
        String query;
        try{
            query = "Insert Into user(user_id, following, follower, posting, is_enabled, password) VALUES ('TEST_ID', 0, 0, 0, false ,'TEST_PW');";
            stmt.executeUpdate(query);

            String baseURL = "http://localhost:" + port + "/login";
            driver.get(baseURL);

            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            driver.findElement(By.name("content")).sendKeys("This is Test");
            driver.findElement(By.tagName("form")).submit();

            List<WebElement> div = driver.findElements(By.className("feedPosting"));
            assertEquals("$글이 작성 되지 않았습니다.$",1, div.size());
            WebElement p = driver.findElement(By.className("feedPostingUserId"));
            assertEquals("$작성한 글의 아이디가 일치하지 않습니다.$", "TEST_ID", p.getText());
            p = driver.findElement(By.className("feedPostingContent"));
            assertEquals("$작성한 글의 내용이 일치하지 않습니다.$", "This is Test", p.getText());
            p = driver.findElement(By.className("feedPostingNo"));
            assertEquals("$포스팅 수가 증가하지 않았습니다.$", "1", p.getText());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("$html이 제대로 호출되지 않았습니다.$");
        } finally {
            query = "TRUNCATE TABLE user;";
            stmt.executeUpdate(query);
            query = "TRUNCATE TABLE post;";
            stmt.executeUpdate(query);
        }
    }

    @Test //다른사람의 글을 feed 페이지에서 볼수있는가
    public void feedPosting() throws Exception{
        String query;
        try{
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

            String baseURL = "http://localhost:" + port + "/login";
            driver.get(baseURL);
            driver.findElement(By.name("userId")).sendKeys("TEST_ID");
            driver.findElement(By.name("password")).sendKeys("TEST_PW");
            driver.findElement(By.tagName("form")).submit();

            List<WebElement> div = driver.findElements(By.className("feedPosting"));
            assertEquals(6, div.size());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("$html이 제대로 호출되지 않았습니다.$");
        } finally {
            query = "TRUNCATE TABLE user;";
            stmt.executeUpdate(query);
            query = "TRUNCATE TABLE post;";
            stmt.executeUpdate(query);
        }
    }
}
