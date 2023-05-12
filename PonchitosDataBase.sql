

create table city
(
    city_name varchar(50) not null,
    country   varchar(50) not null,
    primary key (city_name, country)
);

create table circuit
(
    circuit_id        varchar(50) not null
        primary key,
    description       varchar(50) null,
    trip_duration     int         null,
    cost              int         null,
    departing_city    varchar(50) null,
    arrival_city      varchar(50) null,
    departing_country varchar(50) null,
    arrival_country   varchar(50) null,
    constraint circuit_ibfk_1
        foreign key (departing_city, departing_country) references city (city_name, country),
    constraint circuit_ibfk_2
        foreign key (arrival_city, arrival_country) references city (city_name, country)
);

create index arrival_city
    on circuit (arrival_city, arrival_country);

create index departing_city
    on circuit (departing_city, departing_country);

create table client
(
    client_id         int auto_increment
        primary key,
    client_type       enum ('Company', 'Group', 'Individual')                   null,
    registration_year date                                                      null,
    agency            tinyint(1)                                                null,
    address           varchar(50)                                               null,
    payment_method    enum ('Apple Pay', 'PayPal', 'Credit Card', 'Debit Card') null,
    client_name       varchar(50)                                               null
);

create table clientauth
(
    client_id     int          not null
        primary key,
    username      varchar(100) null,
    password_hash varchar(255) null,
    salt          varchar(255) null,
    constraint username
        unique (username),
    constraint clientauth_ibfk_1
        foreign key (client_id) references client (client_id)
);

create table datecircuit
(
    circuit_id     varchar(50) not null,
    departure_date date        not null,
    arrival_date   date        not null,
    num_people     int         null,
    primary key (circuit_id, departure_date),
    constraint datecircuit_ibfk_1
        foreign key (circuit_id) references circuit (circuit_id)
);

create index circuit_id
    on datecircuit (circuit_id);

create table hotel
(
    hotel_name     varchar(50) not null
        primary key,
    address        varchar(50) null,
    num_rooms      int         null,
    room_cost      int         null,
    breakfast_cost int         null,
    city_name      varchar(50) null,
    country        varchar(50) null,
    constraint address
        unique (address),
    constraint hotel_ibfk_1
        foreign key (city_name, country) references city (city_name, country)
);

create index city_name
    on hotel (city_name, country);

create table placetovisit
(
    place_name  varchar(50) not null,
    address     varchar(50) not null,
    description varchar(50) null,
    cost        int         null,
    city_name   varchar(50) null,
    country     varchar(50) null,
    primary key (place_name, address),
    constraint placetovisit_ibfk_1
        foreign key (city_name, country) references city (city_name, country)
);

create index city_name
    on placetovisit (city_name, country);

create table potentialclient
(
    client_name varchar(50) not null
        primary key
);

create table simulation
(
    simulation_id         int auto_increment
        primary key,
    client_name           varchar(50) null,
    client_id             int         null,
    departure_date        date        null,
    arrival_date          date        null,
    num_people            int         null,
    cost                  int         null,
    simulation_created_at date        null,
    is_reservation        tinyint(1)  null,
    constraint simulation_ibfk_1
        foreign key (client_name) references potentialclient (client_name),
    constraint simulation_ibfk_2
        foreign key (client_id) references client (client_id)
);

create table reservation
(
    reservation_id         int auto_increment
        primary key,
    simulation_id          int  null,
    client_id              int  null,
    reservation_created_at date null,
    constraint reservation_ibfk_1
        foreign key (simulation_id) references simulation (simulation_id),
    constraint reservation_ibfk_2
        foreign key (client_id) references client (client_id)
);

create index client_id
    on reservation (client_id);

create index simulation_id
    on reservation (simulation_id);

create table reservationcircuit
(
    reservation_id int         not null,
    circuit_id     varchar(50) not null,
    primary key (reservation_id, circuit_id),
    constraint reservationcircuit_ibfk_1
        foreign key (reservation_id) references reservation (reservation_id),
    constraint reservationcircuit_ibfk_2
        foreign key (circuit_id) references circuit (circuit_id)
);

