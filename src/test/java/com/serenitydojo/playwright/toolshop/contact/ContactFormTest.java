package com.serenitydojo.playwright.toolshop.contact;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.junit.UsePlaywright;
import com.microsoft.playwright.options.AriaRole;
import com.serenitydojo.playwright.HeadlessChromeOptions;
import com.serenitydojo.playwright.toolshop.fixtures.RecordsAllureScreenshots;
import com.serenitydojo.playwright.toolshop.catalog.pageobjects.NavBar;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@DisplayName("Contact form")
@Feature("Contact form")
@UsePlaywright(HeadlessChromeOptions.class)
public class ContactFormTest implements RecordsAllureScreenshots {

    ContactForm contactForm;
    NavBar navigate;

    @BeforeEach
    void openContactPage(Page page) {
        contactForm = new ContactForm(page);
        navigate = new NavBar(page);
        navigate.toTheContactPage();
    }


    @BeforeEach
    void setupTrace(BrowserContext context) {
        context.tracing().start(
                new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true)
        );
    }

    @AfterEach
    void recordTrace(TestInfo testInfo, BrowserContext context) {
        String traceName = testInfo.getDisplayName().replace(" ", "-").toLowerCase();
        context.tracing().stop(
                new Tracing.StopOptions()
                        .setPath(Paths.get("target/trace-" + traceName + ".zip"))
        );
    }


    @Story("Submitting a request")
    @DisplayName("Customers can use the contact form to contact us")
    @Test
    void completeForm(Page page) throws URISyntaxException {
        contactForm.setFirstName("Sarah-Jane");
        contactForm.setLastName("Smith");
        contactForm.setEmail("sarah@example.com");
        contactForm.setMessage("A very long message to the warranty service about a warranty on a product!");
        contactForm.selectSubject("Warranty");

        Path fileToUpload = Paths.get(ClassLoader.getSystemResource("data/sample-data.txt").toURI());
        contactForm.setAttachment(fileToUpload);

        Assertions.assertThat(contactForm.getAlertMessage())
                .contains("Thanks for your message! We will contact you shortly.");
    }

    @Step
    void submitTheForm(Page page) {
        recordScreenshot(page, "Submit form");
        contactForm.submitForm();
    }

    @Story("Submitting a request")
    @DisplayName("First name, last name, email and message are mandatory")
    @ParameterizedTest(name = "{arguments} is a mandatory field")
    @ValueSource(strings = {"First name", "Last name", "Email", "Message"})
    void mandatoryFields(String fieldName, Page page) {
        // Fill in the field values
        contactForm.setFirstName("Sarah-Jane");
        contactForm.setLastName("Smith");
        contactForm.setEmail("sarah@example.com");
        contactForm.setMessage("A very long message to the warranty service about a warranty on a product!");
        contactForm.selectSubject("Warranty");

        // Clear one of the fields
        contactForm.clearField(fieldName);

        submitTheForm(page);

        // Check the error message for that field
        var errorMessage = page.getByRole(AriaRole.ALERT).getByText(fieldName + " is required");

        assertThat(errorMessage).isVisible();
    }

    @Story("Submitting a request")
    @DisplayName("The message must be at least 50 characters long")
    @Test
    void messageTooShort(Page page) {

        contactForm.setFirstName("Sarah-Jane");
        contactForm.setLastName("Smith");
        contactForm.setEmail("sarah@example.com");
        contactForm.setMessage("A short long message.");
        contactForm.selectSubject("Warranty");

        submitTheForm(page);

        assertThat(page.getByRole(AriaRole.ALERT)).hasText("Message must be minimal 50 characters");
    }

    @Story("Submitting a request")
    @DisplayName("The email address must be correctly formatted")
    @ParameterizedTest(name = "'{arguments}' should be rejected")
    @ValueSource(strings = {"not-an-email", "not-an.email.com", "notanemail"})
    void invalidEmailField(String invalidEmail, Page page) {
        contactForm.setFirstName("Sarah-Jane");
        contactForm.setLastName("Smith");
        contactForm.setEmail(invalidEmail);
        contactForm.setMessage("A very long message to the warranty service about a warranty on a product!");
        contactForm.selectSubject("Warranty");

        submitTheForm(page);

        assertThat(page.getByRole(AriaRole.ALERT)).hasText("Email format is invalid");
    }

//    @AfterEach
//    public void tearDown(Page page) {
//        Allure.addAttachment("End of Test Screenshot", new ByteArrayInputStream(page.screenshot()));
//    }
}
