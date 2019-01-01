package mezz.jei.util;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Nonnull
@TypeQualifierDefault({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldsAndMethodsAreNonnullByDefault {
}
