package com.backend.escort.model;

import com.backend.escort.payload.response.ImageInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Table(
        name = "alerts"
)
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Alert {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(name = "tag")
    private String tag;


    @Column(name = "body")
    private String body;


    @Column(name = "time")
    private String timePosted;
    // Add the ability to attach a video/picture



    /*
    Location -> where the incident occurred
    User
    Comments
     */
    // The driver can reply to the person who commented a post
    @Column(name = "reported")
    private boolean reported;

    // Users can created multiple alerts
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    // @JsonIgnore
    private Student student;


    @Column(name = "orgId")
    private Long orgId;

    @Column(name = "uniqueAlertId")
    private String uniqueAlertId;


    public Alert( @NotBlank String tag, @NotBlank String body, @NotBlank String timePosted, boolean reported,
                  Long orgId,
                  String uniqueAlertId) {
        this.tag = tag;
        this.body = body;
        this.timePosted = timePosted;
        this.reported = reported;
        this.orgId = orgId;
        this.uniqueAlertId = uniqueAlertId;

    }


}
