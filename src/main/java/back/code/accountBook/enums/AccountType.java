package back.code.accountBook.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AccountType {
    INCOME,
    EXPENSE;

    @JsonCreator
    public static AccountType from(String value) {
        if (value == null) return null;
        return AccountType.valueOf(value.toUpperCase());
    }

}