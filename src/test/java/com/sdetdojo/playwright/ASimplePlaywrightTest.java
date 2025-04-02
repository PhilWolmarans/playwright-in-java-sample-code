package com.sdetdojo.playwright;

import com.microsoft.playwright.*;
import com.microsoft.playwright.impl.AssertionsTimeout;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.*;

import java.util.Arrays;

public class ASimplePlaywrightTest {

    static private Playwright playwright;
    static private Browser browser;
    static private BrowserContext browserContext;

    Page page;

    @BeforeAll
    public static void setUpBrowser(){
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)
                        .setArgs(Arrays.asList("--no-sandbox","--disable-extensions"))
        );
        browserContext = browser.newContext();
    }

    @BeforeEach
    public void setup(){
        page = browserContext.newPage();
    }

    @AfterAll
    public static void teardown(){

        browser.close();
        playwright.close();
    }

    @Test
    void shouldShowThePageTitle() {

        page.navigate("https://practicesoftwaretesting.com");
        String title = page.title();

        Assertions.assertTrue(title.contains("Practice Software Testing"));

    }

    @Test
    void shouldShowSearchTermsInTheTitle() {

        page.navigate("https://practicesoftwaretesting.com");
        page.locator("[placeholder=Search]").fill("Pliers");
        page.locator("button:has-text('Search')").click();

        int matchingProductCount = page.locator(".card").count();

        Assertions.assertTrue(matchingProductCount > 0);
    }
}
