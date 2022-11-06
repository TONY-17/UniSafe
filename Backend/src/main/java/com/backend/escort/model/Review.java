package com.backend.escort.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;


@Entity
@Table(
        name = "reviews"
)
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(name = "rating")
    private int rating;

    @Column(name = "comment")
    private String comment;

    // We need to know the student who wrote the review
    @Column(name = "studentId")
    private Long studentId;

    // We need to know the driver the student is reviewing
    @Column(name = "driverId")
    private Long driverId;

   @Column(name = "orgId")
    private Long orgId;

    public Review(int rating, String comment, Long studentId, Long driverId,Long orgId) {
        this.rating = rating;
        this.comment = comment;
        this.studentId = studentId;
        this.driverId = driverId;
        this.orgId = orgId;
    }
}