create index circuit_id
    on reservationcircuit (circuit_id);

create table reservationhotel
(
    reservation_id int         not null,
    hotel_name     varchar(50) not null,
    departure_date date        null,
    arrival_date   date        null,
    primary key (reservation_id, hotel_name),
    constraint reservationhotel_ibfk_1
        foreign key (reservation_id) references reservation (reservation_id),
    constraint reservationhotel_ibfk_2
        foreign key (hotel_name) references hotel (hotel_name)
);

create index hotel_name
    on reservationhotel (hotel_name);

create index client_id
    on simulation (client_id);

create index client_name
    on simulation (client_name);

create table simulationcircuit
(
    simulation_id int         not null,
    circuit_id    varchar(50) not null,
    primary key (simulation_id, circuit_id),
    constraint simulationcircuit_ibfk_1
        foreign key (simulation_id) references simulation (simulation_id),
    constraint simulationcircuit_ibfk_2
        foreign key (circuit_id) references circuit (circuit_id)
);

create index circuit_id
    on simulationcircuit (circuit_id);

create table simulationhotel
(
    simulation_id  int         not null,
    hotel_name     varchar(50) not null,
    departure_date date        null,
    arrival_date   date        null,
    primary key (simulation_id, hotel_name),
    constraint simulationhotel_ibfk_1
        foreign key (simulation_id) references simulation (simulation_id),
    constraint simulationhotel_ibfk_2
        foreign key (hotel_name) references hotel (hotel_name)
);

create index hotel_name
    on simulationhotel (hotel_name);

create table stage
(
    stage_order    int         not null,
    order_duration int         null,
    circuit_id     varchar(50) not null,
    place_name     varchar(50) null,
    address        varchar(50) null,
    primary key (stage_order, circuit_id),
    constraint stage_ibfk_1
        foreign key (place_name, address) references placetovisit (place_name, address),
    constraint stage_ibfk_2
        foreign key (circuit_id) references circuit (circuit_id)
);

create index circuit_id
    on stage (circuit_id);

create index place_name
    on stage (place_name, address);

INSERT INTO city (city_name, country)
VALUES ('CDMX', 'Mexico');
INSERT INTO city (city_name, country)
VALUES ('Chicago', 'USA');
INSERT INTO city (city_name, country)
VALUES ('London', 'UK');
INSERT INTO city (city_name, country)
VALUES ('Los Angeles', 'USA');
INSERT INTO city (city_name, country)
VALUES ('New York', 'USA');
INSERT INTO city (city_name, country)
VALUES ('Paris', 'France');
INSERT INTO city (city_name, country)
VALUES ('Puebla', 'Mexico');
INSERT INTO city (city_name, country)
VALUES ('Tokyo', 'Japan');
INSERT INTO city (city_name, country)
VALUES ('Veracruz', 'Mexico');
INSERT INTO circuit (circuit_id, description, trip_duration, cost, departing_city, arrival_city,
                                           departing_country, arrival_country)
VALUES ('C4', 'World Tour', 30, 5000, 'New York', 'Tokyo', 'USA', 'Japan');
INSERT INTO circuit (circuit_id, description, trip_duration, cost, departing_city, arrival_city,
                                           departing_country, arrival_country)
VALUES ('C8', 'Mexico Tour P3', 15, 2000, 'CDMX', 'CDMX', 'Mexico', 'Mexico');
INSERT INTO circuit (circuit_id, description, trip_duration, cost, departing_city, arrival_city,
                                           departing_country, arrival_country)
VALUES ('C2', 'Europe Tour', 12, 1800, 'Paris', 'London', 'France', 'UK');
INSERT INTO circuit (circuit_id, description, trip_duration, cost, departing_city, arrival_city,
                                           departing_country, arrival_country)
VALUES ('C1', 'USA Tour', 10, 1500, 'New York', 'Los Angeles', 'USA', 'USA');
INSERT INTO circuit (circuit_id, description, trip_duration, cost, departing_city, arrival_city,
                                           departing_country, arrival_country)
