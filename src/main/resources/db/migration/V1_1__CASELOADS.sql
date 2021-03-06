CREATE TABLE "CASELOADS"
(
  "CASELOAD_ID"                   VARCHAR2(6 CHAR)                  NOT NULL ,
  "DESCRIPTION"                   VARCHAR2(40 CHAR)                 NOT NULL ,
  "CASELOAD_TYPE"                 VARCHAR2(12 CHAR)                 NOT NULL ,
  "LIST_SEQ"                      NUMBER(6, 0),
  "TRUST_ACCOUNTS_FLAG"           VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ,
  "ACCESS_PROPERTY_FLAG"          VARCHAR2(1 CHAR) DEFAULT 'N',
  "TRUST_CASELOAD_ID"             VARCHAR2(6 CHAR),
  "PAYROLL_FLAG"                  VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL ,
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ,
  "DEACTIVATION_DATE"             DATE,
  "COMMISSARY_FLAG"               VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL ,
  "PAYROLL_TRUST_CASELOAD"        VARCHAR2(6 CHAR),
  "COMMISSARY_TRUST_CASELOAD"     VARCHAR2(6 CHAR),
  "TRUST_COMMISSARY_CASELOAD"     VARCHAR2(6 CHAR),
  "COMMUNITY_TRUST_CASELOAD"      VARCHAR2(6 CHAR),
  "MDT_FLAG"                      VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL ,
  "CASELOAD_FUNCTION"             VARCHAR2(12 CHAR)                 NOT NULL ,
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT user()    NOT NULL ,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  CONSTRAINT "CASELOAD_PK" PRIMARY KEY ("CASELOAD_ID")
);

COMMENT ON COLUMN "CASELOADS"."CASELOAD_ID" IS 'An identifing code for a caseload';
COMMENT ON COLUMN "CASELOADS"."DESCRIPTION" IS 'The description of the Caseload ID';
COMMENT ON COLUMN "CASELOADS"."CASELOAD_TYPE" IS 'Refrence Code [ CSLD_TYPE ] : Type of Caseload - ie Institution, Office etc.';
COMMENT ON COLUMN "CASELOADS"."LIST_SEQ" IS 'Controls the order in which caseload information will appear on a list of values.';
COMMENT ON COLUMN "CASELOADS"."TRUST_ACCOUNTS_FLAG" IS 'Indicates this institutional caseload has trust accounting capabilities.';
COMMENT ON COLUMN "CASELOADS"."ACTIVE_FLAG" IS 'Active data indicator';
COMMENT ON COLUMN "CASELOADS"."COMMISSARY_FLAG" IS 'Caseload is a Commissary Caseload.';
COMMENT ON COLUMN "CASELOADS"."PAYROLL_TRUST_CASELOAD" IS 'Central Trust Caseload for Payroll. Multiple Payroll, Single Trust.';
COMMENT ON COLUMN "CASELOADS"."COMMISSARY_TRUST_CASELOAD" IS 'Central Trust Caseload for Commissary. Multiple Commissary, Single Trust.';
COMMENT ON COLUMN "CASELOADS"."TRUST_COMMISSARY_CASELOAD" IS 'Central Commissary Caseload for Trust. Multiple Trust, One Commissary.';
COMMENT ON COLUMN "CASELOADS"."COMMUNITY_TRUST_CASELOAD" IS 'Link between a non-financial community caseload to a financial admin caseload';
COMMENT ON COLUMN "CASELOADS"."MDT_FLAG" IS 'Mandatory Drug Testing Flag';
COMMENT ON COLUMN "CASELOADS"."CREATE_DATETIME" IS 'The timestamp when the record is created';
COMMENT ON COLUMN "CASELOADS"."CREATE_USER_ID" IS 'The user who creates the record';
COMMENT ON COLUMN "CASELOADS"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';
COMMENT ON COLUMN "CASELOADS"."MODIFY_USER_ID" IS 'The user who modifies the record';
COMMENT ON TABLE "CASELOADS" IS 'An administrative business unit (grouping) for management of offender records by establishments (Institution) and/or probation offices/court workers (Community).';


CREATE INDEX "CASELOADS_NI1"
  ON "CASELOADS" ("CASELOAD_TYPE", "CASELOAD_ID");
CREATE INDEX "CASELOAD_FK1"
  ON "CASELOADS" ("COMMISSARY_TRUST_CASELOAD");
CREATE INDEX "CASELOAD_FK2"
  ON "CASELOADS" ("TRUST_COMMISSARY_CASELOAD");
CREATE INDEX "CASELOAD_FK3"
  ON "CASELOADS" ("PAYROLL_TRUST_CASELOAD");
CREATE INDEX "CASELOAD_FK4"
  ON "CASELOADS" ("COMMUNITY_TRUST_CASELOAD");


