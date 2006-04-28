

-- Add index to Test table, helps with ChartServlet query

delete from Version;
insert into Version ( Major, Minor, Patch ) Values ( 1, 2, 0 );

create index TestIdx3 on Test ( QualifiedName, SubmissionId );
