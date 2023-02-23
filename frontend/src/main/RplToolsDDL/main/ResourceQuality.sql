create table ResourceQuality
(
    Quality TEXT not null
);

create unique index ResourceQualit_Quality_uindex
    on ResourceQuality (Quality);

