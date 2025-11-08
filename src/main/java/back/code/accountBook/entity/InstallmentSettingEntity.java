package back.code.accountBook.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
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
@Table(name = "installment_setting")
public class InstallmentSettingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int installmentId;
    private int totalAmount;
    private int totalMonths;
    private int currentMonth;
    private int monthlyAmount;
    private LocalDate startDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private AccountBookEntity account;
}
