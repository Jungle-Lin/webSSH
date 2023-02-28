package com.jy.webssh.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author JungleLin
 * @date 2023/2/261:53
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)//生命周期
@Constraint(validatedBy = IpPattenValidator.class)
public @interface IpPatten {
    String message() default "Incorrect ip format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
