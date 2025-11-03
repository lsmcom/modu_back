package back.code.common.handler;

import back.code.common.dto.ApiErrorResponse;
import back.code.common.dto.ErrorCode;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

/**
 * 서블릿 기반 예외(404, 403, 500 등)를 공통 API 응답 포맷으로 처리하는 컨트롤러.
 *
 * <p>Spring Boot 3.x 이상에서는 {@code NoHandlerFoundException} 설정이 제거되었기 때문에,
 * 존재하지 않는 URL(404)이나 내부 서버 오류(500) 등의 예외는
 * DispatcherServlet이 {@link ErrorController}를 통해 직접 위임한다.</p>
 *
 * <p>이 클래스는 {@link ApiErrorResponse} 포맷을 사용하여
 * 클라이언트에게 일관된 에러 응답 구조를 제공한다.</p>
 *
 * <p>예시 응답:</p>
 * <pre>
 * {
 *   "code": "E404",
 *   "message": "찾을 수 없습니다.",
 *   "timestamp": "2025-10-29 23:10:31",
 *   "fieldErrors": null
 * }
 * </pre>
 */
@Controller
@RequestMapping("${server.error.path:/error}")
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    /**
     * {@link ErrorAttributes}는 Spring Boot가 내부적으로 생성한 에러 정보를 담는 객체이다.
     *
     * @param errorAttributes 현재 요청의 에러 속성을 제공하는 객체
     */
    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    /**
     * DispatcherServlet에 의해 전달된 서블릿 오류를 처리하여
     * {@link ApiErrorResponse} 형식으로 변환한다.
     *
     * @param request 현재 웹 요청 컨텍스트
     * @return 상태 코드에 맞는 {@link ApiErrorResponse}
     */
    @RequestMapping
    public ResponseEntity<ApiErrorResponse> handleError(WebRequest request) {
        // Spring Boot가 수집한 오류 속성들 (status, error, message 등)
        Map<String, Object> attributes =
                errorAttributes.getErrorAttributes(request, ErrorAttributeOptions.defaults());

        int status = (int) attributes.getOrDefault("status", 500);
        HttpStatus httpStatus = HttpStatus.valueOf(status);

        ApiErrorResponse response;

        // 상태 코드별 커스텀 에러 응답 생성
        if (status == 404) {
            response = ApiErrorResponse.error(ErrorCode.NOT_FOUND);
        } else if (status == 403) {
            response = ApiErrorResponse.error(ErrorCode.FORBIDDEN_ERROR);
        } else {
            response = ApiErrorResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.status(httpStatus).body(response);
    }
}
