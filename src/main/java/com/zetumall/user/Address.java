package com.zetumall.user;

import com.zetumall.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"Address\"", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "street_address", nullable = false)
    private String streetAddress;

    @Column
    private String city;

    @Column
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    @Column
    private String country = "KEN";

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type")
    private AddressType addressType = AddressType.SHIPPING;

    public enum AddressType {
        SHIPPING, BILLING, BOTH
    }
}
