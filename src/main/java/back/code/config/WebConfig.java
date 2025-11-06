package back.code.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * 웹 관련 전역 설정 클래스.
 *
 * <p>정적 리소스(이미지 파일 등)의 외부 경로 매핑</p>
 * <p>업로드 파일 크기 제한 설정</p>
 *
 * <p>이 설정은 {@link WebMvcConfigurer}를 구현하여 Spring MVC 설정을 확장한다.</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * application.yml에 정의된 이미지 저장 경로 주입.
     */
    @Value("${server.file.upload.path}")
    private String filePath;

    /**
     * 정적 리소스(이미지) 요청 경로 매핑 설정.
     *
     * <p>이미지 캐싱을 방지하기 위해 {@code setCachePeriod(0)} 설정.</p>
     *
     * @param registry Spring MVC의 리소스 핸들러 등록 객체
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/imgs/**") // URL 접근 경로
                .addResourceLocations("file:" + filePath) // 실제 파일 경로
                .setCachePeriod(0) // 캐시 비활성화 (개발 환경용)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }

    /**
     * 업로드 파일 크기 제한 설정.
     *
     * <p>단일 파일 및 전체 요청의 최대 크기를 각각 50MB로 제한한다.</p>
     *
     * <p>Spring Boot의 기본 Multipart 설정보다 우선 적용된다.</p>
     *
     * @return MultipartConfigElement (서블릿 multipart 설정 객체)
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.of(50, DataUnit.MEGABYTES));   // 단일 파일 최대 크기
        factory.setMaxRequestSize(DataSize.of(50, DataUnit.MEGABYTES)); // 전체 요청 최대 크기
        return factory.createMultipartConfig();
    }
}
