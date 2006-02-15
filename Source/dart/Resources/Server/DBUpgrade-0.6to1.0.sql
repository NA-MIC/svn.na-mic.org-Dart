
-- Add a ClientProperties table
-- Drop the Designation table

delete from Version;
insert into Version ( Major, Minor, Patch ) Values ( 1, 0, 0 );

create table ClientProperty (
  ClientPropertyId bigint generated always as identity primary key,
  ClientId bigint,
  Name varchar(1024),
  Value varchar(1024)
);

drop table Designation