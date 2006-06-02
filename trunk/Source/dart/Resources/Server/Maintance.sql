-- These statements are run nightly for db maintance

-- How we select a Client during Submission processing
drop index  ClientIdx1;
create index ClientIdx1 on Client ( Site, BuildName );


drop index  SubmissionIdx1;
create index SubmissionIdx1 on Submission ( ClientId, Timestamp, Type );
drop index  SubmissionIdx2;
create index SubmissionIdx2 on Submission ( TrackId );


-- This a common query for processing tests
drop index  TestIdx1;
create index TestIdx1 on Test ( SubmissionId, QualifiedName );
drop index TestIdx2;
create index TestIdx2 on Test ( ParentTestId );

-- Used to report results
drop index  ResultIdx1;
create index ResultIdx1 on Result ( TestId, Name );


-- Usual task query
drop index TaskQueueIdx1;
create index TaskQueueIdx1 on TaskQueue ( Priority, TaskId );
