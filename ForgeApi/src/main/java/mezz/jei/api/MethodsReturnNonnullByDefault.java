package mezz.jei.api;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @deprecated use {@link net.minecraft.MethodsReturnNonnullByDefault} instead.
 */
@Nonnull
@TypeQualifierDefault({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Deprecated(forRemoval = true, since = "9.5.3")
public @interface MethodsReturnNonnullByDefault {
}
