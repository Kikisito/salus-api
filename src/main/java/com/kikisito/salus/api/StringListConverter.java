package com.kikisito.salus.api;

import com.kikisito.salus.api.type.RoleType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Converter
public class StringListConverter implements AttributeConverter<List<RoleType>, String> {

    private static final String SPLIT_CHAR = ";";

    @Override
    public String convertToDatabaseColumn(List<RoleType> roleTypeList) {
        return roleTypeList != null ? roleTypeList.stream()
                .map(RoleType::name)
                .collect(Collectors.joining(SPLIT_CHAR))
                : "";
    }

    @Override
    public List<RoleType> convertToEntityAttribute(String string) {
        return string != null ? Arrays.stream(string.split(SPLIT_CHAR))
                .map(RoleType::valueOf)
                .collect(Collectors.toList())
                : emptyList();
    }
}
