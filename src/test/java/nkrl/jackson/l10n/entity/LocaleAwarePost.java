package nkrl.jackson.l10n.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import nkrl.jackson.l10n.append.JsonLocaleAware;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@JsonLocaleAware
public class LocaleAwarePost {
    /**
     * An attribute that is not localized.
     */
    private String author;

    /**
     * A string attribute that is localized.
     */
    private final Map<Locale, String> contents = new HashMap<>();

    /**
     * An integer attribute that is localized.
     */
    @JsonIgnore
    private final Map<Locale, Integer> readers = new HashMap<>();

    /**
     * A boolean attribute that is localized.
     */
    @JsonIgnore
    private final Map<Locale, Boolean> features = new HashMap<>();

    @JsonIgnore
    private final Map<Locale, String> links = new HashMap<>();

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @JsonGetter
    public String getContent(Locale locale) {
        return contents.get(locale);
    }

    public void setContent(Locale locale, String content) {
        this.contents.put(locale, content);
    }

    @JsonGetter
    public Boolean isFeatured(Locale locale) {
        return features.get(locale);
    }

    public void setFeatured(Locale locale, Boolean featured) {
        this.features.put(locale, featured);
    }

    public void setReaders(Locale locale, Integer readers) {
        this.readers.put(locale, readers);
    }

    /**
     * A renamed attribute
     * @param locale
     * @return
     */
    @JsonProperty("audience")
    public Integer readers(Locale locale) {
        return this.readers.get(locale);
    }

    @JsonGetter("url")
    public String getLink(Locale locale) {
        return this.links.get(locale);
    }

    public void setLink(Locale locale, String link) {
        this.links.put(locale, link);
    }

    /**
     * Conforms to Bean accessor convention, but name should be left as is because marked with @JsonProperty
     * @param locale
     * @return
     */
    @JsonProperty
    public boolean isNew(Locale locale) {
        return true;
    }

    @JsonProperty
    public int score(Locale locale) {
        int multiple = 1;
        if (features.get(locale)) {
            multiple = 20;
        }
        return readers.get(locale) * multiple;
    }

    /**
     * An attribute that is marked for inclusion into serialization, but has an incompatible parameter list (param is not locale).
     * @param error
     * @return
     */
    @JsonProperty
    public boolean erroneous(boolean error) {
        return error;
    }

    /**
     * An attribute that is marked for inclusion into serialization, but has an incompatible parameter list (unknown param).
     * @param locale
     * @param error
     * @return
     */
    @JsonProperty
    public boolean alsoErroneous(Locale locale, boolean error) {
        return error;
    }
}
