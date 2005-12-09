-- User information
create table Users (
  UserId ${auto} primary key,
  Email varchar(100) not null unique default '',
  Password varchar(40) not null default '',
  FirstName varchar(40) not null default '',
  LastName varchar(40) not null default '',
  Active int not null default 1
);  

-- Role information. 
-- Encapsulate the project name in the role so a single user can have 
-- different roles for different projects
create table Roles (
  RoleId ${auto} not null primary key,
  Name varchar(100) not null unique -- ProjectName.Administrator, ProjectName.User, ProjectName.Power User, ProjectName.Guest
);

-- User+Role information.
create table UserRoles (
  UserId integer not null,
  RoleId integer not null
);

create index UserRolesIdx1 on UserRoles (UserId);

-- Query information
create table Query (
  QueryId ${auto} not null primary key,
  UserId int not null,
  Name varchar(40) not null,
  Query varchar(2000) not null default ''  
);

insert into Users (Email, Password, FirstName, LastName, Active) values('admin', 'password', 'Dart', 'Administrator', 1);
insert into Users (Email, Password, FirstName, LastName, Active) values('guest', 'guest', 'Guest', 'User', 1);
insert into Users (Email, Password, FirstName, LastName, Active) values('millerjv@research.ge.com', 'jim', 'Jim', 'Miller', 1);
insert into Roles (Name) values('TestServer.Administator');
insert into Roles (Name) values('TestServer.Power User');
insert into Roles (Name) values('TestServer.User');
insert into Roles (Name) values('TestServer.Guest');
insert into Roles (Name) values('Dart.Administrator');
insert into Roles (Name) values('Dart.Power User');

insert into UserRoles values (1, 1); -- admin is adminstrator on TestServer
insert into UserRoles values (1, 2); -- admin is also a power user on TestServer
insert into UserRoles values (1, 3); -- admin is also a user on TestServer
insert into UserRoles values (1, 4); -- admin is also a guest on TestServer

insert into UserRoles values (2, 4); -- guest is just a guest on TestServer

insert into UserRoles values (3, 2); -- jim is a power user on TestServer
insert into UserRoles values (3, 3); -- jim is also a user on TestServer
insert into UserRoles values (3, 4); -- jim is also a guest on TestServer

