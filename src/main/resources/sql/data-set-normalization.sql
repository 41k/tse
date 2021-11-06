-- If initial_data_set contains timestamp of bar opening then we should convert open timestamp to close timestamp
-- for each row which represents particular bar using the next script:
INSERT INTO normalized_data_set (id, exchange_gateway, symbol, time_interval, duration, `timestamp`, `open`, high, low, `close`, volume)
SELECT id, exchange_gateway, symbol, time_interval, duration,
CASE
    WHEN time_interval = 'ONE_MINUTE' THEN `timestamp` + 60000
    WHEN time_interval = 'THREE_MINUTES' THEN `timestamp` + 180000
    WHEN time_interval = 'FIVE_MINUTES' THEN `timestamp` + 300000
    WHEN time_interval = 'FIFTEEN_MINUTES' THEN `timestamp` + 900000
    WHEN time_interval = 'THIRTY_MINUTES' THEN `timestamp` + 1800000
    WHEN time_interval = 'ONE_HOUR' THEN `timestamp` + 3600000
    WHEN time_interval = 'TWO_HOURS' THEN `timestamp` + 7200000
    WHEN time_interval = 'FOUR_HOURS' THEN `timestamp` + 14400000
    WHEN time_interval = 'SIX_HOURS' THEN `timestamp` + 21600000
    WHEN time_interval = 'EIGHT_HOURS' THEN `timestamp` + 28800000
    WHEN time_interval = 'TWELVE_HOURS' THEN `timestamp` + 43200000
    WHEN time_interval = 'ONE_DAY' THEN `timestamp` + 86400000
    WHEN time_interval = 'THREE_DAYS' THEN `timestamp` + 259200000
    ELSE `timestamp`
END AS close_timestamp,
`open`, high, low, `close`, volume
FROM initial_data_set;

-- Check normalized_data_set: values of close price (e.g. close) should be equal:
-- for ONE_MINUTE, FIVE_MINUTES, FIFTEEN_MINUTES at e.g. 09:15
-- for ONE_MINUTE, FIVE_MINUTES at e.g. 10:40
SELECT ds.time_interval, FROM_UNIXTIME(ds.`timestamp`/1000) as ts, ds.`close`
FROM normalized_data_set ds
WHERE ds.symbol = 'ETH_USD'
AND (ds.time_interval = 'ONE_MINUTE' OR ds.time_interval = 'FIVE_MINUTES' OR ds.time_interval = 'FIFTEEN_MINUTES')
ORDER BY ts ASC;
