use project7;

create table if not exists User
(
    uuid int not null, 
    name varchar(100) not null, 
    homeaddress varchar(500) not null, 
    phonenumber varchar(20) not null,
    primary key (uuid)
);

create table if not exists Course
(
    courseid int not null, 
    coursetitle varchar(100) not null, 
    primary key (courseid)
);

create table if not exists UserRole
(
    uuid int not null, 
    role varchar(50) not null, 
    foreign key (uuid) references User(uuid),
    primary key (uuid,role)
);

create table if not exists InstructorStatus
(
    instructorid int not null, 
    ishired boolean not null, 
    foreign key (instructorid) references User(uuid),
    primary key (instructorid)
);

create table if not exists EligibleCourse
(
    instructorid int not null, 
    courseid int not null, 
    foreign key (instructorid) references User(uuid),
    foreign key (courseid) references Course(courseid),
    primary key (instructorid,courseid)
);

create table if not exists Prerequisite
(     
    courseid int not null, 
    precourseid int not null, 
    foreign key (courseid) references Course(courseid),
    foreign key (precourseid) references Course(courseid),
    primary key (courseid,precourseid)
);

create table if not exists OfferTerm
(     
    courseid int not null, 
    term varchar(10) not null,
    foreign key (courseid) references Course(courseid),
    primary key (courseid,term)
);

create table if not exists CourseRequest
(
    studentid int not null,
    courseid int not null,
    year int not null,
    term varchar(10) not null,
    status int not null,
    requestdate datetime not null,
    foreign key (studentid) references User(uuid),
    foreign key (courseid) references Course(courseid),
    primary key (studentid,courseid,year,term)
);

create table if not exists WaitList
(
    studentid int not null,
    courseid int not null,
    year int not null,
    term varchar(10) not null,
    status varchar(10) not null,
    insertdate datetime not null,
    foreign key (studentid) references User(uuid),
    foreign key (courseid) references Course(courseid),
    primary key (studentid,courseid,year,term)
);

create table if not exists CourseOffering
(
    courseid int not null,
    instructorid int not null,
    year int not null,
    term varchar(10) not null,
    availableseats int not null,
    foreign key (instructorid) references User(uuid),
    foreign key (courseid) references Course(courseid),
    primary key (courseid,instructorid,year,term)
);

create table if not exists AcademicRecord
(
    studentid int not null,
    courseid int not null,
    year int not null,
    term varchar(10) not null,
    instructorid int null,
    grade varchar(1),
    comment varchar(500),
    foreign key (studentid) references User(uuid),
    foreign key (courseid) references Course(courseid),
    foreign key (instructorid) references User(uuid),
    primary key (studentid, courseid,instructorid,year,term)
);

create table if not exists CurrentTerm
(
	year int not null,
	term varchar(10) not null,
	primary key (year,term)
);	