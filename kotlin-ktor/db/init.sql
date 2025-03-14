CREATE TABLE IF NOT EXISTS meetings (
    "id" SERIAL PRIMARY KEY,
    "title" VARCHAR(255) NOT NULL,
    "date" DATE NOT NULL,
    "start" TIME NOT NULL,
    "end" TIME NOT NULL
);

insert into meetings(title, date, start, "end") values ('Lunch phone call', now(), '12:00', '12:45');
insert into meetings(title, date, start, "end") values ('Extended team standup', now(), '09:10', '10:45');
insert into meetings(title, date, start, "end") values ('Design review', now(), '15:00', '15:30');
