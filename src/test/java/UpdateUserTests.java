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
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED;
import static org.apache.http.HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE;


public class UpdateUserTests {

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


    private Map<String, String> createUserAndReturnContext() {
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

        assertThat(initialResponse.getStatusCode(), equalTo(SC_CREATED));

        // Parse the response body
        ApiResponse<SuccessData> apiResponse = TestUtils.parseResponse(initialResponse, new TypeReference<ApiResponse<SuccessData>>() {});
        SuccessData successData = apiResponse.getData();
        assertThat(successData.getId(), notNullValue());

        // Return attributes with the generated ID
        Map<String, String> context = new HashMap<>(initialAttributes);
        context.put("id", String.valueOf(successData.getId()));
        context.put("name", String.valueOf(successData.getName()));
        context.put("status", String.valueOf(successData.getStatus()));
        context.put("email", String.valueOf(successData.getEmail()));
        context.put("gender", String.valueOf(successData.getGender()));
        return context;
    }


    @Test
    public void Users_UpdateUsers_Success() {
        // Create initial user with email
        Map<String, String> context = createUserAndReturnContext();
        // Extract user ID from the initial creation response and convert it to an integer
        int userId = Integer.parseInt(context.get("id"));
        String email = "fake_" + faker.internet().emailAddress();
        //Prepare update attributes
        Map<String, String> updateAttributes = Map.of(
                "name", "Allasani Peddana",
                "status", "active",
                "email", email,
                "gender", "female");
        CreateUserModel updateUserModel = createUserModel(updateAttributes);

        //Update the user
        Response updateResponse = GoRestService.updateUser(userId, updateUserModel);

        TestUtils.assertSuccessfulResponse(updateResponse, SC_OK, updateAttributes);
    }


    @Test
    public void Users_UpdateUsers_ExistingEmail() {
        // Create initial user with email
        Map<String, String> context = createUserAndReturnContext();
        // Extract user ID from the initial creation response and convert it to an integer
        int userId = Integer.parseInt(context.get("id"));
        //Prepare update attributes
        Map<String, String> updateAttributes = Map.of(
                "email", "email@email.com"
        );
        CreateUserModel updateUserModel = createUserModel(updateAttributes);

        //Update the user
        Response updateResponse = GoRestService.updateUser(userId, updateUserModel);

        assertThat(updateResponse.getStatusCode(), equalTo(SC_UNPROCESSABLE_ENTITY));
        TestUtils.assertUnprocessableEntityResponse(updateResponse, "email", "has already been taken");
    }

    @Test
    public void Users_UpdateUsers_WrongStatus() {
        // Create initial user with email
        Map<String, String> context = createUserAndReturnContext();
        // Extract user ID from the initial creation response and convert it to an integer
        int userId = Integer.parseInt(context.get("id"));
        //Prepare update attributes
        Map<String, String> updateAttributes = Map.of(
                "status", "wrongStatus"
        );
        CreateUserModel updateUserModel = createUserModel(updateAttributes);

        //Update the user
        Response updateResponse = GoRestService.updateUser(userId, updateUserModel);

        assertThat(updateResponse.getStatusCode(), equalTo(SC_UNPROCESSABLE_ENTITY));
        TestUtils.assertUnprocessableEntityResponse(updateResponse, "status", "can't be blank");


    }

    @Test
    public void Users_UpdateUsers_EmptyBody() {
        // Create initial user with email
        Map<String, String> context = createUserAndReturnContext();
        // Extract user ID from the initial creation response and convert it to an integer
        int userId = Integer.parseInt(context.get("id"));
        // Prepare update attributes
        Map<String, String> updateAttributes = Map.of();
        CreateUserModel updateUserModel = createUserModel(updateAttributes);

        //Update the user
        Response updateResponse = GoRestService.updateUser(userId, updateUserModel);

        TestUtils.assertSuccessfulResponse(updateResponse, SC_OK, updateAttributes);
    }

    @Test
    public void Users_UpdateUsers_WrongId() {
        // Create initial user with email
        Map<String, String> context = createUserAndReturnContext();
        // Extract user ID from the initial creation response and convert it to an integer
        int userId = 1;
        // Prepare update attributes
        Map<String, String> updateAttributes = Map.of();
        CreateUserModel updateUserModel = createUserModel(updateAttributes);

        //Update the user
        Response updateResponse = GoRestService.updateUser(userId, updateUserModel);

        assertThat(updateResponse.getStatusCode(), equalTo(SC_NOT_FOUND));
    }

    @Test
    public void Users_UpdateUsers_Unauthorized() {
        // Create initial user with email
        Map<String, String> context = createUserAndReturnContext();
        // Extract user ID from the initial creation response and convert it to an integer
        int userId = Integer.parseInt(context.get("id"));
        // Prepare update attributes
        Map<String, String> updateAttributes = Map.of(
                "name", "Unauthorized Update",
                "email", "unauthorized.update@example.com",
                "status", "active"
        );
        CreateUserModel updateUserModel = createUserModel(updateAttributes);

        // Update the user with an incorrect authorization token
        Response updateResponse = RestAssured.given()
                .baseUri("https://gorest.co.in/public/v1")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer INVALID_ACCESS_TOKEN")
                .body(updateUserModel)
                .when()
                .patch("/users/" + userId);

        // Assert that the status code is 401 Unauthorized
        assertThat(updateResponse.getStatusCode(), equalTo(SC_UNAUTHORIZED));
    }

    @Test
    public void Users_UpdateUsers_BadRequest() {
        // Create initial user with email
        Map<String, String> context = createUserAndReturnContext();
        // Extract user ID from the initial creation response and convert it to an integer
        int userId = Integer.parseInt(context.get("id"));

        // Send request with malformed JSON
        String malformedJson = "{ \"name\": \"Bad Request\", \"email\": \"bad.request@example.com\" "; // Missing closing bracket

        // Update the user with malformed JSON
        Response updateResponse = RestAssured.given()
                .baseUri("https://gorest.co.in/public/v1")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer fa80cece96297cd1ee8f66607d62a94723a4ddd79769451e5a6ef9efba66ca61")
                .body(malformedJson)
                .when()
                .patch("/users/" + userId)
                .then()
                .log().all()
                .extract().response();

        // Assert that the status code is 400 Bad Request
        assertThat(updateResponse.getStatusCode(), equalTo(SC_BAD_REQUEST));
    }

}
