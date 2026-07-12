package com.nagaralert.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "app_users")
@Data
@NoArgsConstructor
public class AppUser {

    @Id
    private String id;

    @Field("oauth_provider")
    private String oauthProvider; // e.g., GOOGLE, FACEBOOK, LINKEDIN, MICROSOFT

    @Field("oauth_id")
    private String oauthId;

    private String name;

    private String email;

    @Field("mobile_number")
    private String mobileNumber;

    @Field("profile_image")
    private String profileImage;

    private String dob;
}
