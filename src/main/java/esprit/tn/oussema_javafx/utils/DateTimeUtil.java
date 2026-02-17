package esprit.tn.oussema_javafx.utils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateTimeUtil {

    public static Timestamp toTimestamp(LocalDateTime dt){
        if(dt == null) return null;
        return Timestamp.valueOf(dt);
    }

    public static LocalDateTime toLocalDateTime(Timestamp ts){
        if(ts == null) return null;
        return ts.toLocalDateTime();
    }

    public static java.sql.Date toSqlDate(LocalDate date){
        if(date == null) return null;
        return java.sql.Date.valueOf(date);
    }

    public static LocalDate toLocalDate(java.sql.Date date){
        if(date == null) return null;
        return date.toLocalDate();
    }
}
