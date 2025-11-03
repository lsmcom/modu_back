package back.code.file.entity;

import back.code.common.entity.BaseTimeEntity;
import back.code.user.entity.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "file")
public class FileEntity extends BaseTimeEntity {

    @Id
    private String fileId; // 파일 번호

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user; // 회원 정보

    private String fileType; // 파일 종류(예: profile, post)

    private String fileName; // 원본 파일명

    private String storedName; // 파일 저장 이름

    private String filePath; // 파일 경로

    private Long fileSize; // 파일 크기

    private String fileThumbName; // 썸네일 파일명(이미지일 경우)

}