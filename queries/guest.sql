-- R-2. Checking Availability
-- CheckIn:  "2010-5-8"
-- CheckOut: "2010-5-9"

    SELECT status
    FROM (
        SELECT DISTINCT ro.RoomId, "occupied" AS status
        FROM myReservations re
            JOIN myRooms ro
                ON (re.Room = ro.RoomId)
        WHERE (DATEDIFF(CheckIn,  DATE("2010-5-8")) <= 0  -- FirstDate inner
        AND DATEDIFF(CheckOut,  DATE("2010-5-9")) > 0)  -- LastDate inner
        OR (DATEDIFF(CheckIn,  DATE("2010-5-8")) > 0   -- FirstDate outer left
        AND DATEDIFF(CheckIn,   DATE("2010-5-9")) <= 0) -- LastDate outer left
        OR (DATEDIFF(CheckOut, DATE("2010-5-8")) > 0   -- FirstDate outer right
        AND DATEDIFF(CheckOut,  DATE("2010-5-9")) < 0) -- LastDate outer right
        UNION
        SELECT DISTINCT RoomId, "empty" AS status
        FROM myRooms
        WHERE RoomId NOT IN (
            SELECT DISTINCT re.Room
                FROM myReservations re
                    JOIN myRooms ro
                        ON (re.Room = ro.RoomId)
            WHERE (DATEDIFF(CheckIn,  DATE("2010-5-8")) <= 0  -- FirstDate inner
            AND DATEDIFF(CheckOut,  DATE("2010-5-9")) > 0)  -- LastDate inner
            OR (DATEDIFF(CheckIn,  DATE("2010-5-8")) > 0   -- FirstDate outer left
            AND DATEDIFF(CheckIn,   DATE("2010-5-9")) <= 0) -- LastDate outer left
            OR (DATEDIFF(CheckOut, DATE("2010-5-8")) > 0   -- FirstDate outer right
            AND DATEDIFF(CheckOut,  DATE("2010-5-9")) < 0)   
        )
    ) AS tb
    WHERE RoomId = 'CAS'; -- user input


SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY, IS_NULLABLE
FROM information_schema.columns 
WHERE table_schema = 'INN';