VALUES ('C3', 'Asia Tour', 8, 1200, 'Tokyo', 'Tokyo', 'Japan', 'Japan');
INSERT INTO circuit (circuit_id, description, trip_duration, cost, departing_city, arrival_city,
                                           departing_country, arrival_country)
VALUES ('C5', 'City Hopping', 5, 800, 'London', 'Paris', 'UK', 'France');
INSERT INTO circuit (circuit_id, description, trip_duration, cost, departing_city, arrival_city,
                                           departing_country, arrival_country)
VALUES ('C6', 'Mexico Tour P1', 5, 600, 'CDMX', 'Puebla', 'Mexico', 'Mexico');
INSERT INTO circuit (circuit_id, description, trip_duration, cost, departing_city, arrival_city,
                                           departing_country, arrival_country)
VALUES ('C7', 'Mexico Tour P2', 5, 500, 'Puebla', 'Veracruz', 'Mexico', 'Mexico');
INSERT INTO client (client_id, client_type, registration_year, agency, address, payment_method,
                                          client_name)
VALUES (1, 'Individual', '2022-01-01', 1, '111 Alice Street', 'Credit Card', 'Alice');
INSERT INTO client (client_id, client_type, registration_year, agency, address, payment_method,
                                          client_name)
VALUES (2, 'Individual', '2021-06-01', 0, '222 Bob Street', 'PayPal', 'Bob');
INSERT INTO client (client_id, client_type, registration_year, agency, address, payment_method,
                                          client_name)
