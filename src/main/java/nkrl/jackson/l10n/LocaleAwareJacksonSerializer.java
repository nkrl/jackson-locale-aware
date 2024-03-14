package nkrl.jackson.l10n;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.*;

/**
 * A serializer appending all localized properties provided locale.
 *
 * TODO set this on class containing localizable props, set attribute in createContextual and get attribute in serialize
 * Use this to add extra attributes to entities if they have the annotation; add this serializer via BeanSerializerModifier
 */
public class LocaleAwareJacksonSerializer extends StdSerializer<Object> /*implements ContextualSerializer*/ {

    private BeanSerializerBuilder builder;

    private AnnotatedClass bean;

    private final List<BeanPropertyWriter> properties = new LinkedList<>();

    /*public LocaleAwareJacksonSerializer() {
        super();
    }*/

    public LocaleAwareJacksonSerializer(BeanSerializerBuilder builder) {
        super(builder.getBeanDescription().getType());
        this.builder = builder;
        this.bean = builder.getClassInfo();
    }

    /*public LocaleAwareJacksonSerializer(JavaType type, BeanSerializerBuilder builder, BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        super(type, builder, properties, filteredProperties);
    }*/

    /*protected LocaleAwareJacksonSerializer(Class<T> t) {
        super(t);
    }*/

    /*public LocaleAwareJacksonSerializer(Locale locale) {
        super(String.class);
        this.locale = locale;
    }*/

    // TODO custom serializer for object refs

    /*@Override
    public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property) throws JsonMappingException {
        property.getMember().getDeclaringClass().getDeclaredMethods();
        // add @JsonLocaleAware that extends Jackson annotations and calls method given in parameter via reflection
        return this; // todo how do we get locale?
    }*/
    /*
     TODO
     use serializer for class to which we want to add virtual properties
     these methods are identified by custom annotation that we access using reflection
     collect method return values in #createContextual
     write them out in serialize
     ALT:
     annotate class with @JsonAppend with @JsonAppend.Prop and custom VirtualBeanPropertyWriter implementation
     */


    @Override
    public void serialize(Object value, JsonGenerator json, SerializerProvider provider) throws IOException {
        // TODO alt default serialization: json.writeStartObject provider.defaultSerializer/findValueSerializer json.writeEndObject
        properties.addAll(builder.getProperties());
        //json.writeString(value);
        //provider.getAttribute("country");
        //super.serialize(value, json, provider);

        /*for (Iterator<PropertyWriter> it = this.properties(); it.hasNext(); ) {
            PropertyWriter writer = it.next();
            writer.

        }*/
        //AnnotatedClass annotatedClass = AnnotatedClassResolver.resolveWithoutSuperTypes(provider.getConfig(), value.getClass());
        AnnotatedClass annotatedClass = this.bean;
        for (AnnotatedMethod method : annotatedClass.memberMethods()) {
            PropertyName name = provider.getAnnotationIntrospector().findNameForSerialization(method);
            if (name == null)
                continue; // method is not annotated with @JsonGetter or @JsonProperty, continue with next candidate

            String key;
            if (name.equals(PropertyName.USE_DEFAULT)) {
                key = method.getName();
            } else {
                key = name.getSimpleName();
            }

            String property = provider.getConfig().getAccessorNaming().forPOJO(provider.getConfig(), annotatedClass).findNameForIsGetter(method, key);
            if (property == null) {
                property = provider.getConfig().getAccessorNaming().forPOJO(provider.getConfig(), annotatedClass).findNameForRegularGetter(method, key);
            }
            // e.g. camelCase, under_score, etc.
            provider.getConfig().getPropertyNamingStrategy().nameForGetterMethod(provider.getConfig(), method, key);

            properties.add(new LocaleAwareMethodPropertyWriter(method, provider.getConfig()));
        }
        //provider.getAnnotationIntrospector().findAndAddVirtualProperties(provider.getConfig(), annotatedClass, List.of()); this is @JsonAppend

        // TODO BeanDescription instead of BeanSerializerBuilder might be sufficient OR use provider.defaultSerializer
        BeanSerializer serializer = new BeanSerializer(builder.getBeanDescription().getType(), builder, properties.toArray(new BeanPropertyWriter[0]), builder.getFilteredProperties());
        serializer.serialize(value, json, provider);
    }
}
