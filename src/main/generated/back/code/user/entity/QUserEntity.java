package back.code.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserEntity is a Querydsl query type for UserEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserEntity extends EntityPathBase<UserEntity> {

    private static final long serialVersionUID = -165106736L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserEntity userEntity = new QUserEntity("userEntity");

    public final back.code.common.entity.QBaseTimeEntity _super = new back.code.common.entity.QBaseTimeEntity(this);

    public final StringPath addr = createString("addr");

    public final StringPath addrDetail = createString("addrDetail");

    public final StringPath agency = createString("agency");

    public final DatePath<java.time.LocalDate> birth = createDate("birth", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createAt = _super.createAt;

    public final StringPath email = createString("email");

    public final StringPath password = createString("password");

    public final StringPath phone = createString("phone");

    public final StringPath socialType = createString("socialType");

    public final StringPath status = createString("status");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateAt = _super.updateAt;

    public final StringPath userId = createString("userId");

    public final StringPath userName = createString("userName");

    public final StringPath userNick = createString("userNick");

    public final QUserRoleEntity userRole;

    public final DateTimePath<java.time.Instant> withdrawAt = createDateTime("withdrawAt", java.time.Instant.class);

    public final StringPath withdrawReason = createString("withdrawReason");

    public QUserEntity(String variable) {
        this(UserEntity.class, forVariable(variable), INITS);
    }

    public QUserEntity(Path<? extends UserEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserEntity(PathMetadata metadata, PathInits inits) {
        this(UserEntity.class, metadata, inits);
    }

    public QUserEntity(Class<? extends UserEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.userRole = inits.isInitialized("userRole") ? new QUserRoleEntity(forProperty("userRole")) : null;
    }

}

