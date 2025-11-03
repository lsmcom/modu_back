package back.code.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserSettingEntity is a Querydsl query type for UserSettingEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserSettingEntity extends EntityPathBase<UserSettingEntity> {

    private static final long serialVersionUID = 1356842438L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserSettingEntity userSettingEntity = new QUserSettingEntity("userSettingEntity");

    public final StringPath alarmAllowed = createString("alarmAllowed");

    public final StringPath locationInfoAgreed = createString("locationInfoAgreed");

    public final StringPath marketingInfoAgreed = createString("marketingInfoAgreed");

    public final DateTimePath<java.time.LocalDateTime> marketingRejectDate = createDateTime("marketingRejectDate", java.time.LocalDateTime.class);

    public final StringPath personalInfoAgreed = createString("personalInfoAgreed");

    public final StringPath settingId = createString("settingId");

    public final StringPath theDayOfWeek = createString("theDayOfWeek");

    public final StringPath themeMode = createString("themeMode");

    public final QUserEntity user;

    public QUserSettingEntity(String variable) {
        this(UserSettingEntity.class, forVariable(variable), INITS);
    }

    public QUserSettingEntity(Path<? extends UserSettingEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserSettingEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserSettingEntity(PathMetadata metadata, PathInits inits) {
        this(UserSettingEntity.class, metadata, inits);
    }

    public QUserSettingEntity(Class<? extends UserSettingEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUserEntity(forProperty("user"), inits.get("user")) : null;
    }

}

