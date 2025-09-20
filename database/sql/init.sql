create sequence scan_seq start with 1 increment by 50;
create table scan
(status smallint not null, id bigint not null, user_id varchar(100) not null, url varchar(256) not null, created_at timestamp(6) with time zone, updated_at timestamp(6) with time zone, primary key (id));
create index IDXdjvrc9ovp4smei194tw4xo1t0 on scan (user_id, created_at);
create index IDX501ofq0b7ln4x1y9dalhd196g on scan (user_id, status, created_at);
