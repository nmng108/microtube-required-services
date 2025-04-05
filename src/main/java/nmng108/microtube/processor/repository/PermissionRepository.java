package nmng108.microtube.processor.repository;

import nmng108.microtube.processor.dto.auth.GrantedAuthorityDTO;
import nmng108.microtube.processor.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    @NativeQuery(value = """
        SELECT DISTINCT *
        FROM (SELECT p.*
              FROM permission_relation_user pru
                JOIN permission p ON p.ID = pru.PERMISSION_ID
              WHERE pru.USER_ID = :userId
                AND p.SYS_ENTITY_ID IS NULL
                AND p.SYS_ACTION_ID IS NULL
                AND pru.DELETED_AT IS NULL AND pru.DELETED_BY IS NULL
              UNION
              SELECT p.*
              FROM permission_group_relation_user pgru
                JOIN permission_group_relation_permission pgrp ON pgru.PERMISSION_GROUP_ID = pgrp.PERMISSION_GROUP_ID
                JOIN permission p ON pgrp.PERMISSION_ID = p.ID
              WHERE pgru.USER_ID = :userId
                AND p.SYS_ENTITY_ID IS NULL
                AND p.SYS_ACTION_ID IS NULL
                AND pgru.DELETED_AT IS NULL AND pgru.DELETED_BY IS NULL
                AND pgrp.DELETED_AT IS NULL AND pgrp.DELETED_BY IS NULL
        ) p
        WHERE p.ID NOT IN (SELECT PERMISSION_ID
                           FROM permission_exclusion
                           WHERE USER_ID = :userId
                             AND DELETED_AT IS NULL AND DELETED_BY IS NULL) -- not directly excluded
         AND DELETED_AT IS NULL AND DELETED_BY IS NULL
        UNION
        -- Case 2: entity & action
        SELECT DISTINCT *
        FROM (SELECT p.*
              FROM permission_relation_user pru
                JOIN permission p ON p.ID = pru.PERMISSION_ID
              WHERE pru.USER_ID = :userId
                AND p.SYS_ENTITY_ID IS NOT NULL
                AND p.SYS_ACTION_ID IS NOT NULL
                AND pru.DELETED_AT IS NULL AND pru.DELETED_BY IS NULL
              UNION
              SELECT p.*
              FROM permission_group_relation_user pgru
                JOIN permission_group_relation_permission pgrp ON pgru.PERMISSION_GROUP_ID = pgrp.PERMISSION_GROUP_ID
                JOIN permission p ON pgrp.PERMISSION_ID = p.ID
              WHERE pgru.USER_ID = :userId
                AND p.SYS_ENTITY_ID IS NOT NULL
                AND p.SYS_ACTION_ID IS NOT NULL
                AND pgru.DELETED_AT IS NULL AND pgru.DELETED_BY IS NULL
                AND pgrp.DELETED_AT IS NULL AND pgrp.DELETED_BY IS NULL
        ) p
        WHERE p.ID NOT IN (SELECT PERMISSION_ID
                           FROM permission_exclusion
                           WHERE USER_ID = :userId
                             AND DELETED_AT IS NULL AND DELETED_BY IS NULL) -- not directly excluded
         AND DELETED_AT IS NULL AND DELETED_BY IS NULL
        UNION
        -- Case 3: entity & no action & entity does not exist in `permission_exclusion` (no child permission is blocked)
        SELECT DISTINCT *
        FROM (SELECT p.*
              FROM permission_relation_user pru
                JOIN permission p ON p.ID = pru.PERMISSION_ID
              WHERE pru.USER_ID = :userId
                AND p.SYS_ENTITY_ID IS NOT NULL
                AND p.SYS_ACTION_ID IS NULL
                AND pru.DELETED_AT IS NULL AND pru.DELETED_BY IS NULL
              UNION
              SELECT p.*
              FROM permission_group_relation_user pgru
                JOIN permission_group_relation_permission pgrp ON pgru.PERMISSION_GROUP_ID = pgrp.PERMISSION_GROUP_ID
                JOIN permission p ON pgrp.PERMISSION_ID = p.ID
              WHERE pgru.USER_ID = :userId
                AND p.SYS_ENTITY_ID IS NOT NULL
                AND p.SYS_ACTION_ID IS NULL
                AND pgru.DELETED_AT IS NULL AND pgru.DELETED_BY IS NULL
                AND pgrp.DELETED_AT IS NULL AND pgrp.DELETED_BY IS NULL
        ) p
        WHERE p.ID NOT IN (SELECT PERMISSION_ID
                           FROM permission_exclusion
                           WHERE USER_ID = :userId
                             AND DELETED_AT IS NULL AND DELETED_BY IS NULL) -- not directly excluded
          AND DELETED_AT IS NULL AND DELETED_BY IS NULL
          -- has no blocked child
          AND SYS_ENTITY_ID NOT IN (SELECT s_p.SYS_ENTITY_ID
                                    FROM permission_exclusion pe
                                        JOIN permission s_p ON pe.PERMISSION_ID = s_p.ID
                                    WHERE USER_ID = :userId
                                      AND s_p.SYS_ENTITY_ID IS NOT NULL
                                      AND s_p.SYS_ACTION_ID IS NOT NULL
                                      AND pe.DELETED_AT IS NULL AND pe.DELETED_BY IS NULL
                                      AND s_p.DELETED_AT IS NULL AND s_p.DELETED_BY IS NULL)
        UNION
        -- Case 4: entity & no action & entity exists in `permission_exclusion` (at least 1 child permission is blocked)
        -- -> Select detail permissions (which have non-null SYS_ACTION_ID) for these entities and exclude blocked ones
        SELECT p.*
        FROM permission p
            JOIN (
                SELECT DISTINCT pe.SYS_ENTITY_ID, pe.SYS_ACTION_ID
                FROM (
                    SELECT p.*
                    FROM permission_relation_user pru
                      JOIN permission p ON p.ID = pru.PERMISSION_ID
                    WHERE pru.USER_ID = :userId
                      AND p.SYS_ENTITY_ID IS NOT NULL
                      AND p.SYS_ACTION_ID IS NULL
                      AND pru.DELETED_AT IS NULL AND pru.DELETED_BY IS NULL
                    UNION
                    SELECT p.*
                    FROM permission_group_relation_user pgru
                      JOIN permission_group_relation_permission pgrp ON pgru.PERMISSION_GROUP_ID = pgrp.PERMISSION_GROUP_ID
                      JOIN permission p ON pgrp.PERMISSION_ID = p.ID
                    WHERE pgru.USER_ID = :userId
                      AND p.SYS_ENTITY_ID IS NOT NULL
                      AND p.SYS_ACTION_ID IS NULL
                      AND pgru.DELETED_AT IS NULL AND pgru.DELETED_BY IS NULL
                      AND pgrp.DELETED_AT IS NULL AND pgrp.DELETED_BY IS NULL
                ) p
                    JOIN (
                        SELECT DISTINCT s_p.SYS_ENTITY_ID, s_p.SYS_ACTION_ID
                        FROM permission_exclusion pe
                               JOIN permission s_p ON pe.PERMISSION_ID = s_p.ID
                        WHERE USER_ID = :userId
                          AND s_p.SYS_ENTITY_ID IS NOT NULL
                          AND s_p.SYS_ACTION_ID IS NOT NULL
                          AND pe.DELETED_AT IS NULL AND pe.DELETED_BY IS NULL
                          AND s_p.DELETED_AT IS NULL AND s_p.DELETED_BY IS NULL
                    ) pe ON p.SYS_ENTITY_ID = pe.SYS_ENTITY_ID -- has at least 1 blocked child
                    WHERE p.ID NOT IN (SELECT PERMISSION_ID
                                       FROM permission_exclusion
                                       WHERE USER_ID = :userId
                                         AND DELETED_AT IS NULL AND DELETED_BY IS NULL) -- not directly excluded
                      AND p.DELETED_AT IS NULL AND p.DELETED_BY IS NULL
            ) sys_ea ON p.SYS_ENTITY_ID = sys_ea.SYS_ENTITY_ID AND p.SYS_ACTION_ID != sys_ea.SYS_ACTION_ID
        """, sqlResultSetMapping = "sqlResultSetMappingPermission")
    <T> List<T> findByUserId(long userId, Class<T> type);

//    @NativeQuery(name = "findByUserId", sqlResultSetMapping = "sqlResultSetMappingPermission")
//    List<GrantedAuthorityDTO> findGrantedAuthorityDTOByUserId(long userId);
}
