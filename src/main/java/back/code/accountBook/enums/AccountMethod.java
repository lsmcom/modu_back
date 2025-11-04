package back.code.accountBook.enums;

import lombok.Getter;

@Getter
public enum AccountMethod {
    BANK("은행"),
    CARD("카드"),
    CASH("현금"),
    ETC("기타");

    private final String label;

    AccountMethod(String label) {
        this.label = label;
    }

}
