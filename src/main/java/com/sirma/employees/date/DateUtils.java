package com.sirma.employees.date;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class DateUtils {
    private static final String INVALID_DATE = "Invalid date";
    private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {
        {
            put("^\\d{8}$", "yyyyMMdd");
            put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
            put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
            put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
            put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
            put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
            put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
        }
    };

    private static String determineDateFormat(String dateString) {
        for (String regex : DATE_FORMAT_REGEXPS.keySet()) {
            if (dateString.toLowerCase()
                          .matches(regex)) {
                return DATE_FORMAT_REGEXPS.get(regex);
            }
        }
        return null; // Unknown format.
    }

    public static LocalDate parseDate(String stringifiedDate) {
        String format = determineDateFormat(stringifiedDate);
        if (format == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_DATE);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        return LocalDate.parse(stringifiedDate, formatter);
    }
}
