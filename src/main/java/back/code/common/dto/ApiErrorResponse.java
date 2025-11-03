package back.code.common.dto;

import back.code.common.utils.TimeFormatUtils;
import jakarta.validation.ConstraintViolation;
import lombok.Getter;
import org.springframework.validation.FieldError;

import java.util.List;

/**
 * API 요청 실패 시 클라이언트에게 반환되는 공통 에러 응답 DTO.
 *
 * <p>단일 에러 또는 다중 필드 유효성 검증 에러를 모두 지원한다.</p>
 *
 * <p>시간 정보는 {@link TimeFormatUtils}를 통해 "yyyy-MM-dd HH:mm:ss"로 자동 생성된다.</p>
 */
@Getter
public class ApiErrorResponse {

    private final String code;     // 비즈니스 에러 코드 (ex: E400, T401)
    private final String message;  // 에러 메시지
    private final String timestamp; // 에러 발생 시각
    private final List<FieldErrorDetail> fieldErrors; // 필드별 유효성 에러 목록

    /**
     * ApiErrorResponse 생성자 (외부에서 직접 호출하지 않음).
     *
     * @param code 비즈니스 에러 코드
     * @param message 에러 메시지
     * @param fieldErrors 유효성 검사 에러 목록 (null 가능)
     */
    private ApiErrorResponse(String code, String message, List<FieldErrorDetail> fieldErrors) {
        this.code = code;
        this.message = message;
        this.timestamp = TimeFormatUtils.getNowTime();
        this.fieldErrors = fieldErrors;
    }

    /**
     * 단일 에러 응답 생성.
     *
     * @param code 에러 코드 (예: "E400")
     * @param message 에러 메시지
     * @return 단일 에러 응답 객체
     */
    public static ApiErrorResponse error(String code, String message) {
        return new ApiErrorResponse(code, message, null);
    }

    /**
     * 다중 필드 유효성 에러를 포함하는 에러 응답 생성.
     *
     * @param code 에러 코드 (예: "E400")
     * @param message 에러 메시지
     * @param fieldErrors 필드별 에러 목록
     * @return 필드 에러 포함 에러 응답 객체
     */
    public static ApiErrorResponse error(String code, String message, List<FieldErrorDetail> fieldErrors) {
        return new ApiErrorResponse(code, message, fieldErrors);
    }

    /**
     * {@link ErrorCode} enum 기반의 에러 응답 생성.
     *
     * @param errorCode {@link ErrorCode} 열거형 상수
     * @return 에러 코드 기반 응답 객체
     */
    public static ApiErrorResponse error(ErrorCode errorCode) {
        return new ApiErrorResponse(errorCode.getErrorCode(), errorCode.getMessage(), null);
    }

    /**
     * 개별 필드의 유효성 검사 에러 정보를 담는 내부 DTO 클래스.
     */
    @Getter
    public static class FieldErrorDetail {
        private final String field;
        private final String value;
        private final String message;

        /**
         * FieldErrorDetail 생성자 (외부에서 직접 호출하지 않음).
         *
         * @param field 에러 필드명
         * @param value 잘못된 값
         * @param message 에러 메시지
         */
        private FieldErrorDetail(String field, String value, String message) {
            this.field = field;
            this.value = value;
            this.message = message;
        }

        /**
         * Spring의 {@link FieldError} 객체를 기반으로 FieldErrorDetail 생성.
         *
         * @param fieldError Spring Validation의 FieldError
         * @return 변환된 FieldErrorDetail 객체
         */
        public static FieldErrorDetail from(FieldError fieldError) {
            return new FieldErrorDetail(
                    fieldError.getField(),
                    fieldError.getRejectedValue() == null ? "" : fieldError.getRejectedValue().toString(),
                    fieldError.getDefaultMessage()
            );
        }

        /**
         * {@link ConstraintViolation} 객체를 기반으로 FieldErrorDetail 생성.
         *
         * @param violation Bean Validation ConstraintViolation
         * @return 변환된 FieldErrorDetail 객체
         */
        public static FieldErrorDetail from(ConstraintViolation<?> violation) {
            String fieldName = violation.getPropertyPath().toString();
            fieldName = fieldName.substring(fieldName.lastIndexOf('.') + 1);
            return new FieldErrorDetail(
                    fieldName,
                    violation.getInvalidValue() == null ? "" : violation.getInvalidValue().toString(),
                    violation.getMessage()
            );
        }

        /**
         * 직접 필드명, 값, 메시지를 지정하여 FieldErrorDetail 생성.
         *
         * @param field 필드명
         * @param value 잘못된 값
         * @param message 에러 메시지
         * @return FieldErrorDetail 객체
         */
        public static FieldErrorDetail of(String field, String value, String message) {
            return new FieldErrorDetail(field, value, message);
        }
    }
}
