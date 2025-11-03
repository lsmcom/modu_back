package back.code.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * JPA Auditing(자동 생성·수정 시간 관리) 설정 클래스.
 *
 * <p>BaseTimeEntity 등에서 {@code @CreatedDate}, {@code @LastModifiedDate}
 * 애노테이션을 통해 엔티티의 생성 및 수정 시간을 자동으로 채워준다.</p>
 *
 * <p>Auditing 기능은 {@link EnableJpaAuditing} 으로 활성화되며,
 * {@link DateTimeProvider} Bean을 통해 현재 시각을 주입받는다.</p>
 *
 * <p>현재 프로젝트는 한국(KST, UTC+9) 기준으로 운영되므로,
 * 별도의 타임존 변환 없이 {@link LocalDateTime#now()}를 사용한다.</p>
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef="auditorDateTimeProvider")
public class JpaAuditingConfig {

	/**
	 * Auditing에 사용할 현재 시각 공급자 Bean.
	 *
	 * <p>Spring Data JPA가 엔티티의 {@code @CreatedDate}, {@code @LastModifiedDate} 필드에
	 * 이 Bean이 제공하는 {@link LocalDateTime} 값을 자동으로 할당한다.</p>
	 *
	 * @return 현재 로컬 시각(KST)을 반환하는 DateTimeProvider
	 */
	@Bean(name="auditorDateTimeProvider")
	public DateTimeProvider auditorDateTimeProvider() {
		return () -> Optional.of(LocalDateTime.now());
	}

}