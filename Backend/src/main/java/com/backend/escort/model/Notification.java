package com.backend.escort.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Table(
        name = "notifications"
)
@Getter
@Setter
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;
    @Column(name = "date_created")
    private String dateCreated;
    @Column(name = "content")
    private String content;

    /*
    * These notifications will be long to a certain org
    * An org can contain multiple notifications hence a one to many relationship
     */

    private Long orgId;

    public Notification(String dateCreated, String content, Long orgId) {
        this.dateCreated = dateCreated;
        this.content = content;
        this.orgId = orgId;
    }
}
