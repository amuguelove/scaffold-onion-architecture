package top.flyeric.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class YamlDateParser {

    private static final String DATE_PREFIX = "now";
    private static final String EMPTY_STRING = "";
    private static final ImmutableList<String> DATE_TIME_DURATION_UNITS =
            ImmutableList.of("y", "M", "d", "H", "m");

    private YamlDateParser() {

    }

    static String parseYamlDate(String yamlStr) {
        if (StringUtils.isEmpty(yamlStr)) {
            return yamlStr;
        }
        if (isDate().test(yamlStr)) {
            return parseToDateStr(yamlStr);
        } else {
            return yamlStr;
        }
    }

    private static Predicate<String> isDate() {
        return str -> str.contains(DATE_PREFIX);
    }

    private static String parseToDateStr(String dateStr) {
        String offsetStr = remove(dateStr, DATE_PREFIX);
        Optional<String> optDateStr =
                DATE_TIME_DURATION_UNITS.stream().filter(offsetStr::contains).map(durationUnit -> {
                    int offsetDuration = Integer.parseInt(remove(offsetStr, durationUnit));
                    Map<String, LocalDateTime> dateUnitMap =
                            getDurationUnitDateTimeMap(offsetDuration);
                    return Timestamp.valueOf(dateUnitMap.get(durationUnit)).toString();
                }).findFirst();
        return optDateStr.orElse(Timestamp.valueOf(LocalDate.now().atStartOfDay()).toString());
    }

    private static Map<String, LocalDateTime> getDurationUnitDateTimeMap(int offsetDuration) {
        return ImmutableMap.of("y", LocalDate.now().atStartOfDay().plusYears(offsetDuration), "M",
                LocalDate.now().atStartOfDay().plusMonths(offsetDuration), "d",
                LocalDate.now().atStartOfDay().plusDays(offsetDuration), "H",
                LocalDate.now().atStartOfDay().plusHours(offsetDuration), "m",
                LocalDate.now().atStartOfDay().plusMinutes(offsetDuration));
    }

    private static String remove(String str, String strToRemove) {
        if (StringUtils.isEmpty(str)) {
            return EMPTY_STRING;
        }
        return str.replace(strToRemove, EMPTY_STRING);
    }
}
