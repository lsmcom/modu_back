package back.code.inquiry.entity;

// 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
public enum InquiryStatus {
    TEMP, // 임시 저장
    SUBMITTED, // 제출됨 (답변 대기)
    ANSWERED // 답변 완료
}