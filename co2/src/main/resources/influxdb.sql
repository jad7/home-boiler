CREATE RETENTION POLICY forever DURATION INF REPLICATION 1
CREATE RETENTION POLICY one_year ON boiler  DURATION 52w REPLICATION 1
CREATE CONTINUOUS QUERY "cq_1h" on "boiler" RESAMPLE EVERY 1h FOR 1h BEGIN SELECT mean("value"), min("value"), max("value") INTO "one_year"."one_year_history" FROM "sensors"  GROUP BY time(1h), "sensor" END
CREATE RETENTION POLICY one_year ON boiler  DURATION 52w REPLICATION 1
CREATE CONTINUOUS QUERY "cq_1d" on "boiler" RESAMPLE EVERY 1d FOR 1d BEGIN SELECT mean("value"), min("value"), max("value") INTO "forever"."history" FROM "sensors"  GROUP BY time(1d), "sensor" END


##Enable
gpio write 17 1
##Disable
gpio write 17 0