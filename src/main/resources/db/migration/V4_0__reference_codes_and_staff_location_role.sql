create table reference_codes
(
    domain      varchar(20) not null,
    code        varchar(10) not null,
    active_flag varchar(1)  not null
);

create table staff_location_roles
(
    cal_agy_loc_id varchar(10) not null,
    sac_staff_id   bigint      not null,
    role           varchar(12) not null,
    from_date      date        not null,
    position       varchar(12) not null,
    schedule_type  varchar(12) not null,
    hours_per_week int         not null,
    to_date        date
);