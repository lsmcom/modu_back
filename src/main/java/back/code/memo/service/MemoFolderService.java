package back.code.memo.service;

import back.code.memo.dto.MemoFolderDTO;
import java.util.List;

public interface MemoFolderService {
    List<MemoFolderDTO> getAllFolders();
    List<MemoFolderDTO> getUserFolders(String userId);
    MemoFolderDTO addFolder(MemoFolderDTO dto);
    MemoFolderDTO updateFolder(Integer folderId, MemoFolderDTO dto);
    void deleteFolder(Integer folderId);
}
