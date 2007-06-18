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


-- Derby call to reclaim disk space, note 'APP' is the default schema name
-- call SYSCS_UTIL.SYSCS_INPLACE_COMPRESS_TABLE('APP', 'RESULT', 1, 1, 1);
-- call SYSCS_UTIL.SYSCS_INPLACE_COMPRESS_TABLE('APP', 'TEST', 1, 1, 1);
-- call SYSCS_UTIL.SYSCS_INPLACE_COMPRESS_TABLE('APP', 'SUBMISSION', 1, 1, 1);

-- Backup the Derby database
-- CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE('c:/backupdir');

-- Backup without waiting for in progress transactions
-- CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE_NOWAIT('c:/backupdir');


-- Derby backup, and enable logging for full recovery
-- CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE_AND_ENABLE_LOG_ARCHIVE_MODE('c:/backupdir', 1 );
