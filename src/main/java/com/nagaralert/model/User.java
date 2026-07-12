package com.nagaralert.model;

/**
 * Abstract class representing a user in the Nagar Alert Hub system.
 */
public abstract class User {
    private String name;
    private String userId;

    /**
     * Default constructor.
     */
    public User() {
    }

    /**
     * Parameterized constructor.
     *
     * @param name   The user's name
     * @param userId The user's unique ID
     */
    public User(String name, String userId) {
        this.name = name;
        this.userId = userId;
    }

    /**
     * Gets the user's name.
     *
     * @return the user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user's ID.
     *
     * @return the user's ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user's ID.
     *
     * @param userId the user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
