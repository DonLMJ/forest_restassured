package utils;

import java.util.HashMap;
import java.util.Map;
import com.github.javafaker.Faker;
import io.restassured.response.Response;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import models.ApiResponse;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;
import models.SuccessData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import static org.hamcrest.MatcherAssert.assertThat;
import java.util.List;
import models.*;



public class TestUtils {

    private static Faker faker = new Faker();
    private static ObjectMapper objectMapper = new ObjectMapper(); // Declare objectMapper



    public static Map<String, String> generateAttributes(Attributes... attributesToGenerate) {
        Map<String, String> attributes = new HashMap<>();

        for (Attributes attribute : attributesToGenerate) {
            switch (attribute) {
                case NAME:
                    attributes.put("name", faker.name().fullName());
                    break;
                case EMAIL:
                    attributes.put("email", "fake_" + faker.internet().emailAddress());
                    break;
                case GENDER:
                    attributes.put("gender", faker.options().option("male", "female"));
                    break;
                case STATUS:
                    attributes.put("status", faker.options().option("active", "inactive"));
                    break;
                default:
                    // Handle unknown attributes or ignore
                    break;
            }
        }

        return attributes;
    }

    public static <T> ApiResponse<T> parseResponse(Response response, TypeReference<ApiResponse<T>> typeReference) {
        ApiResponse<T> apiResponse = null;
        try {
            apiResponse = objectMapper.readValue(response.getBody().asString(), typeReference);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public static void assertUnprocessableEntityResponse(Response response, String expectedField, String expectedMessage) {
        assertThat(response.getStatusCode(), equalTo(SC_UNPROCESSABLE_ENTITY));

        // Parse the response body
        ApiResponse<List<ErrorData>> apiResponse = parseResponse(response, new TypeReference<ApiResponse<List<ErrorData>>>() {});
        assertThat(apiResponse.getMeta(), equalTo(null));
        ErrorData errorData = apiResponse.getData().get(0);
        assertThat(errorData.getField(), equalTo(expectedField));
        assertThat(errorData.getMessage(), equalTo(expectedMessage));
    }

    public static void assertSuccessfulResponse(Response response, int expectedStatusCode, Map<String, String> attributes) {
        assertThat(response.getStatusCode(), equalTo(expectedStatusCode));

        if (expectedStatusCode == SC_CREATED) {
            // Parse the response body
            ApiResponse<SuccessData> apiResponse = parseResponse(response, new TypeReference<ApiResponse<SuccessData>>() {});
            SuccessData successData = apiResponse.getData();
            assertThat(successData.getId(), notNullValue());

            // Iterate through attributes and assert each one dynamically
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                String attribute = entry.getKey();
                String expectedValue = entry.getValue();

                switch (attribute) {
                    case "name":
                        assertThat(successData.getName(), equalTo(expectedValue));
                        break;
                    case "email":
                        assertThat(successData.getEmail(), equalTo(expectedValue));
                        break;
                    case "status":
                        assertThat(successData.getStatus(), equalTo(expectedValue));
                        break;
                    case "gender":
                        assertThat(successData.getGender(), equalTo(expectedValue));
                        break;
                    default:
                        // Handle unknown attributes if necessary
                        break;
                }
            }
        } else if (expectedStatusCode == SC_OK) {
            // Parse the response body for SC_OK response
            ApiResponse<SuccessData> apiResponse = parseResponse(response, new TypeReference<ApiResponse<SuccessData>>() {});
            SuccessData successData = apiResponse.getData();

            // Assert the response data matches the updated attributes
            assertThat(successData.getName(), equalTo(attributes.getOrDefault("name", successData.getName())));
            assertThat(successData.getEmail(), equalTo(attributes.getOrDefault("email", successData.getEmail())));
            assertThat(successData.getStatus(), equalTo(attributes.getOrDefault("status", successData.getStatus())));
            assertThat(successData.getGender(), equalTo(attributes.getOrDefault("gender", successData.getGender())));
        }
    }
}
