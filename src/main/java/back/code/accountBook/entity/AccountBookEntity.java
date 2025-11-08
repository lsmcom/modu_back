package back.code.accountBook.entity;

import back.code.accountBook.enums.AccountMethod;
import back.code.accountBook.enums.AccountType;
import back.code.common.entity.BaseTimeEntity;
import back.code.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "account_book")
public class AccountBookEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int accountId;

    @Enumerated(EnumType.STRING)
    private AccountType type;
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    private AccountMethod method;
    private int amount;
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private AccountCategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private AccountSavingsGoalEntity goal;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountFileMappingEntity> files = new ArrayList<>();

}