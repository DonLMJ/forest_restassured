package models;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateUserModel {

    private String name;
    private String gender;
    private String email;
    private String status;

    private CreateUserModel(Builder builder) {
        this.name = builder.name;
        this.gender = builder.gender;
        this.email = builder.email;
        this.status = builder.status;
    }

    public static class Builder {
        private String name;
        private String gender;
        private String email;
        private String status;

        public Builder() {}

        public Builder withAttributes(Map<String, String> attributes) {
            this.name = attributes.get("name");
            this.gender = attributes.get("gender");
            this.email =  attributes.get("email"); // or handle differently as needed
            this.status = attributes.get("status");
            return this;
        }

        public CreateUserModel build() {
            return new CreateUserModel(this);
        }
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
