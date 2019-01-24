package com.podcasses.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by aleksandar.kovachev.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    private String username;

    private String password;

    private String keycloakId;

    private String email;

    private String firstName;

    private String lastName;

    private int emailVerified;

}
