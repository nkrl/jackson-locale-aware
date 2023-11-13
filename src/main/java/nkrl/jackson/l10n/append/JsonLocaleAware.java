package nkrl.jackson.l10n.append;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonAppend;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks classes that add computed, locale-dependent attributes to their JSON
 * serialization using {@link LocaleAwareMultiPropertyWriter}.
 */
@JacksonAnnotationsInside
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@JsonAppend(props = @JsonAppend.Prop(value = LocaleAwareMultiPropertyWriter.class))
//@JsonSerialize()
public @interface JsonLocaleAware {
}