VALUES (3, 'Group', '2023-05-11', 1, 'Address1', 'Apple Pay', 'root');
INSERT INTO clientauth (client_id, username, password_hash, salt)
VALUES (3, 'root', 'CWsCo354KdE36HWvFYX8uzCFKrQfMnMxm8otHMDYA2I=', 'wG70/lUl3C/uEmrBNYAiiw==');
INSERT INTO datecircuit (circuit_id, departure_date, arrival_date, num_people)
VALUES ('C1', '2023-05-10', '2023-09-10', 10);
INSERT INTO datecircuit (circuit_id, departure_date, arrival_date, num_people)
VALUES ('C1', '2023-06-01', '2023-09-10', 10);
INSERT INTO datecircuit (circuit_id, departure_date, arrival_date, num_people)
VALUES ('C2', '2023-06-15', '2023-09-10', 8);
INSERT INTO datecircuit (circuit_id, departure_date, arrival_date, num_people)
VALUES ('C3', '2023-07-01', '2023-09-10', 12);
INSERT INTO datecircuit (circuit_id, departure_date, arrival_date, num_people)
VALUES ('C4', '2023-07-10', '2023-09-10', 5);
INSERT INTO datecircuit (circuit_id, departure_date, arrival_date, num_people)
VALUES ('C5', '2023-08-01', '2023-09-10', 15);
INSERT INTO datecircuit (circuit_id, departure_date, arrival_date, num_people)
VALUES ('C6', '2023-08-15', '2023-09-10', 10);
INSERT INTO datecircuit (circuit_id, departure_date, arrival_date, num_people)
VALUES ('C7', '2023-08-20', '2023-09-10', 10);
INSERT INTO datecircuit (circuit_id, departure_date, arrival_date, num_people)
VALUES ('C8', '2023-09-01', '2023-09-10', 15);
INSERT INTO hotel (hotel_name, address, num_rooms, room_cost, breakfast_cost, city_name, country)
VALUES ('Fiesta Americana CDMX', 'Calle CDMX 234', 40, 75, 8, 'CDMX', 'Mexico');
INSERT INTO hotel (hotel_name, address, num_rooms, room_cost, breakfast_cost, city_name, country)
VALUES ('Fiesta Inn CDMX', 'Calle CDMX 123', 50, 80, 8, 'CDMX', 'Mexico');
INSERT INTO hotel (hotel_name, address, num_rooms, room_cost, breakfast_cost, city_name, country)
VALUES ('Fiesta Inn Puebla', 'Calle Puebla 123', 50, 80, 8, 'Puebla', 'Mexico');
INSERT INTO hotel (hotel_name, address, num_rooms, room_cost, breakfast_cost, city_name, country)
VALUES ('Fiesta Inn Veracruz', 'Calle Veracruz 123', 50, 80, 8, 'Veracruz', 'Mexico');
INSERT INTO hotel (hotel_name, address, num_rooms, room_cost, breakfast_cost, city_name, country)
VALUES ('LA Hotel', '456 LA Street', 150, 110, 12, 'Los Angeles', 'USA');
INSERT INTO hotel (hotel_name, address, num_rooms, room_cost, breakfast_cost, city_name, country)
VALUES ('London Hotel', '321 London Street', 120, 130, 18, 'London', 'UK');
INSERT INTO hotel (hotel_name, address, num_rooms, room_cost, breakfast_cost, city_name, country)
VALUES ('NY Hotel', '123 NY Street', 100, 120, 15, 'New York', 'USA');
INSERT INTO hotel (hotel_name, address, num_rooms, room_cost, breakfast_cost, city_name, country)
VALUES ('Paris Hotel', '789 Paris Street', 80, 140, 20, 'Paris', 'France');
INSERT INTO hotel (hotel_name, address, num_rooms, room_cost, breakfast_cost, city_name, country)
VALUES ('Tokyo Hotel', '654 Tokyo Street', 90, 100, 10, 'Tokyo', 'Japan');
INSERT INTO placetovisit (place_name, address, description, cost, city_name, country)
VALUES ('Big Ben', 'Westminster', 'Famous clock tower', 15, 'London', 'UK');
INSERT INTO placetovisit (place_name, address, description, cost, city_name, country)
VALUES ('Eiffel Tower', 'Champ de Mars', 'Iconic tower in Paris', 20, 'Paris', 'France');
INSERT INTO placetovisit (place_name, address, description, cost, city_name, country)
VALUES ('Hollywood Sign', 'Mount Lee', 'Famous sign in LA', 10, 'Los Angeles', 'USA');
INSERT INTO placetovisit (place_name, address, description, cost, city_name, country)
VALUES ('Statue of Liberty', '1 Liberty Island', 'Famous statue in NY', 25, 'New York', 'USA');
INSERT INTO placetovisit (place_name, address, description, cost, city_name, country)
VALUES ('Tokyo Tower', '4 Chome-2-8 Shibakoen', 'Iconic tower in Tokyo', 18, 'Tokyo', 'Japan');
INSERT INTO potentialclient (client_name)
VALUES ('Alice');
INSERT INTO potentialclient (client_name)
VALUES ('Alma');
INSERT INTO potentialclient (client_name)
VALUES ('Bob');
INSERT INTO potentialclient (client_name)
VALUES ('Charlie');
INSERT INTO potentialclient (client_name)
VALUES ('David');
INSERT INTO potentialclient (client_name)
VALUES ('Eve');
INSERT INTO simulation (simulation_id, client_name, client_id, departure_date, arrival_date,
                                              num_people, cost, simulation_created_at, is_reservation)
VALUES (1, 'Alice', 1, '2023-06-01', '2023-06-11', 2, 2500, '2023-05-01', 1);
INSERT INTO simulation (simulation_id, client_name, client_id, departure_date, arrival_date,
                                              num_people, cost, simulation_created_at, is_reservation)
VALUES (2, 'Bob', 2, '2023-06-15', '2023-06-27', 1, 1800, '2023-05-02', 0);
INSERT INTO simulation (simulation_id, client_name, client_id, departure_date, arrival_date,
                                              num_people, cost, simulation_created_at, is_reservation)
VALUES (3, 'Charlie', null, '2023-07-01', '2023-07-09', 4, 4800, '2023-05-03', 0);
INSERT INTO simulation (simulation_id, client_name, client_id, departure_date, arrival_date,
                                              num_people, cost, simulation_created_at, is_reservation)
VALUES (4, 'David', null, '2023-07-10', '2023-08-09', 3, 15000, '2023-05-04', 0);
INSERT INTO simulation (simulation_id, client_name, client_id, departure_date, arrival_date,
                                              num_people, cost, simulation_created_at, is_reservation)
VALUES (5, 'Eve', null, '2023-08-01', '2023-08-06', 2, 1600, '2023-05-05', 0);
INSERT INTO simulation (simulation_id, client_name, client_id, departure_date, arrival_date,
                                              num_people, cost, simulation_created_at, is_reservation)
