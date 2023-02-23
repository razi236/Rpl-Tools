create table Resources
(
    ResourceID         INTEGER not null
        constraint Resources_pk
            primary key autoincrement,
    ResourceCategory   TEXT,
    ResourceEfficiency INT
);

create unique index Resources_ResourceID_uindex
    on Resources (ResourceID);

