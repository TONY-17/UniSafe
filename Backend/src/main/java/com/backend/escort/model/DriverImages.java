package com.backend.escort.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@ToString
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "driver_images")
public class DriverImages {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private Long driverId;
    String url;
    public DriverImages(Long driverId,String url) {
        this.driverId = driverId;
        this.url = url;
    }
}
