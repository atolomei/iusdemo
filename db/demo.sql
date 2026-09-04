-- ------------------------------------------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------------------------------------------
BEGIN;
set client_encoding to 'utf8';
COMMIT;
END;



BEGIN;


CREATE SEQUENCE public.sequence_id
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


    CREATE SEQUENCE public.sequence_user_id
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE SEQUENCE public.sequence_visit_id
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

						
CREATE TABLE  Users (
						id					 bigint primary key default nextval('sequence_id'),
						name				 character varying(512) not null,
						state 				 integer default 1,
						
						firstname          character varying(128),
						lastname            character varying(512),
						
						password           character varying(512), 
 						email              character varying(512), 
						phone              character varying(64),

						locale            character varying(64)   default 'es',
 						zoneid            character varying(512)  default 'America/Buenos_aires',
						
						showwelcome        	boolean default true,
					    initialsignindone   boolean default  false,
						welcomeemailsent    timestamp with time zone,
						emailvalidated      boolean default false,
						
						created				 timestamp with time zone DEFAULT now() not null,
						lastmodified		 timestamp with time zone DEFAULT now() not null,
						lastmodifieduser	 bigint references users(id) on delete restrict 
					);
					


					
CREATE TABLE  UserPreferences (
 	id bigint              primary key default nextval('sequence_id'),
	 user_id 				bigint references users(id) not null, 
	 preferences			text,
 	created			 	timestamp with time zone DEFAULT now() not null,
 	lastmodified		 	timestamp with time zone DEFAULT now() not null,
 	lastmodifieduser	    bigint references users(id) on delete restrict not null
);



INSERT INTO users (id, name, state,  firstname, lastname, email) VALUES(1, 'root', 1, 'root', null, 'info@novamens.com');
update users set lastModifiedUser = 1 where id=1;


INSERT INTO users (id, name, email, lastmodifieduser) VALUES  (nextval('sequence_id'), 'atolomei', 'atolomei@novamens.com', (select id from users where name='root'));
INSERT INTO users (id, name, email, lastmodifieduser) VALUES  (nextval('sequence_id'), 'aferraria', 'aferraria@novamens.com', (select id from users where name='root'));

insert into UserPreferences (id, user_id, created, lastModified, lastModifieduser)  VALUES  (nextval('sequence_id'), (select id from users where name='root')      , now(), now(), (select id from users where name='root'));
insert into UserPreferences (id, user_id, created, lastModified, lastModifieduser)  VALUES  (nextval('sequence_id'), (select id from users where name='atolomei')  , now(), now(), (select id from users where name='root'));
insert into UserPreferences (id, user_id, created, lastModified, lastModifieduser)  VALUES  (nextval('sequence_id'), (select id from users where name='aferraria') , now(), now(), (select id from users where name='root'));


CREATE TABLE  Query (
 id                     bigint primary key default nextval('sequence_id'),
 query	                text    not null, 
 results 				text,
 session_id             character varying(255) not null,
 durationMillisecs	    bigint default 0,
 created			 	timestamp with time zone DEFAULT now() not null,
 lastmodified		 	timestamp with time zone DEFAULT now() not null,
 lastmodifieduser	 bigint references users(id) on delete restrict not null
);


CREATE TABLE  DocumentAnalyze (
 id bigint              primary key default nextval('sequence_id'),
 query_id	            bigint references query(id) not null, 
 results 				text,
 session_id             character varying(255) not null,
 durationMillisecs	    bigint default 0,
 created			 	timestamp with time zone DEFAULT now() not null,
 lastmodified		 	timestamp with time zone DEFAULT now() not null,
 lastmodifieduser	 bigint references users(id) on delete restrict not null
);


CREATE TABLE  Stat (
id bigint               primary key default nextval('sequence_visit_id'),
 page_id                character varying(255)    not null, 
 session_id             character varying(255)    not null,
 info					text,
 ts                     timestamp with time zone  not null  default now(),
 useragent              character varying(1024)  
 );

				
					
COMMIT;
	

END;

