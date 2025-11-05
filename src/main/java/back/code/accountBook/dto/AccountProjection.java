package back.code.accountBook.dto;

public interface AccountProjection {
    String getDate();
    String getWeekStart();
    String getWeekEnd();
    String getYearMonth();
    Integer getIncome();
    Integer getExpense();
}
