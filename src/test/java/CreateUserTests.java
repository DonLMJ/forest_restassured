import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.ApiResponse;
import models.CreateUserModel;
import models.ErrorData;
import models.SuccessData;
import utils.TestUtils;
import utils.Attributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.GoRestService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

public class CreateUserTests {

    private Faker faker;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        objectMapper = new ObjectMapper();
    }

    private CreateUserModel createUserModel(Map<String, String> attributes) {
        return new CreateUserModel.Builder()
                .withAttributes(attributes)
                .build();
    }

    @Test
    public void Users_CreateUsers_Success() {
// Generate random attributes using AttributeGenerator for name, email, gender, and status
        Map<String, String> attributes = TestUtils.generateAttributes(
                Attributes.NAME,
                Attributes.EMAIL,
                Attributes.GENDER,
                Attributes.STATUS
        );
        // Create CreateUserModel with all fields populated using the factory method
        CreateUserModel createUserModel = createUserModel(attributes);

        // Send request and receive response
        Response response = GoRestService.createUser(createUserModel);

        // Assert success response
        TestUtils.assertSuccessfulResponse(response,SC_CREATED, attributes);
    }

    @Test
    public void Users_CreateUsers_MissingEmail() {
        Map<String, String> attributes = TestUtils.generateAttributes(
                Attributes.NAME,
                Attributes.GENDER,
                Attributes.STATUS
        );
        // Create CreateUserModel instance with missing email field using the factory method
        CreateUserModel createUserModel = createUserModel(attributes);

        // Send request and receive response
        Response response = GoRestService.createUser(createUserModel);

        // Assert Unprocessable Entity response for missing email
        TestUtils.assertUnprocessableEntityResponse(response, "email", "can't be blank");
    }

    @Test
    public void Users_CreateUsers_ExistingEmail() {
        // Create initial user with email
        Map<String, String> initialAttributes = TestUtils.generateAttributes(
                Attributes.NAME,
                Attributes.GENDER,
                Attributes.EMAIL,
                Attributes.STATUS
        );

        // Create the initial user
        CreateUserModel initialCreateUserModel = createUserModel(initialAttributes);
        Response initialResponse = GoRestService.createUser(initialCreateUserModel);
        initialResponse.then().log().all();
        // Extract email to use for duplicate user creation
        String existingEmail = initialAttributes.get("email");
        // Generate new attributes with the same email
        Map<String, String> duplicateAttributes = TestUtils.generateAttributes(
                Attributes.NAME,
                Attributes.GENDER,
                Attributes.STATUS
        );
        duplicateAttributes.put("email", existingEmail);
        // Create CreateUserModel instance with existing email field using the factory method
        CreateUserModel duplicateCreateUserModel = createUserModel(duplicateAttributes);

        // Send request to create user with existing email and receive response
        Response duplicateResponse = GoRestService.createUser(duplicateCreateUserModel);

        // Assert Unprocessable Entity response for missing email
        TestUtils.assertUnprocessableEntityResponse(duplicateResponse, "email", "has already been taken");
    }

    @Test
    public void Users_CreateUsers_MissingGender() {
        Map<String, String> attributes = TestUtils.generateAttributes(
                Attributes.NAME,
                Attributes.EMAIL,
                Attributes.STATUS
        );

        // Create CreateUserModel instance with missing gender field using the factory method
        CreateUserModel createUserModel = createUserModel(attributes);

        // Send request and receive response
        Response response = GoRestService.createUser(createUserModel);

        // Assert Unprocessable Entity response for missing gender
        TestUtils.assertUnprocessableEntityResponse(response, "gender", "can't be blank, can be male of female");
    }

    @Test
    public void Users_CreateUsers_MissingStatus() {
        Map<String, String> attributes = TestUtils.generateAttributes(
                Attributes.NAME,
                Attributes.EMAIL,
                Attributes.GENDER
        );

        // Create CreateUserModel instance with missing status field using the factory method
        CreateUserModel createUserModel = createUserModel(attributes);

        // Send request and receive response
        Response response = GoRestService.createUser(createUserModel);

        // Assert Unprocessable Entity response for missing status
        TestUtils.assertUnprocessableEntityResponse(response, "status", "can't be blank");
    }

    @Test
    public void Users_CreateUsers_WrongStatus() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("name", faker.name().fullName());
        attributes.put("email", "fake_" + faker.internet().emailAddress());
        attributes.put("gender", faker.options().option("Male", "Female"));
        attributes.put("status", "InvalidStatus");

        // Create CreateUserModel instance with wrong status field using the factory method
        CreateUserModel createUserModel = createUserModel(attributes);

        // Send request and receive response
        Response response = GoRestService.createUser(createUserModel);

        // Assert Unprocessable Entity response for wrong status
        TestUtils.assertUnprocessableEntityResponse(response, "status", "can't be blank");
    }

    @Test
    public void Users_CreateUsers_WrongEmail() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("name", faker.name().fullName());
        attributes.put("email", "InvalidEmail");
        attributes.put("gender", faker.options().option("Male", "Female"));
        attributes.put("status", faker.options().option("Active", "Inactive"));

        // Create CreateUserModel instance with wrong status field using the factory method
        CreateUserModel createUserModel = createUserModel(attributes);

        // Send request and receive response
        Response response = GoRestService.createUser(createUserModel);

        // Assert Unprocessable Entity response for wrong status
        TestUtils.assertUnprocessableEntityResponse(response, "email", "is invalid");
    }

}
