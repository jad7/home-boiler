
create table if not exists current (
    name VARCHAR(10),
    value FLOAT,
    created timestamp default now(),
    INDEX (created)
);

create table if not exists history (
    name    VARCHAR(10),
    value   FLOAT,
    created timestamp,
    INDEX (created)
);
