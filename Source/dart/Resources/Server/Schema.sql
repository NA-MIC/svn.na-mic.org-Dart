

-- Version table
create table Version (
    Major int,
    Minor int,
    Patch int,
    TimeStamp timestamp default ${now}
);

delete from Version;
insert into Version ( Major, Minor, Patch ) Values ( 1, 1, 0 );

create table Client (
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
create index ClientIdx1 on Client ( Site, BuildName );

-- Client properties
create table ClientProperty (
  ClientPropertyId ${auto} primary key,
  ClientId bigint,
  Name varchar(1024),
  Value varchar(1024)
);

create table Submission (
  SubmissionId ${auto} primary key,
  ClientId bigint,
  TimeStamp timestamp,
  Type varchar(64),
  Project varchar(1024),
  Status varchar(64),
  CreatedTimeStamp timestamp default ${now},
  ArchivedTimeStamp timestamp default null,
  ArchiveLevel int default 0,
  TrackId bigint,
  NextSubmissionId bigint,
  LastSubmissionId bigint,
  Generator varchar(1024) default null
);

create index SubmissionIdx1 on Submission ( ClientId, Timestamp, Type );
create index SubmissionIdx2 on Submission ( TrackId );
create index SubmissionIdx3 on Submission ( CreatedTimeStamp, TimeStamp, ArchiveLevel );

create table Test ( 
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
create index TestIdx1 on Test ( SubmissionId, QualifiedName );
create index TestIdx2 on Test ( ParentTestId );

create table RelatedTest (
  TestId bigint primary key not null,
  Relation varchar(32) not null,
  RelatedTestId bigint not null
);

create index RelatedTestIdx1 on RelatedTest ( TestId, Relation );

create table Result (
  ResultId ${auto} primary key,
  TestId bigint not null,
  Name varchar(64),
  Type varchar(32),  -- numeric/double, image/png, image/jpeg, text/xml, text/html, text/text, text/string
  Value varchar(2000)
);

-- Used to report results
create index ResultIdx1 on Result ( TestId, Name );

-- Used to report results
create index ResultIdx2 on Result ( Value );

-- Track table
create table Track (
  TrackId ${auto} primary key,
  Name varchar(32),
  StartTime timestamp,
  EndTime timestamp,
  NextTrackId bigint,
  LastTrackId bigint
);

-- Speed up Next/Last queries
create index TrackIdx1 on Track ( Name, StartTime );
create index TrackIdx2 on Track ( Name, EndTime );
create index TrackIdx3 on Track ( Name, EndTime, StartTime );

create table TaskQueue (
  TaskId ${auto} primary key,
  Priority int,
  QueuedTime timestamp default ${now},
  Type varchar(2000),
  Properties varchar(2000)
);

-- Usual task query
create index TaskQueueIdx1 on TaskQueue ( Priority, TaskId );

create table CompletedTask (
  TaskId bigint not null primary key,
  Status varchar(10),
  ProcessedTime timestamp default ${now},
  Priority int,
  Type varchar(2000),
  Properties varchar(2000),
  Result varchar(2000)
);


-- Notes
create table Note (
  NoteId ${auto} not null primary key,
  Reference varchar(64) not null,       -- Client, Submission, Test, Result
  ReferenceId bigint,                      -- ClientId, SubmissionId, etc.
  Type varchar(32),                     -- text/html, text/text, text/string
  Value varchar(2000)                   -- Some of our notes are very long, varchar(2000) may not be large enough.  May have to move some notes to results?
);


