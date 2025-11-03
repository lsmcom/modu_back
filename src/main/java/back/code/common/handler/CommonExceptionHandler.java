package back.code.common.handler;

import back.code.common.dto.ApiErrorResponse;
import back.code.common.dto.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

/**
 * 전역 예외를 공통 포맷으로 처리하는 핸들러 클래스.
 *
 * <p>컨트롤러 계층에서 발생하는 다양한 예외를
 * {@link ApiErrorResponse} 형식으로 변환하여 클라이언트에게 일관된 에러 응답을 반환한다.</p>
 *
 * <p>핸들링 대상:</p>
 * <ul>
 *   <li>{@link RuntimeException} - 시스템 내부 예외</li>
 *   <li>{@link MethodArgumentNotValidException} - @Valid 유효성 검증 실패</li>
 *   <li>{@link ConstraintViolationException} - @RequestParam, @PathVariable 검증 실패</li>
 *   <li>{@link MissingServletRequestParameterException} - 쿼리 파라미터 누락</li>
 *   <li>{@link HttpRequestMethodNotSupportedException} - 지원하지 않는 HTTP 메서드 호출</li>
 *   <li>{@link HttpMessageNotReadableException} - 요청 본문(JSON) 파싱 실패</li>
 *   <li>{@link AccessDeniedException} - 권한 부족(403)</li>
 *   <li>{@link NoHandlerFoundException} - 존재하지 않는 경로(404)</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    /**
     * 서버 내부에서 발생한 예기치 못한 런타임 예외 처리.
     *
     * @param e {@link RuntimeException}
     * @return HTTP 500 상태의 {@link ApiErrorResponse}
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("==== RuntimeException 발생 ====", e);

        // 개발자가 던진 메시지가 있으면 그대로 반영
        String message = (e.getMessage() != null && !e.getMessage().isBlank())
                ? e.getMessage()
                : ErrorCode.INTERNAL_SERVER_ERROR.getMessage();

        ApiErrorResponse error = ApiErrorResponse.error(
                ErrorCode.INTERNAL_SERVER_ERROR.getErrorCode(),
                message
        );

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(error);
    }

    /**
     * @RequestBody 유효성 검증 실패 시 발생하는 예외 처리.
     * (예: @Valid 검증 실패 시)
     *
     * @param e {@link MethodArgumentNotValidException}
     * @return HTTP 400 상태의 {@link ApiErrorResponse}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("==== MethodArgumentNotValidException 발생 ====", e);

        List<ApiErrorResponse.FieldErrorDetail> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(ApiErrorResponse.FieldErrorDetail::from)
                .toList();

        ApiErrorResponse apiErrorResponse = ApiErrorResponse.error(
                ErrorCode.INVALID_PARAMETER.getErrorCode(),
                ErrorCode.INVALID_PARAMETER.getMessage(),
                fieldErrors
        );

        return ResponseEntity
                .status(ErrorCode.INVALID_PARAMETER.getStatus())
                .body(apiErrorResponse);
    }

    /**
     * @RequestParam 또는 @PathVariable 검증 실패 시 발생하는 예외 처리.
     *
     * @param e {@link ConstraintViolationException}
     * @return HTTP 400 상태의 {@link ApiErrorResponse}
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ApiErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("==== ConstraintViolationException 발생 ====", e);

        List<ApiErrorResponse.FieldErrorDetail> fieldErrors = e.getConstraintViolations()
                .stream()
                .map(ApiErrorResponse.FieldErrorDetail::from)
                .toList();

        ApiErrorResponse apiErrorResponse = ApiErrorResponse.error(
                ErrorCode.INVALID_PARAMETER.getErrorCode(),
                ErrorCode.INVALID_PARAMETER.getMessage(),
                fieldErrors
        );

        return ResponseEntity
                .status(ErrorCode.INVALID_PARAMETER.getStatus())
                .body(apiErrorResponse);
    }

    /**
     * GET 요청 시 쿼리 파라미터가 누락된 경우 발생하는 예외 처리.
     *
     * @param e {@link MissingServletRequestParameterException}
     * @return HTTP 400 상태의 {@link ApiErrorResponse}
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e
    ) {
        log.error("==== MissingServletRequestParameterException 발생 ====", e);

        ApiErrorResponse.FieldErrorDetail fieldError =
                ApiErrorResponse.FieldErrorDetail.of(e.getParameterName(), "", e.getMessage());

        ApiErrorResponse apiErrorResponse = ApiErrorResponse.error(
                ErrorCode.INVALID_PARAMETER.getErrorCode(),
                ErrorCode.INVALID_PARAMETER.getMessage(),
                List.of(fieldError)
        );

        return ResponseEntity
                .status(ErrorCode.INVALID_PARAMETER.getStatus())
                .body(apiErrorResponse);
    }

    /**
     * 지원하지 않는 HTTP 메서드로 요청했을 때 발생하는 예외 처리.
     * (예: GET만 허용하는 API에 POST 요청)
     *
     * @param e {@link HttpRequestMethodNotSupportedException}
     * @return HTTP 405 상태의 {@link ApiErrorResponse}
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("==== HttpRequestMethodNotSupportedException 발생 ====", e);

        ApiErrorResponse apiErrorResponse = ApiErrorResponse.error(
                ErrorCode.INVALID_PARAMETER.getErrorCode(),
                "지원하지 않는 요청 메서드입니다."
        );

        return ResponseEntity
                .status(405)
                .body(apiErrorResponse);
    }

    /**
     * 요청 본문(JSON 등) 파싱 실패 시 발생하는 예외 처리.
     * (예: 잘못된 형식의 JSON 전송)
     *
     * @param e {@link HttpMessageNotReadableException}
     * @return HTTP 400 상태의 {@link ApiErrorResponse}
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("==== HttpMessageNotReadableException 발생 ====", e);

        ApiErrorResponse apiErrorResponse = ApiErrorResponse.error(
                ErrorCode.INVALID_PARAMETER.getErrorCode(),
                "요청 본문 형식이 올바르지 않습니다."
        );

        return ResponseEntity
                .status(ErrorCode.INVALID_PARAMETER.getStatus())
                .body(apiErrorResponse);
    }

    /**
     * 인증은 되었지만 접근 권한이 없는 경우 발생하는 예외 처리.
     * (Spring Security에서 발생)
     *
     * @param e {@link AccessDeniedException}
     * @return HTTP 403 상태의 {@link ApiErrorResponse}
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("==== AccessDeniedException 발생 ====", e);

        ApiErrorResponse apiErrorResponse = ApiErrorResponse.error(ErrorCode.FORBIDDEN_ERROR);

        return ResponseEntity
                .status(ErrorCode.FORBIDDEN_ERROR.getStatus())
                .body(apiErrorResponse);
    }
}
