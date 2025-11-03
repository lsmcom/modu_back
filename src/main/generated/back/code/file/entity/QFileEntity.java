package back.code.file.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFileEntity is a Querydsl query type for FileEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFileEntity extends EntityPathBase<FileEntity> {

    private static final long serialVersionUID = -2036577230L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFileEntity fileEntity = new QFileEntity("fileEntity");

    public final back.code.common.entity.QBaseTimeEntity _super = new back.code.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createAt = _super.createAt;

    public final StringPath fileId = createString("fileId");

    public final StringPath fileName = createString("fileName");

    public final StringPath filePath = createString("filePath");

    public final NumberPath<Long> fileSize = createNumber("fileSize", Long.class);

    public final StringPath fileThumbName = createString("fileThumbName");

    public final StringPath fileType = createString("fileType");

    public final StringPath storedName = createString("storedName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateAt = _super.updateAt;

    public final back.code.user.entity.QUserEntity user;

    public QFileEntity(String variable) {
        this(FileEntity.class, forVariable(variable), INITS);
    }

    public QFileEntity(Path<? extends FileEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFileEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFileEntity(PathMetadata metadata, PathInits inits) {
        this(FileEntity.class, metadata, inits);
    }

    public QFileEntity(Class<? extends FileEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new back.code.user.entity.QUserEntity(forProperty("user"), inits.get("user")) : null;
    }

}

