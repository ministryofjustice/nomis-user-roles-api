create table REFERENCE_DOMAINS
(
    DOMAIN                        VARCHAR2(12 char)                      not null
        constraint REFERENCE_DOMAINS_PK
            primary key,
    DESCRIPTION                   VARCHAR2(40 char)                      not null,
    DOMAIN_STATUS                 VARCHAR2(12 char)                      not null,
    OWNER_CODE                    VARCHAR2(12 char)                      not null,
    APPLN_CODE                    VARCHAR2(12 char)                      not null,
    OLD_CODE_TABLE                VARCHAR2(40 char),
    PARENT_DOMAIN                 VARCHAR2(12 char)
        constraint REF_DOMAIN_REF_DOMAIN_FK1
            references REFERENCE_DOMAINS,
    CODE_LENGTH                   NUMBER(3),
    CREATE_DATETIME               TIMESTAMP(9)      default systimestamp not null,
    CREATE_USER_ID                VARCHAR2(32 char) default user()       not null,
    MODIFY_DATETIME               TIMESTAMP(9),
    MODIFY_USER_ID                VARCHAR2(32 char),
    SUPER_SET_DOMAIN              VARCHAR2(12 char)
        constraint REF_DOMAIN_REF_DOMAIN_FK2
            references REFERENCE_DOMAINS,
    AUDIT_TIMESTAMP               TIMESTAMP(9),
    AUDIT_USER_ID                 VARCHAR2(32 char),
    AUDIT_MODULE_NAME             VARCHAR2(65 char),
    AUDIT_CLIENT_USER_ID          VARCHAR2(64 char),
    AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39 char),
    AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64 char),
    AUDIT_ADDITIONAL_INFO         VARCHAR2(256 char)
);

comment on table REFERENCE_DOMAINS is 'The reference doamin- Retrofitted- Retrofitted';
comment on column REFERENCE_DOMAINS.DOMAIN is 'Domain of the Reference Code';
comment on column REFERENCE_DOMAINS.DESCRIPTION is 'Description of the Domain';
comment on column REFERENCE_DOMAINS.DOMAIN_STATUS is 'Reference Code ( DOMAIN_STS ) Status of the Domain';
comment on column REFERENCE_DOMAINS.OWNER_CODE is 'Reference Code ( USER_CLS ) : Domain Owner Type (Party who have the right to modify codes)';
comment on column REFERENCE_DOMAINS.APPLN_CODE is 'Reference Code ( APPLN ) Business Application Which uses the Domain';
comment on column REFERENCE_DOMAINS.OLD_CODE_TABLE is 'The name of the old table in Previous Version';
comment on column REFERENCE_DOMAINS.PARENT_DOMAIN is 'The parent domain of this domain (It is for modeling hierachical structure of domain)';
comment on column REFERENCE_DOMAINS.CODE_LENGTH is 'The length of the code in previous version';
comment on column REFERENCE_DOMAINS.CREATE_DATETIME is 'The timestamp when the record is created';
comment on column REFERENCE_DOMAINS.CREATE_USER_ID is 'The user who creates the record';
comment on column REFERENCE_DOMAINS.MODIFY_DATETIME is 'The timestamp when the record is modified ';
comment on column REFERENCE_DOMAINS.MODIFY_USER_ID is 'The user who modifies the record';
create index REF_DOMAIN_REF_DOMAIN_FK1
    on REFERENCE_DOMAINS (PARENT_DOMAIN);
create index REF_DOMAIN_REF_DOMAIN_FK2
    on REFERENCE_DOMAINS (SUPER_SET_DOMAIN);

INSERT INTO REFERENCE_DOMAINS (DOMAIN, DESCRIPTION, DOMAIN_STATUS, OWNER_CODE, APPLN_CODE, OLD_CODE_TABLE,
                               PARENT_DOMAIN, CODE_LENGTH, CREATE_DATETIME, CREATE_USER_ID, MODIFY_DATETIME,
                               MODIFY_USER_ID, SUPER_SET_DOMAIN, AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME,
                               AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS, AUDIT_CLIENT_WORKSTATION_NAME,
                               AUDIT_ADDITIONAL_INFO)
