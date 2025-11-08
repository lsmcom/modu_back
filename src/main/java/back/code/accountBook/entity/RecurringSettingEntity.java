package back.code.accountBook.entity;

import java.time.LocalDate;

import back.code.accountBook.enums.AccountCycle;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "recurring_setting")
public class RecurringSettingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int recurringId;
    @Enumerated(EnumType.STRING)
    private AccountCycle cycle;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;
    private String daysOfWeek;
    private LocalDate nextDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private AccountBookEntity account;
}
