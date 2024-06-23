package services;

import io.restassured.response.Response;
import models.CreateUserModel;


public class GoRestService extends BaseService {

    public static Response createUser(final CreateUserModel createUserModel){
        return defaultRequestSpecification()
                .body(createUserModel)
                .log().all() // Log the request
                .when()
                .post("/public/v1/users")
                .then()
                .log().all() // Log the response
                .extract().response();

    }

    public static Response updateUser(final int userId,final CreateUserModel updateUserModel) {
        return defaultRequestSpecification()
                .body(updateUserModel)
                .log().all() // Log the request
                .when()
                .patch("/public/v1/users/" + userId)
                .then()
                .log().all() // Log the response
                .extract().response();
    }

    public static Response createUserV2(final CreateUserModel createUserModel){
        return defaultRequestSpecification()
                .body(createUserModel)
                .log().all() // Log the request
                .when()
                .post("/public/v2/users")
                .then()
                .log().all() // Log the response
                .extract().response();

    }
}
