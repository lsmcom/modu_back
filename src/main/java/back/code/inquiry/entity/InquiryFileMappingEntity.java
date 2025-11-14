package back.code.inquiry.entity;

import back.code.common.entity.BaseCreateTimeEntity;
import back.code.file.entity.FileEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inquiry_file_mapping")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryFileMappingEntity extends BaseCreateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inquiryFileMappingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private InquiryEntity inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntity file;
}
