package back.code.admin.service.report;

import back.code.admin.dto.report.AdminReportDTO;
import back.code.admin.repository.AdminReportRepository;
import back.code.community.entity.CommunityReportEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReportService {

    private final AdminReportRepository reportRepository;

    /**
     * 전체 신고 조회 + seq 지정
     */
    @Transactional(readOnly = true)
    public List<AdminReportDTO> getAllReports() {

        // 1) 모든 신고 엔티티 조회 후 DTO 변환
        List<AdminReportDTO> list = new ArrayList<>(reportRepository.findAll()
                .stream()
                .map(AdminReportDTO::fromEntity)
                .toList());

        // 2) 생성일 기준 최신순 정렬 (옵션: 필요 없으면 제거 가능)
        list.sort((a, b) -> b.getCreateAt().compareTo(a.getCreateAt()));

        // 3) seq 번호 1부터 부여
        long seq = 1;
        for (AdminReportDTO dto : list) {
            dto.setSeq(seq++);
        }

        log.info("[AdminReportService] 신고 {}건 조회 (seq 부여 완료)", list.size());

        return list;
    }

    @Transactional
    public void updateReportStatus(Integer reportId, String newStatus) {

        CommunityReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고 정보를 찾을 수 없습니다."));

        // 문자열 → Enum 변환
        CommunityReportEntity.ReportStatus statusEnum;

        try {
            statusEnum = CommunityReportEntity.ReportStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 상태 값입니다: " + newStatus);
        }

        report.setReportStatus(statusEnum); // 상태 변경
    }
}