VALUES ('STAFF_SKILLS', 'Staff Member Skills', 'FINAL', 'ADMIN', 'TAG', null, null, null,
        TO_TIMESTAMP('2006-06-28 16:19:39.780575000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER',
        TO_TIMESTAMP('2010-03-07 16:27:58.073910000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER', null,
        TO_TIMESTAMP('2010-03-07 16:27:58.073946000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER',
        'sqlplus@psa10766 (TNS V1-V3)', 'oracle', '10.96.137.42', null, null);
INSERT INTO REFERENCE_DOMAINS (DOMAIN, DESCRIPTION, DOMAIN_STATUS, OWNER_CODE, APPLN_CODE, OLD_CODE_TABLE,
                               PARENT_DOMAIN, CODE_LENGTH, CREATE_DATETIME, CREATE_USER_ID, MODIFY_DATETIME,
                               MODIFY_USER_ID, SUPER_SET_DOMAIN, AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME,
                               AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS, AUDIT_CLIENT_WORKSTATION_NAME,
                               AUDIT_ADDITIONAL_INFO)
VALUES ('SKL_SUB_TYPE', 'Staff Skill Sub-Types', 'CONF', 'USER', 'TAG', null, 'STAFF_SKILLS', null,
        TO_TIMESTAMP('2006-06-28 16:19:39.557888000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER',
        TO_TIMESTAMP('2010-03-07 16:27:58.072648000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER', null,
        TO_TIMESTAMP('2010-03-07 16:27:58.072877000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER',
        'sqlplus@psa10766 (TNS V1-V3)', 'oracle', '10.96.137.42', null, null);
INSERT INTO REFERENCE_DOMAINS (DOMAIN, DESCRIPTION, DOMAIN_STATUS, OWNER_CODE, APPLN_CODE, OLD_CODE_TABLE,
                               PARENT_DOMAIN, CODE_LENGTH, CREATE_DATETIME, CREATE_USER_ID, MODIFY_DATETIME,
                               MODIFY_USER_ID, SUPER_SET_DOMAIN, AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME,
                               AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS, AUDIT_CLIENT_WORKSTATION_NAME,
                               AUDIT_ADDITIONAL_INFO)
VALUES ('SORT_ORDER', 'Sort Order', 'FINAL-SYSCON', 'ADMIN', 'TAG', null, null, null,
        TO_TIMESTAMP('2006-06-28 16:19:39.592938000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER',
        TO_TIMESTAMP('2010-03-07 16:27:58.073150000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER', null,
        TO_TIMESTAMP('2010-03-07 16:27:58.073189000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER',
        'sqlplus@psa10766 (TNS V1-V3)', 'oracle', '10.96.137.42', null, null);
INSERT INTO REFERENCE_DOMAINS (DOMAIN, DESCRIPTION, DOMAIN_STATUS, OWNER_CODE, APPLN_CODE, OLD_CODE_TABLE,
                               PARENT_DOMAIN, CODE_LENGTH, CREATE_DATETIME, CREATE_USER_ID, MODIFY_DATETIME,
                               MODIFY_USER_ID, SUPER_SET_DOMAIN, AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME,
                               AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS, AUDIT_CLIENT_WORKSTATION_NAME,
                               AUDIT_ADDITIONAL_INFO)
VALUES ('STAFF_POS', 'Staff Position', 'ACTIVE', 'ADMIN', 'OMS', null, null, null,
        TO_TIMESTAMP('2006-06-28 16:19:39.629806000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER',
        TO_TIMESTAMP('2010-03-07 16:27:58.073344000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER', null,
        TO_TIMESTAMP('2010-03-07 16:27:58.073380000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER',
        'sqlplus@psa10766 (TNS V1-V3)', 'oracle', '10.96.137.42', null, null);
INSERT INTO REFERENCE_DOMAINS (DOMAIN, DESCRIPTION, DOMAIN_STATUS, OWNER_CODE, APPLN_CODE, OLD_CODE_TABLE,
                               PARENT_DOMAIN, CODE_LENGTH, CREATE_DATETIME, CREATE_USER_ID, MODIFY_DATETIME,
                               MODIFY_USER_ID, SUPER_SET_DOMAIN, AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME,
                               AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS, AUDIT_CLIENT_WORKSTATION_NAME,
                               AUDIT_ADDITIONAL_INFO)
VALUES ('STAFF_ROLE', 'Staff Role', 'ACTIVE', 'ADMIN', 'OMS', null, null, null,
        TO_TIMESTAMP('2006-06-28 16:19:39.669086000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER',
        TO_TIMESTAMP('2010-03-07 16:27:58.073532000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER', null,
        TO_TIMESTAMP('2010-03-07 16:27:58.073568000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER',
        'sqlplus@psa10766 (TNS V1-V3)', 'oracle', '10.96.137.42', null, null);
INSERT INTO REFERENCE_DOMAINS (DOMAIN, DESCRIPTION, DOMAIN_STATUS, OWNER_CODE, APPLN_CODE, OLD_CODE_TABLE,
                               PARENT_DOMAIN, CODE_LENGTH, CREATE_DATETIME, CREATE_USER_ID, MODIFY_DATETIME,
                               MODIFY_USER_ID, SUPER_SET_DOMAIN, AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME,
                               AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS, AUDIT_CLIENT_WORKSTATION_NAME,
                               AUDIT_ADDITIONAL_INFO)
VALUES ('STAFF_SEARCH', 'Staff Statuses', 'ACTIVE', 'SYSCON', 'CTAG', null, null, null,
        TO_TIMESTAMP('2006-06-28 16:19:39.704655000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER',
        TO_TIMESTAMP('2010-03-07 16:27:58.073721000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER', null,
        TO_TIMESTAMP('2010-03-07 16:27:58.073757000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER',
        'sqlplus@psa10766 (TNS V1-V3)', 'oracle', '10.96.137.42', null, null);
INSERT INTO REFERENCE_DOMAINS (DOMAIN, DESCRIPTION, DOMAIN_STATUS, OWNER_CODE, APPLN_CODE, OLD_CODE_TABLE,
                               PARENT_DOMAIN, CODE_LENGTH, CREATE_DATETIME, CREATE_USER_ID, MODIFY_DATETIME,
                               MODIFY_USER_ID, SUPER_SET_DOMAIN, AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME,
                               AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS, AUDIT_CLIENT_WORKSTATION_NAME,
                               AUDIT_ADDITIONAL_INFO)
VALUES ('STAFF_STATUS', 'Staff Status', 'ACTIVE', 'ADMIN', 'OMS', null, null, null,
        TO_TIMESTAMP('2006-06-28 16:19:39.819421000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER',
        TO_TIMESTAMP('2010-03-07 16:27:58.074100000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER', null,
        TO_TIMESTAMP('2010-03-07 16:27:58.074136000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER',
        'sqlplus@psa10766 (TNS V1-V3)', 'oracle', '10.96.137.42', null, null);
INSERT INTO REFERENCE_DOMAINS (DOMAIN, DESCRIPTION, DOMAIN_STATUS, OWNER_CODE, APPLN_CODE, OLD_CODE_TABLE,
                               PARENT_DOMAIN, CODE_LENGTH, CREATE_DATETIME, CREATE_USER_ID, MODIFY_DATETIME,
                               MODIFY_USER_ID, SUPER_SET_DOMAIN, AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME,
                               AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS, AUDIT_CLIENT_WORKSTATION_NAME,
                               AUDIT_ADDITIONAL_INFO)
VALUES ('EMAIL_DOMAIN', 'Valid Email Domain addresses', 'ACTIVE', 'ADMIN', 'OMS', null, null, null,
        TO_TIMESTAMP('2018-05-16 16:32:08.018278000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER', null, null, null,
        TO_TIMESTAMP('2018-05-16 16:32:08.018411000', 'YYYY-MM-DD HH24:MI:SS.FF9'), 'OMS_OWNER',
        'TAGUPGRADE_DB_V11.2.1.1.58', 'oracle', '10.101.63.135', null, null);
