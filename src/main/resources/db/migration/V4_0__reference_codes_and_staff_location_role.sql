create table reference_codes
(
    domain                        varchar2(12 char)                      not null,
    code                          varchar2(12 char)                      not null,
    description                   varchar2(40 char)                      not null,
    list_seq                      number(6),
    active_flag                   varchar2(1 char)  default 'y'          not null,
    system_data_flag              varchar2(1 char)  default 'y'          not null,
    modify_user_id                varchar2(32 char),
    expired_date                  date,
    new_code                      varchar2(12 char),
    parent_code                   varchar2(12 char),
    parent_domain                 varchar2(12 char),
    create_datetime               timestamp(9) default systimestamp not null,
    create_user_id                varchar2(32 char) default user         not null,
    modify_datetime               timestamp(9),
    audit_timestamp               timestamp(9),
    audit_user_id                 varchar2(32 char),
    audit_module_name             varchar2(65 char),
    audit_client_user_id          varchar2(64 char),
    audit_client_ip_address       varchar2(39 char),
    audit_client_workstation_name varchar2(64 char),
    audit_additional_info         varchar2(256 char),
    constraint reference_codes_pk
        primary key (domain, code)
);

create table staff_location_roles
(
    cal_agy_loc_id                varchar2(6 char)                       not null,
    sac_staff_id                  number(10)                             not null
        constraint staff_lr_staff_fk1
            references staff_members,
    from_date                     date                              not null,
    to_date                       date,
    position                      varchar2(12 char)                      not null,
    role                          varchar2(12 char)                      not null,
    schedule_type                 varchar2(12 char),
    hours_per_week                number(4, 2),
    supervisor_agy_loc_id         varchar2(6 char),
    supervisor_staff_id           number(10),
    supervisor_from_date          date,
    supervisor_position           varchar2(12 char),
    supervisor_role               varchar2(12 char),
    staff_unit                    varchar2(12 char),
    create_datetime               timestamp(9) default systimestamp not null,
    create_user_id                varchar2(32 char) default user         not null,
    modify_datetime               timestamp(9),
    modify_user_id                varchar2(32 char),
    audit_timestamp               timestamp(9),
    audit_user_id                 varchar2(32 char),
    audit_module_name             varchar2(65 char),
    audit_client_user_id          varchar2(64 char),
    audit_client_ip_address       varchar2(39 char),
    audit_client_workstation_name varchar2(64 char),
    audit_additional_info         varchar2(256 char),
    constraint staff_location_roles_pk
        primary key (sac_staff_id, cal_agy_loc_id, from_date, position, role),
    constraint staff_lr_staff_lr_fk1
        foreign key (supervisor_staff_id, supervisor_agy_loc_id, supervisor_from_date, supervisor_position,
                     supervisor_role) references staff_location_roles,
    constraint chk_dates
        check (from_date <= nvl(to_date, from_date))
)