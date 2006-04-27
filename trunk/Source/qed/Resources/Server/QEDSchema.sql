
create table Population (
    PopulationId ${auto} primary key,
    Name varchar(64)
);

create table PopulationAttribute (
    PopulationId integer not null,
    Name varchar(64),
    Value varchar(1024)
);

create table SubjectMap (
    PopulationId int not null,
    SubjectId int not null
);

create table Subject (
    SubjectId ${auto} primary key,
    Name varchar(64),
    Id varchar(64)
);

create table SubjectAttribute (
    SubjectId integer not null,
    Name varchar(64),
    Value varchar(1024)
);

create table Measurement (
  MeasurementId ${auto} primary key,
  SubjectId int not null,
  Name varchar(256),
  Type varchar(32),  -- numeric/double, image/png, image/jpeg, text/xml, text/html, text/text, text/string
  Value varchar(2000)
);

create table Experiment (
  ExperimentId ${auto} primary key,
  Name varchar(128),
  TimeStamp timestamp default ${now}
);

create table Run (
  RunId ${auto} primary key,
  ExperimentId int not null,
  Name varchar(128),
  TimeStamp timestamp default ${now}
);

create table MeasurementMap (
  RunId int not null,
  MeasurementId int not null 
);

create table RunAttribute (
  RunId int not null,
  Name varchar(64),
  Value varchar(1024)
);


