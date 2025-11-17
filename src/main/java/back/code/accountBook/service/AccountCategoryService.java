package back.code.accountBook.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.code.accountBook.dto.AccountCategoryDTO;
import back.code.accountBook.entity.AccountCategoryEntity;
import back.code.accountBook.enums.AccountType;
import back.code.accountBook.repository.CategoryRepository;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountCategoryService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

     // 카테고리 조회
    @Transactional
    public List<AccountCategoryDTO.Response> categoryList(String userId) throws Exception{

        // 사용자 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 기본 카테고리
        List<AccountCategoryEntity> defaultCategories = categoryRepository.findAllByIsDefaultTrue();
        // 사용자 정의 카테고리
        List<AccountCategoryEntity> userCategories = categoryRepository.findAllByUser(user);

        List<AccountCategoryEntity> allCategories = new ArrayList<>();
        allCategories.addAll(defaultCategories);
        allCategories.addAll(userCategories);
        // dto로 변경
        List<AccountCategoryDTO.Response> response = allCategories.stream()
                .map(AccountCategoryDTO.Response::of)
                .toList();

        return response;
    }

    // 카테고리 추가
    @Transactional
    public AccountCategoryDTO.Response categoryAdd(AccountCategoryDTO.Request request) throws Exception{

        // 사용자 확인
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // DTO → 엔티티
        AccountCategoryEntity category = request.to(user);
        // dto로 변경
        AccountCategoryDTO.Response response = AccountCategoryDTO.Response.of(category);
        // 저장
        categoryRepository.save(category);

        return response;
    }

    // 카테고리 수정
    @Transactional
    public AccountCategoryDTO.Response categoryUpdate(AccountCategoryDTO.Request request) throws Exception{

        // 사용자 확인
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        AccountCategoryEntity category;
        // 기존 카테고리 수정 및 새 카테고리 생성
        if(request.getCategoryId() != 0) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        } else {
            category = request.to(user);
        }
        // dto로 변경
        AccountCategoryDTO.Response response = AccountCategoryDTO.Response.of(category);
        // 저장
        categoryRepository.save(category);

        return response;
    }

    // 카테고리 삭제
    @Transactional
    public AccountCategoryDTO.Response categoryDelete(String userId, int categoryId) throws Exception{

        // 카테고리 확인
        AccountCategoryEntity category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        // 해당 카테고리가 사용자의 것인지 확인
        if (!category.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("해당 카테고리에 대한 권한이 없습니다.");
        }
        // dto로 변경
        AccountCategoryDTO.Response response = AccountCategoryDTO.Response.of(category);
        // 삭제
        categoryRepository.delete(category);

        return response;
    }

    // 기본 카테고리 생성
    @Transactional
    public void createDefaultCategory(UserEntity user) {

        Object[][] defaultCategoriesData = {
                {"급여", "INCOME", "#66BB6A"},
                {"용돈", "INCOME", "#4DB6AC"},
                {"저축", "INCOME", "#5C6BC0"},
                {"식비", "EXPENSE", "#4BC0FF"},
                {"교통비", "EXPENSE", "#FF6384"},
                {"쇼핑", "EXPENSE", "#FF7043"},
                {"여가", "EXPENSE", "#FFCE56"},
                {"운동", "EXPENSE", "#AB47BC"},
        };

        List<AccountCategoryEntity> defaultCategories = new ArrayList<>();

        for (Object[] data : defaultCategoriesData) {
            String categoryName = (String) data[0];
            AccountType type = AccountType.valueOf((String) data[1]); // 핵심 수정
            String color = (String) data[2];

            // DTO 생성
            AccountCategoryDTO.Request dto = new AccountCategoryDTO.Request();
            dto.setCategoryName(categoryName);
            dto.setType(type);
            dto.setIsDefault(true);
            dto.setUserId(user.getUserId());

            // 엔티티 변환
            AccountCategoryEntity category = dto.to(user);
            category.setColor(color);

            defaultCategories.add(category);
        }

        categoryRepository.saveAll(defaultCategories);
    }

}
