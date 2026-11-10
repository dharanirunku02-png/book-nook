package com.booknook.dto;

import com.booknook.entity.User;

/**
 * UserDTO — a lightweight representation of a user for API responses.
 */
public class UserDTO {

    private Long   id;
    private String name;

    public static UserDTO from(User user) {
        UserDTO dto = new UserDTO();
        dto.id   = user.getId();
        dto.name = user.getName();
        return dto;
    }

    public Long   getId()   { return id; }
    public String getName() { return name; }
}
