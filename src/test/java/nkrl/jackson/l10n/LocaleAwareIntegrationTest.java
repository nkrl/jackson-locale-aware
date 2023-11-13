package nkrl.jackson.l10n;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nkrl.jackson.l10n.entity.LocaleAwarePost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

public class LocaleAwareIntegrationTest {

    private final Map<Locale, String> json = new HashMap<>();

    @BeforeEach // TODO before all
    public void setUp() throws JsonProcessingException {
        LocaleAwarePost post = new LocaleAwarePost();

        post.setAuthor("John Doe");

        post.setContent(Locale.US, "Hello World!");
        post.setContent(Locale.GERMANY, "Hallo Welt!");

        post.setFeatured(Locale.US, true);
        post.setFeatured(Locale.GERMANY, false);

        post.setReaders(Locale.US, 60000);
        post.setReaders(Locale.GERMANY, 8000);

        post.setLink(Locale.US, "/en-US/hello");
        post.setLink(Locale.GERMANY, "/de-DE/hallo");

        ObjectMapper mapper = new ObjectMapper();
        mapper.setLocale(Locale.US);
        json.put(Locale.US, mapper.writeValueAsString(post));

        mapper.setLocale(Locale.GERMANY);
        json.put(Locale.GERMANY, mapper.writeValueAsString(post));
    }

    @Test
    @DisplayName("serialization should leave unlocalized attributes intact")
    public void unlocalizedAttributesShouldRemainIntact() {
        assertThatJson(json.get(Locale.US)).node("author").isString().isEqualTo("John Doe");
        assertThatJson(json.get(Locale.GERMANY)).node("author").isString().isEqualTo("John Doe");
    }

    @Test
    @DisplayName("serialization should include localized attributes marked with @JsonGetter")
    public void serializationShouldIncludeLocalizedAttributesMarkedAsGetter() {
        assertThatJson(json.get(Locale.US)).and(
                root -> root.node("content").isString().isEqualTo("Hello World!"),
                root -> root.node("featured").isBoolean().isTrue()
        );
        assertThatJson(json.get(Locale.GERMANY)).and(
                root -> root.node("content").isString().isEqualTo("Hallo Welt!"),
                root -> root.node("featured").isBoolean().isFalse()
        );
    }

    @Test
    @DisplayName("serialization should include localized attributes marked with @JsonProperty")
    public void serializationShouldIncludeLocalizedAttributesMarkedAsProperty() {
        assertThatJson(json.get(Locale.US)).node("score").isIntegralNumber();
        assertThatJson(json.get(Locale.GERMANY)).node("score").isIntegralNumber();
    }

    @Test
    @DisplayName("serialization should leave name of localized Java Bean accessor methods marked with @JsonProperty intact")
    public void beanAttributeNamesMarkedAsPropertyShouldRemainIntact() {
        assertThatJson(json.get(Locale.US)).node("isNew").isPresent();
        assertThatJson(json.get(Locale.GERMANY)).node("isNew").isPresent();
    }

    @Test
    @DisplayName("serialization should rename attributes if value of @JsonProperty or @JsonGetter is set")
    public void serializationShouldRenameAttributes() {
        assertThatJson(json.get(Locale.US)).and(
                root -> root.node("audience").isIntegralNumber().isEqualTo(60000),
                root -> root.node("url").isString().isEqualTo("/en-US/hello")
        );
        assertThatJson(json.get(Locale.GERMANY)).and(
                root -> root.node("audience").isIntegralNumber().isEqualTo(8000),
                root -> root.node("url").isString().isEqualTo("/de-DE/hallo")
        );
    }

    @Test
    @DisplayName("serialization should exclude methods with parameters other than Locale")
    public void serializationShouldExcludeMethodsWithErroneousParameters() {
        assertThatJson(json.get(Locale.US)).and(
                root -> root.node("erroneous").isAbsent(),
                root -> root.node("alsoErroneous").isAbsent()
        );
        assertThatJson(json.get(Locale.GERMANY)).and(
                root -> root.node("erroneous").isAbsent(),
                root -> root.node("alsoErroneous").isAbsent()
        );
    }

    // * does contain author (leaves unlocalized attributes intact)
    // * does contain content via @JsonGetter
    // * does contain featured (bool) via @JsonGetter with is prefix
    // * does contain audience (renamed from readers) (int) via @JsonProperty
    // * does contain url (renamed from link) via @JsonGetter
    // * does contain score (int) included via @JsonProperty
    // * does not contain erroneous
    // * does not contain alsoErroneous
}
