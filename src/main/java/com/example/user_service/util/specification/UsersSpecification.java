package com.example.user_service.util.specification;

import com.example.user_service.entity.Users;
import org.springframework.data.jpa.domain.Specification;

public class UsersSpecification {

    public static Specification<Users> firstNameContains(String name) {
        return (root, query, cb) ->
            name == null ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Users> surnameContains(String surname) {
        return (root, query, cb) ->
            surname == null ? null : cb.like(cb.lower(root.get("surname")), "%" + surname.toLowerCase() + "%");
    }

}
