create schema if not exists SYS;

create table SYS.USER$
(
    NAME   VARCHAR2(30)  not null,
    SPARE4 VARCHAR2(255) not null,
    primary key (NAME)
);
