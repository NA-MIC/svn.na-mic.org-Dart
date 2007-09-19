
delete from Version;
insert into Version ( Major, Minor, Patch ) Values ( 1, 3, 0 );


-- Add some indicies
create index testidx4 on test ( SubmissionId );
create index resultidx3 on result ( TestId );
