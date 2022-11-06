package com.backend.escort.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentsOnSystemInfo {
    HashMap<String, Integer> orgStudentCount;
    int totalNumberOfStudentsAllOrganisations;
}
