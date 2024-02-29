package hello.typeconverter.fomatter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.Formatter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

@Slf4j
public class MyNumberFormatter implements Formatter<Number> {

    /**
     * 문자를 객체로 변경
     */
    @Override
    public Number parse(String text, Locale locale) throws ParseException {
        log.info("text = {}, locale = {}", text, locale);
        NumberFormat format = NumberFormat.getInstance(locale);
        return format.parse(text);
    }


    /**
     * 객체를 문자로 변경
     */
    @Override
    public String print(Number object, Locale locale) {
        log.info("objcet = {}, locale = {}", object, locale);
        return NumberFormat.getInstance(locale).format(object);
    }
}