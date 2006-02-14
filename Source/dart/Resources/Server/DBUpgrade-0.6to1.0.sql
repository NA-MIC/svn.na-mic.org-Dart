
-- No Schema changes, just bump to 1.0
delete from Version;
insert into Version ( Major, Minor, Patch ) Values ( 1, 0, 0 );
