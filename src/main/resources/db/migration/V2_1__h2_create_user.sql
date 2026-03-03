CREATE SCHEMA IF NOT EXISTS oms_utils;

CREATE ALIAS IF NOT EXISTS oms_utils.record_logon_date
    FOR 'uk.gov.justice.digital.hmpps.nomisuserrolesapi.db.H2OmsUtils.recordLogonDate';

CREATE ALIAS IF NOT EXISTS oms_utils.create_user
    FOR 'uk.gov.justice.digital.hmpps.nomisuserrolesapi.db.H2OmsUtils.createUser';

CREATE ALIAS IF NOT EXISTS oms_utils.drop_user
    FOR 'uk.gov.justice.digital.hmpps.nomisuserrolesapi.db.H2OmsUtils.dropUser';

CREATE ALIAS IF NOT EXISTS oms_utils.expire_password
    FOR 'uk.gov.justice.digital.hmpps.nomisuserrolesapi.db.H2OmsUtils.expirePassword';

CREATE ALIAS IF NOT EXISTS oms_utils.lock_user
    FOR 'uk.gov.justice.digital.hmpps.nomisuserrolesapi.db.H2OmsUtils.lockUser';

CREATE ALIAS IF NOT EXISTS oms_utils.unlock_user
    FOR 'uk.gov.justice.digital.hmpps.nomisuserrolesapi.db.H2OmsUtils.unlockUser';

CREATE ALIAS IF NOT EXISTS oms_utils.change_user_password
    FOR 'uk.gov.justice.digital.hmpps.nomisuserrolesapi.db.H2OmsUtils.changePassword';
