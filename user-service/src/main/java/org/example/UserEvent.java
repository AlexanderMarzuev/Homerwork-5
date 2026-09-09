package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent implements Serializable {
    private String email;
    private UserOperation operation;

    public enum UserOperation {
        CREATED, DELETED
    }
}
