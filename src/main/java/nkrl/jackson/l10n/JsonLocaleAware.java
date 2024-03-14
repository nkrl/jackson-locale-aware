package nkrl.jackson.l10n;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks classes that add computed, locale-dependent attributes to their JSON
 * serialization using {@link LocaleAwareMethodPropertyWriter}.
 */
@JacksonAnnotationsInside
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@JsonSerialize(using = LocaleAwareJacksonSerializer.class)
public @interface JsonLocaleAware {
}
