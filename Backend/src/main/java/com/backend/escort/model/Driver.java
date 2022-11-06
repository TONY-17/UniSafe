package com.backend.escort.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(
        name = "drivers"
)
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Driver {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @NotBlank
    @Column(name = "title")
    private String title;

    @NotBlank
    @Column(name = "name")
    private String firstName;

    @NotBlank
    @Column(name = "surname")
    private String lastName;

    @NotBlank
    @Column(name = "gender")
    private String gender;

    @NotBlank
    @Column(name = "date_created")
    private String dateCreated;


    @Column(name = "verified")
    private boolean verified;

    @Column(name = "suspended")
    private boolean suspended;

    // The driver belongs to a certain organisation
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Organisation organisation;

    // Was Lazy before
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    // The driver is going to a user
    private User user;

    @Column(name = "admin_id")
    // This will hold the id of the admin who added the driver
    private Long adminId;

    public Driver(@NotBlank String title,
                  @NotBlank String firstName,
                  @NotBlank String lastName,
                  @NotBlank String gender,
                  @NotBlank String dateCreated,
                  Organisation organisation,
                  User user,
                  Long adminId) {
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.organisation = organisation;
        this.user = user;
        this.dateCreated = dateCreated;
        this.adminId = adminId;
    }
}
