create table broken_mods
(
    id                 serial primary key,
    mod_id             varchar(20) not null,
    minecraft_version  varchar(20) not null,
    affected_versions  varchar(40) not null,
    reason             text        not null,
    fixed_version      varchar(20) not null,
    fixed_download_url text        not null
);