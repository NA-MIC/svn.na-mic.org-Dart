-- Version table
create table Version (
    Major int,
    Minor int,
    Patch int,
    TimeStamp timestamp default ${now}
);

delete from Version;
insert into Version ( Major, Minor, Patch ) Values ( 0, 7, 0 );

-- User information
create table Users (
  UserId ${auto} primary key,
  Email varchar(100) not null unique default '',
  Password varchar(40) not null default '',
  FirstName varchar(40) not null default '',
  LastName varchar(40) not null default '',
  Active int not null default 1
);  

-- User properties
create table UserProperty (
  UserPropertyId ${auto} primary key,
  UserId int,
  Name varchar(100),
  Value varchar(100)
);

-- Role information. 
-- Encapsulate the project name in the role so a single user can have 
-- different roles for different projects
create table Role (
  RoleId ${auto} not null primary key,
  Name varchar(100) not null -- ProjectName.Administrator, ProjectName.User, ProjectName.Power User, ProjectName.Guest
);

-- User+Role information.
create table UserRole (
  UserId integer not null,
  RoleId integer not null
);

create index UserRoleIdx1 on UserRole (UserId);

-- Query information
create table Query (
  QueryId ${auto} not null primary key,
  UserId int not null,
  Name varchar(40) not null,
  Query varchar(2000) not null default ''  
);

insert into Users (Email, Password, FirstName, LastName, Active) values('admin', 'password', 'Dart', 'Administrator', 1);
insert into Users (Email, Password, FirstName, LastName, Active) values('guest', 'guest', 'Guest', 'User', 1);
insert into Role (Name) values('Dart.Administrator');
insert into Role (Name) values('Dart.Power User');
-- insert into Role (Name) values('TestServer.Administator');
-- insert into Role (Name) values('TestServer.Power User');
-- insert into Role (Name) values('TestServer.User');
-- insert into Role (Name) values('TestServer.Guest');

insert into UserRole (UserId, RoleId) values (1, 1); -- admin is adminstrator on TestServer
-- insert into UserRole (UserId, RoleId)  values (1, 2); -- admin is also a power user on TestServer
-- insert into UserRole (UserId, RoleId)  values (1, 3); -- admin is also a user on TestServer
-- insert into UserRole (UserId, RoleId)  values (1, 4); -- admin is also a guest on TestServer
-- insert into UserRole (UserId, RoleId)  values (2, 4); -- guest is just a guest on TestServer
