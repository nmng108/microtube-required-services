package nmng108.microtube.processor.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import nmng108.microtube.processor.entity.Permission;
import nmng108.microtube.processor.repository.PermissionRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionRepositoryDecorator extends SimpleJpaRepository<Permission, Integer> implements PermissionRepository {
    @PersistenceContext
    EntityManager entityManager;
    PermissionRepository permissionRepository;
    Class<Permission> domainClass = Permission.class;

    public PermissionRepositoryDecorator(EntityManager entityManager, PermissionRepository permissionRepository) {
        super(Permission.class, entityManager);
        this.entityManager = entityManager;
        this.permissionRepository = permissionRepository;
    }

    /**
     * If the param @{code type} is entity class ({@link Permission} in this case),
     * the original generated method will return {@code List<Object[]>} (including objects of all defined mappings)
     * instead of {@code List<Permission>}.
     * This override is responsible for extracting exact objects of entity class.
     */
    @Override
    public <T> List<T> findByUserId(long userId, Class<T> type) {
        List resultList = permissionRepository.findByUserId(userId, type);

        if (!resultList.isEmpty() && resultList.getFirst() instanceof Object[]) {
            return resultList.stream().map((o) -> {
                if (o instanceof Object[] arr) {
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i].getClass().equals(type)) {
                            return arr[i];
                        }
                    }

                }
                return o;
            }).toList();
        }

        return resultList;
    }
}
