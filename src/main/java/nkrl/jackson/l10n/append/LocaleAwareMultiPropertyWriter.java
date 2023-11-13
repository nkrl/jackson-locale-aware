package nkrl.jackson.l10n.append;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import com.fasterxml.jackson.databind.util.Annotations;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * A hacky property writer that appends all localized properties of a bean marked with @JsonProperty or @JsonGetter.
 * <p>
 * Note that even though VirtualBeanPropertyWriters are intended for appending a single virtual attribute,
 * we are using it to append multiple attributes.
 */
public class LocaleAwareMultiPropertyWriter extends VirtualBeanPropertyWriter {

    private Locale locale = Locale.getDefault();

    /**
     * Jackson uses this default constructor to instantiate this property writer.
     */
    public LocaleAwareMultiPropertyWriter() {}

    protected LocaleAwareMultiPropertyWriter(Locale locale, BeanPropertyDefinition propDef, Annotations contextAnnotations, JavaType declaredType) {
        super(propDef, contextAnnotations, declaredType);
        this.locale = locale;
    }

    @Override
    protected Object value(Object bean, JsonGenerator json, SerializerProvider prov) throws Exception {
        /*
        === ALT ===
        */
        //AnnotatedClass annotatedClass = AnnotatedClassResolver.resolveWithoutSuperTypes(prov.getConfig(), bean.getClass());
        AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(prov.getConfig(), prov.constructType(bean.getClass()), prov.getConfig());
        for (AnnotatedMethod annotatedMethod : annotatedClass.memberMethods()) {
            if (annotatedMethod.getParameterCount() != 1 || !annotatedMethod.getParameterType(0).hasRawClass(Locale.class)) {
                // we do not need to check for parameters inheriting from Locale because the class is final
                continue;
            }

            /*if (annotatedMethod.hasAnnotation(JsonProperty.class)) {
                annotatedMethod.getName()
            }
            else if (annotatedMethod.hasAnnotation(JsonGetter.class)) {

            }*/

            // remove Java Bean accessor naming conventions if marked with @JsonGetter TODO unless renamed
            if (annotatedMethod.hasAnnotation(JsonGetter.class)) {
                String potentialKey = prov.getConfig().getAccessorNaming().forPOJO(prov.getConfig(), annotatedClass).findNameForIsGetter(annotatedMethod, key);
                if (potentialKey == null) {
                    potentialKey = prov.getConfig().getAccessorNaming().forPOJO(prov.getConfig(), annotatedClass).findNameForRegularGetter(annotatedMethod, key);
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
        === ORIG ===
         */

        // call next annotated methods with reflection and return its return value
        /*for (Method method : bean.getClass().getDeclaredMethods()) {
            String key;
            if (method.isAnnotationPresent(JsonProperty.class)) {
                key = method.getAnnotation(JsonProperty.class).value();
                if (key.equals(JsonProperty.USE_DEFAULT_NAME)) {
                    key = method.getName();
                }
            }
            else if (method.isAnnotationPresent(JsonGetter.class)) {
                key = method.getAnnotation(JsonGetter.class).value();
                if (key.isEmpty()) {
                    key = method.getName();
                }
                // TODO remove prefix from getSomething isSomething


                //prov.getConfig().getPropertyNamingStrategy().nameForGetterMethod(prov.getConfig(), getMember()., key);
            }
            else {
                continue;
            }

            if (method.getParameterCount() != 1 || !method.getParameterTypes()[0].equals(Locale.class)) {
                continue;
            }

            Object result = method.invoke(bean, prov.getLocale());
            json.writeFieldName(key);
            prov.findValueSerializer(result.getClass()).serialize(result, json, prov);
            //json.writeFieldName(key);
            //json.writeString(result.toString());
        }*/

        /*
         Usually we would have to return the attribute value to serialize here. Note that
         VirtualBeanPropertyWriters are only intended for appending a single attribute.
         */
        return null;
    }

    @Override
    public VirtualBeanPropertyWriter withConfig(MapperConfig<?> config, AnnotatedClass declaringClass, BeanPropertyDefinition propDef, JavaType type) {
        // declaring class is entity, propDef is nameless SimpleBeanPropertyDefinition with VirtualAnnotatedMember

        return new LocaleAwareMultiPropertyWriter(locale, propDef, declaringClass.getAnnotations(), type);
    }
}
