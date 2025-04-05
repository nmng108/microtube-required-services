package nmng108.microtube.processor.util;

import org.springframework.context.i18n.LocaleContextHolder;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public final class SimpleMessageInterpolator {
    public static String getMessage(String baseName, String key, Object... args) {
        return getMessage(baseName, LocaleContextHolder.getLocale(), key, args);
    }

    public static String getMessage(String baseName, Locale locale, String key, Object... args) {
        return MessageFormat.format(ResourceBundle.getBundle(baseName, locale).getString(key), args);
    }
}
