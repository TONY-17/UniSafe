package com.backend.escort.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Table(
        name = "comments"
)
@Getter
@Setter
// This class contains the attributes of a comment that will be mapped to a single alert
public class Comment {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(name = "content")
    private String content;

    @Column(name = "dateCreated")
    private String dateCreated;

    @Column(name = "studentId")
    private Long studentId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "alert_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Alert alert;





}
