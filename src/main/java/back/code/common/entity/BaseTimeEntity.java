package back.code.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 모든 엔티티의 생성시간, 수정시간을 자동 관리하기 위한 공통 상위 클래스.
 *
 * <p>스프링 데이터 JPA의 Auditing 기능을 사용하여
 * {@code @CreatedDate}, {@code @LastModifiedDate} 필드를 자동으로 채운다.</p>
 *
 * <p>응답 시 JSON 포맷은 "yyyy-MM-dd HH:mm:ss"로 통일된다.</p>
 */
@Getter
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity implements Serializable {

    /** 생성 시간 (엔티티 최초 저장 시 자동 입력) */
    @CreatedDate
    @Column(updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;

    /** 수정 시간 (엔티티 수정 시 자동 업데이트) */
    @LastModifiedDate
    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateAt;
}
