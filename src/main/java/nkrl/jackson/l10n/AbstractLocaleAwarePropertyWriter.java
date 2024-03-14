package nkrl.jackson.l10n;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;

/**
 * Provides common implementations for all locale aware property writers, which differ only in scope and appending technique.
 */
public abstract class AbstractLocaleAwarePropertyWriter extends VirtualBeanPropertyWriter {

    private SerializerProvider provider;

    private AnnotatedMethod localeAwareMethod;

    /**
     * Determines the property name to use for serializing a locale aware method, respecting any name overrides
     * using {@link JsonGetter} or {@link JsonProperty}
     * and any property naming strategy set with {@link JsonNaming} or with
     * {@link ObjectMapper#setPropertyNamingStrategy(PropertyNamingStrategy)}.
     *
     * @param localeAwareMethod The method that generates the localized JSON property
     * @return The property name to use for serialization
     */
    protected String propertyName(AnnotatedMethod localeAwareMethod, SerializerProvider provider) {
        this.provider = provider;
        this.localeAwareMethod = localeAwareMethod;

        String propertyName;
        PropertyName renamed = provider.getAnnotationIntrospector().findNameForSerialization(localeAwareMethod);
        if (renamed.equals(PropertyName.USE_DEFAULT)) {
            propertyName = localeAwareMethod.getName(); // propertyNameFromMethod
        }
        else {
            // property was renamed in annotation
            propertyName = renamed.getSimpleName(); // propertyNameFromAnnotation
        }
        return propertyName; // remove bean getter naming convention
    }

    private String propertyNameFromMethod() {
        return localeAwareMethod.getName(); // TODO remove getter naming convention if annotated with @JsonGetter
    }

    private String propertyNameFromAnnotation() {
        PropertyName renamed = provider.getAnnotationIntrospector().findNameForSerialization(localeAwareMethod);
        if (renamed.equals(PropertyName.USE_DEFAULT)) {
            return null; // or use an Optional
        }
        else {
            return renamed.getSimpleName();
        }
    }

    private String propertyNameInNamingConvention(String propertyName) {
        if (provider.getConfig().getPropertyNamingStrategy() != null) { // by default, property naming strategy is not set
            return provider.getConfig().getPropertyNamingStrategy().nameForGetterMethod(provider.getConfig(), this.localeAwareMethod, propertyName);
        }
        return propertyName;
    }
}
