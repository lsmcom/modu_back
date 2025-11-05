package back.code.memo.service;

import back.code.file.entity.FileEntity;
import back.code.file.repository.FileRepository;
import back.code.memo.entity.FileMemoMapEntity;
import back.code.memo.entity.FileMemoMapId;
import back.code.memo.entity.MemoEntity;
import back.code.memo.repository.FileMemoMapRepository;
import back.code.memo.repository.MemoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileMemoMapService {

    private final MemoRepository memoRepository;
    private final FileRepository fileRepository;
    private final FileMemoMapRepository fileMemoMapRepository;

    /** ✅ 메모와 파일 연결 */
    @Transactional
    public void linkFileToMemo(Integer memoId, String fileId) {
        MemoEntity memo = memoRepository.findById(memoId)
                .orElseThrow(() -> new RuntimeException("메모를 찾을 수 없습니다."));
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));

        FileMemoMapEntity map = FileMemoMapEntity.builder()
                .id(new FileMemoMapId(memoId, fileId))
                .memo(memo)
                .file(file)
                .build();

        fileMemoMapRepository.save(map);
        log.info("[MEMO-FILE] 매핑 완료: memoId={}, fileId={}", memoId, fileId);
    }

    /** ✅ 매핑 해제 (삭제 시) */
    @Transactional
    public void unlinkFileFromMemo(Integer memoId, String fileId) {
        fileMemoMapRepository.deleteById(new FileMemoMapId(memoId, fileId));
        log.info("[MEMO-FILE] 매핑 삭제: memoId={}, fileId={}", memoId, fileId);
    }
}
