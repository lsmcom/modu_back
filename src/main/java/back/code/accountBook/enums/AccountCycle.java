package back.code.accountBook.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AccountCycle {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

    @JsonCreator
    public static AccountCycle from(String value) {
        if (value == null) return null;
        return AccountCycle.valueOf(value.toUpperCase());
    }
}
