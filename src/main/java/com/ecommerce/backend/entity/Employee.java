package com.ecommerce.backend.entity;

import com.ecommerce.backend.model.enums.Role;
import jakarta.persistence.Column;
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
public class Employee extends User{

    private Integer commissionRate = 0;

}
