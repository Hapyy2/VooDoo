package me.hapyy2.voodoo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class BannedWordsValidator implements ConstraintValidator<BannedWords, String> {

    private static final List<String> BANNED = Arrays.asList("admin", "root", "null", "undefined");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        String lower = value.toLowerCase();
        for (String word : BANNED) {
            if (lower.contains(word)) {
                return false;
            }
        }
        return true;
    }
}