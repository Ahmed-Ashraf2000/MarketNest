package com.marketnest.ecommerce.validation;

import com.marketnest.ecommerce.annotation.FieldsValueMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class FieldsValueMatchValidator implements ConstraintValidator<FieldsValueMatch, Object> {
    private String field;
    private String fieldToMatch;
    private String message;

    @Override
    public void initialize(FieldsValueMatch constraintAnnotation) {
        field = constraintAnnotation.field();
        fieldToMatch = constraintAnnotation.fieldToMatch();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(value);
        Object fieldValue = beanWrapper.getPropertyValue(field);
        Object fieldMatchValue = beanWrapper.getPropertyValue(fieldToMatch);

        boolean isValid = (fieldValue == null && fieldMatchValue == null) ||
                          (fieldValue != null && fieldValue.equals(fieldMatchValue));

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            message.isEmpty() ?
                                    field + " and " + fieldToMatch + " must match" : message)
                    .addPropertyNode(fieldToMatch)
                    .addConstraintViolation();
        }

        return isValid;
    }
}