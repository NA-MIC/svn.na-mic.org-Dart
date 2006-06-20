
-- Add a column to the submission table for the generator

delete from Version;
insert into Version ( Major, Minor, Patch ) Values ( 1, 1, 0 );

alter table Submission add Generator varchar(1024) default null;
