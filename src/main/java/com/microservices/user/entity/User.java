package com.microservices.user.entity;

import com.microservices.user.constant.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isExpert = false;

    @Column(length = 50, nullable = false)
    private String firstName;

    @Column(length = 50, nullable = false)
    private String lastName;

    @Column(nullable = false)
    private Gender gender;

    private LocalDate birthDate;

    @Column(length = 150, nullable = false, unique = true)
    private String email;

    @ManyToMany
    @JoinTable(
            name = "user_domain",
            joinColumns = {
                    @JoinColumn(name = "user_id",
                            nullable = false,
                            referencedColumnName = "id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "domain_id",
                            nullable = false,
                            referencedColumnName = "id")
            }
    )
    private List<Domain> domains;

}
