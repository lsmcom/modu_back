package back.code.accountBook.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import back.code.accountBook.dto.AccountSearchDTO;
import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.enums.AccountMethod;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AccountSearchSpecification implements Specification<AccountBookEntity> {

    private AccountSearchDTO.Request search;

    public AccountSearchSpecification(AccountSearchDTO.Request search) {
        this.search = search;
    }

    @Override
    public Predicate toPredicate(Root<AccountBookEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // 검색어
        if (search.getKeyword() != null && !search.getKeyword().trim().isEmpty()) {
            String likeText = "%" + search.getKeyword().trim() + "%";

            List<Predicate> orPredicates = new ArrayList<>();

            orPredicates.add(cb.like(root.get("content"), likeText));
            orPredicates.add(cb.like(root.get("category").get("categoryName"), likeText));
            orPredicates.add(cb.like(root.get("amount").as(String.class), likeText));

            predicates.add(cb.or(orPredicates.toArray(new Predicate[0])));

        }

        // 사용자 ID
        predicates.add(cb.equal(root.get("user").get("userId"), search.getUserId()));

        // 날짜 범위
        if (search.getStartDate() != null && !search.getStartDate().isEmpty()) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("date"), LocalDate.parse(search.getStartDate())));
        }
        if (search.getEndDate() != null && !search.getEndDate().isEmpty()) {
            predicates.add(cb.lessThanOrEqualTo(root.get("date"), LocalDate.parse(search.getEndDate())));
        }

        // 금액 범위
        if (search.getMinAmount() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), search.getMinAmount()));
        }
        if (search.getMaxAmount() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("amount"), search.getMaxAmount()));
        }

        // 카테고리
        if (search.getCategory() != null && !search.getCategory().isEmpty()) {
            predicates.add(cb.equal(root.get("category").get("categoryName"), search.getCategory()));
        }

        // 방법
        if (search.getMethod() != null && !search.getMethod().isEmpty()) {
            predicates.add(cb.equal(root.get("method"), search.getMethod()));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
