package com.backend.escort.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrgDataInfo {
    long cancelled;
    long completed;
    long expired;
    long ongoing;

}
