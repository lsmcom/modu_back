package back.code.admin.service.inquiry;


import back.code.admin.dto.inquiry.AdminInquiryDTO;
import back.code.admin.repository.AdminInquiryRepository;
import back.code.inquiry.entity.InquiryEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminInquiryService {

    private final AdminInquiryRepository inquiryRepository;

    /**
     * isPublic = false (비공개 문의 조회)
     */
    @Transactional(readOnly = true)
    public List<AdminInquiryDTO> getPrivateInquiries() {

        List<AdminInquiryDTO> list = new java.util.ArrayList<>(inquiryRepository.findByIsPublicFalse()
                .stream()
                .map(AdminInquiryDTO::fromEntity)
                .toList());

        // 최신순 정렬
        list.sort((a, b) -> b.getCreateAt().compareTo(a.getCreateAt()));

        // seq 부여
        long seq = 1L;
        for (AdminInquiryDTO dto : list) {
            dto.setSeq(seq++);
        }

        return list;
    }

}
