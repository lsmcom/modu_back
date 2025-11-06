package back.code.accountBook.enums;

import lombok.Getter;

@Getter
public enum AccountMethod {
    BANK("BANK"),
    CARD("CARD"),
    CASH("CASH"),
    ETC("ETC");

    private final String label;

    AccountMethod(String label) {
        this.label = label;
    }
}
