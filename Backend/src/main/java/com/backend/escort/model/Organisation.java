package com.backend.escort.model;

import com.backend.escort.helpers.ListToStringConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(
        name = "organisations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "name"),
                @UniqueConstraint(columnNames = "domain")
        }
)
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Organisation {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @NotBlank
    @Column(name = "name")
    private String name;

    @NotBlank
    @Column(name = "domain")
    private String domain;

    @Column(name = "active")
    private boolean active;

    // An admin can manage/create multiple organisations
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private User user;

    private double score;

    private String rating;
    @Column(name = "address")
    private String address;


    @Column(name = "coordinates")
    private double[] coordinates;


    @Column(name = "pickUpLocations")
    @Convert(converter = ListToStringConverter.class)
    private List<String> pickUpLocation;

    public Organisation(@NotBlank String name,
                        @NotBlank String domain,
                        @NotBlank boolean active,
                        double score,
                        String rating,
                        String address,
                        double[] coordinates,
                        List<String> pickUpLocation) {
        this.name = name;
        this.domain = domain;
        this.active = active;
        this.score = score;
        this.rating = rating;
        this.address = address;
        this.coordinates = coordinates;
        this.pickUpLocation = pickUpLocation;
    }
}
