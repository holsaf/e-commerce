package com.ecommerce.backend.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

import static com.ecommerce.backend.utils.Constants.ROLE_CUSTOMER;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue(ROLE_CUSTOMER)
public class Customer extends User{

    private Integer discount = 0;
}
