

-- Version table
create table version (
    Major int,
    Minor int,
    Patch int,
    TimeStamp timestamp default ${now}
);

delete from version;
insert into version ( Major, Minor, Patch ) Values ( 1, 2, 0 );

create table client (
    ClientId ${auto} primary key,
    Site varchar(64),
    BuildName varchar(64),
    DisplayName varchar(64),
    OS varchar(64),
    OSVersion varchar(64),
    Compiler varchar(64),
    Branch varchar(64),
    Contact varchar(64),
    Comment varchar(64),
    Configuration varchar(64)
);

-- How we select a Client during Submission processing
create index clientidx1 on client ( Site, BuildName );

-- Client properties
create table clientproperty (
  ClientPropertyId ${auto} primary key,
  ClientId bigint,
  Name varchar(1024),
  Value varchar(1024)
);

create table submission (
  SubmissionId ${auto} primary key,
  ClientId bigint,
  TimeStamp timestamp default '1999-09-30 00:00:00',
  Type varchar(64),
  Project varchar(1024),
  Status varchar(64),
  CreatedTimeStamp timestamp default ${now},
  ArchivedTimeStamp timestamp default '1999-09-30 00:00:00',
  ArchiveLevel int default 0,
  TrackId bigint,
  NextSubmissionId bigint,
  LastSubmissionId bigint,
  Generator varchar(1024) default null
);

create index submissionidx1 on submission ( ClientId, Timestamp, Type );
create index submissionidx2 on submission ( TrackId );
create index submissionidx3 on submission ( CreatedTimeStamp, TimeStamp, ArchiveLevel );

create table test ( 
  TestId ${auto} primary key,
  ParentTestId bigint,
  SubmissionId bigint,
  QualifiedName varchar(2048),  -- .Test.itk.Common.PrintSelf
  Name varchar(128),  -- PrintSelf
  Status char,         -- 'p', 'f', 'n' for Passed, Failed, NotRun
  PassedSubTests int not null default 0,
  FailedSubTests int not null default 0,
  NotRunSubTests int not null default 0
);

-- This a common query for processing tests
create index testidx1 on test ( SubmissionId, QualifiedName${indexsize} );
create index testidx2 on test ( ParentTestId );
create index testidx3 on test ( QualifiedName${indexsize}, SubmissionId );

create table relatedtest (
  TestId bigint primary key not null,
  Relation varchar(32) not null,
  RelatedTestId bigint not null
);

create index relatedtestidx1 on relatedtest ( TestId, Relation );

create table result (
  ResultId ${auto} primary key,
  TestId bigint not null,
  Name varchar(64),
  Type varchar(32),  -- numeric/double, image/png, image/jpeg, text/xml, text/html, text/text, text/string
  Value varchar(2000)
);

-- Used to report results
create index resultidx1 on result ( TestId, Name );

-- Used to report results
create index resultidx2 on result ( Value${indexsize} );

-- Track table
create table track (
  TrackId ${auto} primary key,
  Name varchar(32),
  StartTime timestamp default '1999-09-30 00:00:00',
  EndTime timestamp default '1999-09-30 00:00:00',
  NextTrackId bigint,
  LastTrackId bigint
);

-- Speed up Next/Last queries
create index trackidx1 on track ( Name, StartTime );
create index trackidx2 on track ( Name, EndTime );
create index trackidx3 on track ( Name, EndTime, StartTime );

create table taskqueue (
  TaskId ${auto} primary key,
  Priority int,
  QueuedTime timestamp default ${now},
  Type varchar(2000),
  Properties varchar(2000)
);

-- Usual task query
create index taskqueueidx1 on taskqueue ( Priority, TaskId );

create table completedtask (
  TaskId bigint not null primary key,
  Status varchar(10),
  ProcessedTime timestamp default ${now},
  Priority int,
  Type varchar(2000),
  Properties varchar(2000),
  Result varchar(2000)
);


-- Notes
create table note (
  NoteId ${auto} not null primary key,
  Reference varchar(64) not null,       -- Client, Submission, Test, Result
  ReferenceId bigint,                      -- ClientId, SubmissionId, etc.
  Type varchar(32),                     -- text/html, text/text, text/string
  Value varchar(2000)                   -- Some of our notes are very long, varchar(2000) may not be large enough.  May have to move some notes to results?
);


