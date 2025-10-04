package com.ecommerce.backend.entity;

import com.ecommerce.backend.model.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("EMPLOYEE")
public class Employee extends User{

    @Column(nullable = false)
    private Integer commissionRate;

    private Role role;


}
