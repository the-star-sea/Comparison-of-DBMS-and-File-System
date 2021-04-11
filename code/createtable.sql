create table if not exists academy(
    academy varchar(20) not null primary key ,
    english_name varchar(20) not null unique
);
create table if not exists student(
    student_id integer primary key ,
    name varchar(20) not null ,
    gender char ,
    academy varchar(20) constraint w references academy(academy)
);
create table if not exists department(
    name varchar(20) not null primary key
);
create table if not exists course(
    id varchar(20) not null primary key ,
    name varchar(20)  ,
    credit float,
    course_hour int,
    department varchar(20) constraint kk references department(name)

);

create table if not exists teacher(
     mail varchar(40)primary key ,
    name varchar(20) not null  ,
    department varchar(20) constraint kk references department(name),
unique (name,department)
);
create table if not exists class(
    id integer not null  primary key,
    totalCapacity int,
    course_id varchar(20)  constraint dd references course(id),
    class_name varchar(20),
    requirement varchar(60),
     unique(course_id,class_name)

);
create table if not exists students_class(
    student_id integer not null constraint wk references student(student_id),
class_id integer not null constraint ww references class(id),
 primary key (student_id,class_id)

);
create table if not exists class_teacher(
    teacher_mail varchar(40) constraint ee references teacher(mail),
    class_id int constraint eep references class(id),
    primary key (teacher_mail,class_id)

);
create table if not exists prerequisite(
    course_id varchar(20)  constraint dd references course(id),
    prerequisite_id varchar(20)  constraint ii references course(id),
    kind int not null  ,
    primary key (course_id,prerequisite_id,kind)
);
create table if not exists class_detail(
    class_id integer not null constraint oo references class(id),
   location varchar(20),
    weekday varchar(20),
    class_time varchar(20),
    weeklist int[],
     primary key (class_id,location,weekday,class_time,weeklist)
);
