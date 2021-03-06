CREATE TABLE PERSONNEL_IDENTIFICATIONS
(
    STAFF_ID              NUMBER(10)                          NOT NULL
      CONSTRAINT PERSONNEL_ID_STAFF
          REFERENCES STAFF_MEMBERS,
    IDENTIFICATION_TYPE   VARCHAR2(12)                        NOT NULL,
    IDENTIFICATION_NUMBER VARCHAR2(32)                        NOT NULL,
    TEXT_FROM             VARCHAR2(40),
    CREATE_DATETIME       TIMESTAMP(9)  DEFAULT systimestamp  NOT NULL,
    CREATE_USER_ID        VARCHAR2(32)  DEFAULT user()          NOT NULL,
    MODIFY_DATETIME       TIMESTAMP(9),
    MODIFY_USER_ID        VARCHAR2(32),
    CONSTRAINT PERSONNEL_IDENTIFICATION_PK
        PRIMARY KEY (STAFF_ID, IDENTIFICATION_TYPE, IDENTIFICATION_NUMBER)
);
