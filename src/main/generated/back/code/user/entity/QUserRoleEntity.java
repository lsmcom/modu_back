package back.code.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserRoleEntity is a Querydsl query type for UserRoleEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserRoleEntity extends EntityPathBase<UserRoleEntity> {

    private static final long serialVersionUID = -1340954266L;

    public static final QUserRoleEntity userRoleEntity = new QUserRoleEntity("userRoleEntity");

    public final StringPath roleId = createString("roleId");

    public final StringPath roleName = createString("roleName");

    public QUserRoleEntity(String variable) {
        super(UserRoleEntity.class, forVariable(variable));
    }

    public QUserRoleEntity(Path<? extends UserRoleEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserRoleEntity(PathMetadata metadata) {
        super(UserRoleEntity.class, metadata);
    }

}

