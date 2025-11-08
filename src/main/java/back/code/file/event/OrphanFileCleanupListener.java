package back.code.file.event;

import back.code.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrphanFileCleanupListener {

    private final FileService fileService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handle(OrphanFileCleanupEvent event) {
        for (String fileId : event.getFileIds()) {
            try {
                fileService.deleteFileIfOrphan(fileId); // 필요시 REQUIRES_NEW 유지
            } catch (Exception e) {
                log.warn("[CLEANUP][AFTER_COMMIT] orphan 파일 정리 실패 fileId={}", fileId, e);
            }
        }
    }
}
