-- apply changes
create table chest (
  id                            integer not null,
  world_id                      integer,
  x                             integer not null,
  y                             integer not null,
  z                             integer not null,
  looted_at                     timestamp,
  restocked_at                  timestamp,
  time_passed                   double not null,
  status                        varchar(13) not null,
  loot_table                    varchar(255) not null,
  group_uuid                    varchar(40),
  constraint ck_chest_status check ( status in ('RESTOCKED','LOOTED','NEVER_TOUCHED')),
  constraint location unique (world_id,x,y,z),
  constraint pk_chest primary key (id),
  foreign key (world_id) references world (id) on delete restrict on update restrict,
  foreign key (group_uuid) references chest_group (uuid) on delete restrict on update restrict
);

create table chest_group (
  uuid                          varchar(40) not null,
  name                          varchar(255),
  config                        clob not null,
  looted_at                     timestamp,
  restocked_at                  timestamp,
  time_passed                   double not null,
  status                        varchar(13),
  constraint ck_chest_group_status check ( status in ('RESTOCKED','LOOTED','NEVER_TOUCHED')),
  constraint pk_chest_group primary key (uuid)
);

create table world (
  id                            integer not null,
  world_uuid                    varchar(40),
  constraint pk_world primary key (id)
);

