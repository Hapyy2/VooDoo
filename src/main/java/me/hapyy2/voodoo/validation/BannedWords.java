package me.hapyy2.voodoo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BannedWordsValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BannedWords {
    String message() default "Contains banned words";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}