VALUES (6, 'Alice', 1, '2023-06-12', '2023-06-13', 1, 1800, '2023-05-05', 0);
INSERT INTO simulation (simulation_id, client_name, client_id, departure_date, arrival_date,
                                              num_people, cost, simulation_created_at, is_reservation)
VALUES (7, 'Alma', null, '2023-06-12', '2023-06-13', 1, 1800, '2023-05-05', 0);
INSERT INTO simulation (simulation_id, client_name, client_id, departure_date, arrival_date,
                                              num_people, cost, simulation_created_at, is_reservation)
VALUES (8, 'Alice', 1, '2023-06-12', '2023-06-13', 1, 1800, '2023-05-05', 0);
INSERT INTO simulation (simulation_id, client_name, client_id, departure_date, arrival_date,
                                              num_people, cost, simulation_created_at, is_reservation)
VALUES (10, 'Alice', 1, '2023-06-12', '2023-06-13', 1, 1800, '2023-05-05', 0);
INSERT INTO simulation (simulation_id, client_name, client_id, departure_date, arrival_date,
                                              num_people, cost, simulation_created_at, is_reservation)
VALUES (11, 'Alice', 1, '2023-06-12', '2023-06-13', 1, 1800, '2023-05-05', 0);
INSERT INTO simulationcircuit (simulation_id, circuit_id)
VALUES (1, 'C1');
INSERT INTO simulationcircuit (simulation_id, circuit_id)
VALUES (2, 'C2');
INSERT INTO simulationcircuit (simulation_id, circuit_id)
VALUES (3, 'C3');
INSERT INTO simulationcircuit (simulation_id, circuit_id)
VALUES (4, 'C4');
INSERT INTO simulationcircuit (simulation_id, circuit_id)
VALUES (5, 'C5');
INSERT INTO simulationhotel (simulation_id, hotel_name, departure_date, arrival_date)
VALUES (1, 'NY Hotel', '2023-06-01', '2023-06-05');
INSERT INTO simulationhotel (simulation_id, hotel_name, departure_date, arrival_date)
VALUES (2, 'Paris Hotel', '2023-06-15', '2023-06-21');
INSERT INTO simulationhotel (simulation_id, hotel_name, departure_date, arrival_date)
VALUES (3, 'Tokyo Hotel', '2023-07-01', '2023-07-05');
INSERT INTO simulationhotel (simulation_id, hotel_name, departure_date, arrival_date)
VALUES (4, 'London Hotel', '2023-07-10', '2023-07-20');
INSERT INTO simulationhotel (simulation_id, hotel_name, departure_date, arrival_date)
VALUES (5, 'LA Hotel', '2023-08-01', '2023-08-03');
INSERT INTO reservation (reservation_id, simulation_id, client_id, reservation_created_at)
VALUES (1, 1, 1, '2023-05-01');
INSERT INTO reservationcircuit (reservation_id, circuit_id)
VALUES (1, 'C1');
INSERT INTO stage (stage_order, order_duration, circuit_id, place_name, address)
VALUES (1, 2, 'C1', 'Statue of Liberty', null);
INSERT INTO stage (stage_order, order_duration, circuit_id, place_name, address)
VALUES (2, 2, 'C2', 'Eiffel Tower', null);
INSERT INTO stage (stage_order, order_duration, circuit_id, place_name, address)
VALUES (3, 2, 'C3', 'Tokyo Tower', null);
INSERT INTO stage (stage_order, order_duration, circuit_id, place_name, address)
VALUES (4, 2, 'C4', 'Big Ben', null);
INSERT INTO stage (stage_order, order_duration, circuit_id, place_name, address)
VALUES (5, 2, 'C5', 'Hollywood Sign', null);

SELECT Circuit.circuit_id, description, departure_date, trip_duration, cost
FROM circuit,
     datecircuit
WHERE (circuit.circuit_id = datecircuit.circuit_id AND departing_city = 'New York' AND
       arrival_city = 'Los Angeles' AND departure_date = '2023-05-10');

