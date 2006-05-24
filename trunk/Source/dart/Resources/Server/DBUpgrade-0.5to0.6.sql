-- No DB changes, just bump the version number

delete from Version;
insert into Version ( Major, Minor, Patch ) Values ( 0, 6, 0 );
