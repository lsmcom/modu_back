package back.code.common.dto;

import back.code.common.utils.TimeFormatUtils;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * API 응답의 공통 포맷을 정의하는 DTO 클래스.
 *
 * @param <T> 응답 데이터 타입
 */
@Getter
public class ApiResponse<T> {

    /** 응답 발생 시각 */
    private final String timestamp;

    /** HTTP 상태 코드 (예: 200, 400, 404 등) */
    private final int status;

    /** 실제 응답 데이터 */
    private final T response;

    public ApiResponse(HttpStatus status, T response) {
        this.status = status.value();
        this.response = response;
        this.timestamp = TimeFormatUtils.getNowTime();
    }

    /** 성공 응답 (HTTP 200) */
    public static <T> ApiResponse<T> ok(T response) {
        return new ApiResponse<>(HttpStatus.OK, response);
    }

    /** 에러 응답 (HTTP 상태코드 + 메시지) */
    public static <T> ApiResponse<T> error(HttpStatus status, T message) {
        return new ApiResponse<>(status, message);
    }

    /** 에러 응답 (기본 400 BAD REQUEST) */
    public static <T> ApiResponse<T> error(T message) {
        return new ApiResponse<>(HttpStatus.BAD_REQUEST, message);
    }
}
