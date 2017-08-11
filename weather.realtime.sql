-- Table: weather.realtime

DROP TABLE weather.realtime;

CREATE TABLE weather.realtime
(
  primary key (tenant, dateutc),
  tenant character varying NOT NULL,
  dateutc timestamp with time zone NOT NULL,
  windspeedmph real,
  winddir smallint,
  windgustmph real,
  dailyrainin real,
  tempf real,
  baromin real,
  dewptf real,
  humidity real,
  solarradiation smallint,
  uv real
)
WITH (
  OIDS=FALSE
);
ALTER TABLE weather.realtime
  OWNER TO sysweather;
GRANT ALL ON TABLE weather.test TO public;
GRANT ALL ON TABLE weather.test TO sysweather;
