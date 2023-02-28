package com.jy.webssh.validator;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotBlank;

/**
 * @author JungleLin
 * @date 2023/2/261:55
 */
public class IpPattenValidator implements ConstraintValidator<IpPatten, String> {

    private static final String ipPatten = "^((25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))$";

    @Override
    public boolean isValid(@NotBlank(message = "ip cannot be null") String s, ConstraintValidatorContext constraintValidatorContext) {
        return s.matches(ipPatten);
    }
}
