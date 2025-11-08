package back.code.file.event;

import lombok.Value;
import java.util.List;

@Value
public class OrphanFileCleanupEvent {
    List<String> fileIds;
}
