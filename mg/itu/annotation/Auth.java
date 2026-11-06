package mg.itu.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Auth {
    String[] roles() default {}; // rehefa miampy role le izy
}