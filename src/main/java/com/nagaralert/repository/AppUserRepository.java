package com.nagaralert.repository;

import com.nagaralert.model.AppUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends MongoRepository<AppUser, String> {
    Optional<AppUser> findByOauthIdAndOauthProvider(String oauthId, String oauthProvider);
}
