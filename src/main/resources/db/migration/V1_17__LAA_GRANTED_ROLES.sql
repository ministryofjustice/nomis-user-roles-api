create table LAA_GRANTED_ROLES
(
    ROLE_ID                       NUMBER(10)                        not null,
    LOCAL_AUTHORITY_CODE          VARCHAR2(6)                       not null,
    ACTIVE_FLAG                   VARCHAR2(1)  default 'Y'          not null,
    EXPIRY_DATE                   DATE,
    CREATE_DATETIME               TIMESTAMP(9) default systimestamp not null,
    CREATE_USER_ID                VARCHAR2(32) default user()       not null,
    MODIFY_DATETIME               TIMESTAMP(9),
    MODIFY_USER_ID                VARCHAR2(32),
    AUDIT_TIMESTAMP               TIMESTAMP(9),
    AUDIT_USER_ID                 VARCHAR2(32),
    AUDIT_MODULE_NAME             VARCHAR2(65),
    AUDIT_CLIENT_USER_ID          VARCHAR2(64),
    AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39),
    AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64),
    AUDIT_ADDITIONAL_INFO         VARCHAR2(256),
    CONSTRAINT LAA_GRANTED_ROLES_PK PRIMARY KEY (ROLE_ID, LOCAL_AUTHORITY_CODE),
    CONSTRAINT LAA_GRANTED_ROLES_FK1 FOREIGN KEY (LOCAL_AUTHORITY_CODE)
        REFERENCES LOCAL_ADMIN_AUTHORITIES (LOCAL_AUTHORITY_CODE),
    CONSTRAINT LAA_GRANTED_ROLES_FK2 FOREIGN KEY (ROLE_ID)
        REFERENCES OMS_ROLES ("ROLE_ID")
);


