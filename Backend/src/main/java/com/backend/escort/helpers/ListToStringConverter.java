package com.backend.escort.helpers;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Allow us to store comma separated list of pick up locations in a single column
public class ListToStringConverter implements AttributeConverter<List<String>, String> {
    @Override
    public String convertToDatabaseColumn(List<String> strings) {
        return strings == null ? null : String.join(",",strings);
    }

    @Override
    public List<String> convertToEntityAttribute(String s) {
        return s == null ? Collections.emptyList() : Arrays.asList(s.split(","));
    }
}
