import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.restassured.response.Response;
import models.CreateUserModel;
import models.ErrorData;
import models.SuccessData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.GoRestService;
import utils.Attributes;
import utils.TestUtils;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;

public class CreateUserV2Tests {

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

    private <T> T parseResponse(Response response, TypeReference<T> typeReference) {
        T result = null;
        try {
            result = objectMapper.readValue(response.getBody().asString(), typeReference);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void assertUnprocessableEntityResponse(Response response, String expectedField, String expectedMessage) {
        assertThat(response.getStatusCode(), equalTo(SC_UNPROCESSABLE_ENTITY));

        // Parse the response body
        List<ErrorData> errors = parseResponse(response, new TypeReference<List<ErrorData>>() {});
        ErrorData errorData = errors.get(0);
        assertThat(errorData.getField(), equalTo(expectedField));
        assertThat(errorData.getMessage(), equalTo(expectedMessage));
    }

    private void assertSuccessfulResponse(Response response, Map<String, String> attributes) {
        assertThat(response.getStatusCode(), equalTo(SC_CREATED));

        // Parse the response body
        SuccessData successData = parseResponse(response, new TypeReference<SuccessData>() {});
        assertThat(successData.getId(), notNullValue());
        // Convert both expected and actual values to lowercase for case-insensitive comparison
        assertThat(successData.getName().toLowerCase(), equalTo(attributes.get("name").toLowerCase()));
        assertThat(successData.getEmail().toLowerCase(), equalTo(attributes.get("email").toLowerCase()));
        assertThat(successData.getGender().toLowerCase(), equalTo(attributes.get("gender").toLowerCase()));
        assertThat(successData.getStatus().toLowerCase(), equalTo(attributes.get("status").toLowerCase()));
    }

    //@Test
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
        Response response = GoRestService.createUserV2(createUserModel);
        response.then().log().all();

        // Assert success response
        assertSuccessfulResponse(response, attributes);
    }
}
