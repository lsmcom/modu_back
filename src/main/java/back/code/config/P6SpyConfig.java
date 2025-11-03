package back.code.config;

import com.p6spy.engine.spy.P6SpyOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

/**
 * P6Spy SQL 로그 포맷 설정 클래스.
 *
 * <p>애플리케이션 시작 시 한 번만 실행되어
 * {@link back.code.common.config.P6SpySqlFormatter}를 SQL 로그 포맷으로 지정한다.</p>
 */
@Configuration
public class P6SpyConfig {

    /**
     * P6Spy SQL 로그 포맷 설정을 초기화한다.
     */
    @PostConstruct
    public void configureP6SpyLogging() {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(
                "back.code.config.P6SpySqlFormatter"
        );
    }
}