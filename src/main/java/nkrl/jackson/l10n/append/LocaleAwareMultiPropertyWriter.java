package nkrl.jackson.l10n.append;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import com.fasterxml.jackson.databind.util.Annotations;

import java.util.Locale;

/**
 * A hacky property writer that appends all localized properties of a bean marked with @JsonProperty or @JsonGetter.
 * <p>
 * Note that even though VirtualBeanPropertyWriters are intended for appending a single virtual attribute,
 * we are using it to append multiple attributes.
 */
public class LocaleAwareMultiPropertyWriter extends VirtualBeanPropertyWriter {
    /**
     * The bean that is marked with @JsonLocaleAware.
     */
    private AnnotatedClass localeAwareBean;

    /**
     * Jackson uses this default constructor to instantiate this property writer.
     */
    public LocaleAwareMultiPropertyWriter() {}

    protected LocaleAwareMultiPropertyWriter(AnnotatedClass localeAwareBean, BeanPropertyDefinition propDef, Annotations contextAnnotations, JavaType declaredType) {
        super(propDef, contextAnnotations, declaredType);
        this.localeAwareBean = localeAwareBean;
    }

    @Override
    protected Object value(Object bean, JsonGenerator json, SerializerProvider prov) throws Exception {
        for (AnnotatedMethod annotatedMethod : localeAwareBean.memberMethods()) {
            if (annotatedMethod.getParameterCount() != 1 || !annotatedMethod.getParameterType(0).hasRawClass(Locale.class)) {
                // we do not need to check for parameters inheriting from Locale because the class is final
                continue;
            }

            String key;
            PropertyName renamed = prov.getAnnotationIntrospector().findNameForSerialization(annotatedMethod);
            if (renamed.equals(PropertyName.USE_DEFAULT)) {
                key = annotatedMethod.getName();
            }
            else {
                // property was renamed in annotation
                key = renamed.getSimpleName();
            }

            // remove Java Bean accessor naming conventions if marked with @JsonGetter TODO unless renamed
            if (annotatedMethod.hasAnnotation(JsonGetter.class)) {
                String potentialKey = prov.getConfig().getAccessorNaming().forPOJO(prov.getConfig(), localeAwareBean).findNameForIsGetter(annotatedMethod, key);
                if (potentialKey == null) {
                    potentialKey = prov.getConfig().getAccessorNaming().forPOJO(prov.getConfig(), localeAwareBean).findNameForRegularGetter(annotatedMethod, key);
                }
                if (potentialKey != null) {
                    key = potentialKey;
                }
            }

            // adapt to configured property naming strategy
            if (prov.getConfig().getPropertyNamingStrategy() != null) { // by default, property naming strategy is not set
                key = prov.getConfig().getPropertyNamingStrategy().nameForGetterMethod(prov.getConfig(), annotatedMethod, key);
            }

            Object result = annotatedMethod.getAnnotated().invoke(bean, prov.getLocale());

            json.writeFieldName(key);
            prov.findValueSerializer(result.getClass()).serialize(result, json, prov);
        }

        /*
         Usually we would have to return the attribute value to serialize here. Note that
         VirtualBeanPropertyWriters are only intended for appending a single attribute.
         */
        return null;
    }

    @Override
    public VirtualBeanPropertyWriter withConfig(MapperConfig<?> config, AnnotatedClass declaringClass, BeanPropertyDefinition propDef, JavaType type) {
        // declaring class is locale-aware bean, propDef is nameless SimpleBeanPropertyDefinition with VirtualAnnotatedMember
        return new LocaleAwareMultiPropertyWriter(declaringClass, propDef, declaringClass.getAnnotations(), type);
    }
}
