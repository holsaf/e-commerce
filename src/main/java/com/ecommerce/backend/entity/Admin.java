package com.ecommerce.backend.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

import static com.ecommerce.backend.utils.Constants.ROLE_ADMIN;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue(ROLE_ADMIN)
public class Admin extends User{

    private String employeeId;

}
