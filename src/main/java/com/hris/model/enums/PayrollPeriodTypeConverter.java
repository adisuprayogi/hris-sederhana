package com.hris.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Attribute Converter for PayrollPeriodType
 * Converts enum to VARCHAR for database storage (not MySQL native ENUM)
 * This avoids schema validation issues with Hibernate
 */
@Converter(autoApply = true)
public class PayrollPeriodTypeConverter implements AttributeConverter<PayrollPeriodType, String> {

    @Override
    public String convertToDatabaseColumn(PayrollPeriodType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public PayrollPeriodType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            return PayrollPeriodType.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
