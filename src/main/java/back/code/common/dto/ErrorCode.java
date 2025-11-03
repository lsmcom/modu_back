package back.code.common.dto;

import lombok.Getter;

/**
 * 애플리케이션 전역에서 공통적으로 사용하는 에러 코드 정의 열거형(Enum).
 *
 * <p>각 에러 코드는 다음 세 가지 정보를 포함한다:</p>
 * <ul>
 *     <li>{@code status} - HTTP 상태 코드 (예: 400, 404, 500 등)</li>
 *     <li>{@code errorCode} - 서비스 내부 비즈니스 에러 코드 (예: E400, T401 등)</li>
 *     <li>{@code message} - 클라이언트 및 개발자용 에러 메시지</li>
 * </ul>
 *
 * <p>이 Enum은 {@link back.code.common.dto.ApiErrorResponse}와 함께 사용되어
 * 에러 응답의 일관성을 유지하는 데 활용된다.</p>
 */
@Getter
public enum ErrorCode {

    /** 잘못된 요청 매개변수 */
    INVALID_PARAMETER(400, "E400" , "매개변수가 잘못되었습니다."),

    /** 인증되지 않은 요청 (로그인 필요) */
    UN_AUTHORIZED_ERROR(401, "E401", "인증이 필요합니다."),

    /** 유효하지 않은 토큰 */
    INVALID_TOKEN(401, "T401", "유효하지 않은 토큰입니다"),

    /** 만료된 토큰 */
    EXPIRED_TOKEN(401, "TE401", "만료된  토큰입니다"),

    /** 권한이 없는 요청 */
    FORBIDDEN_ERROR(403, "E403", "권한이 없습니다"),

    /** 요청한 리소스를 찾을 수 없음 */
    NOT_FOUND(404, "E404", "찾을 수 없습니다."),

    /** 서버 내부 오류 */
    INTERNAL_SERVER_ERROR(500, "E500", "서버에서 에러가 발생했습니다");

    private final  int status;
    private final String errorCode;
    private final String message;

    /**
     * 에러 코드 생성자.
     *
     * @param status HTTP 상태 코드
     * @param errorCode 비즈니스 에러 코드
     * @param message 에러 메시지
     */
    ErrorCode(int status, String errorCode, String message) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
    }
}
