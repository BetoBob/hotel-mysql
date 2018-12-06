-- OR-1 Occupancy Overview

    -- One Date
    -- Date: "2010-10-30"
    SELECT DISTINCT RoomId, RoomName, "occupied" AS Status  
    FROM myReservations re
        JOIN myRooms ro
            ON (re.Room = ro.RoomId)
    WHERE DATEDIFF(CheckIn,  DATE("2010-10-30")) <= 0 -- Change to specific day
      AND DATEDIFF(CheckOut, DATE("2010-10-30")) >= 0
    UNION 
    SELECT DISTINCT RoomId, RoomName, "empty" AS Status  
    FROM myRooms
    WHERE RoomId NOT IN (
        SELECT DISTINCT RoomId
        FROM myReservations re
            JOIN myRooms ro
                ON (re.Room = ro.RoomId)
        WHERE DATEDIFF(CheckIn,  DATE("2010-10-30")) <= 0
          AND DATEDIFF(CheckOut, DATE("2010-10-30")) >= 0
    )
    ORDER BY RoomId;

    SELECT *
    FROM myReservations
    WHERE Room = "IBS" -- change to a specific room
      AND DATEDIFF(CheckIn,  DATE("2010-10-30")) <= 0 
      AND DATEDIFF(CheckOut, DATE("2010-10-30")) >= 0;

    -- Two Dates
    -- CheckIn: "2010-10-30"
    -- CheckOut: "2010-11-05"
    -- Note: CheckOut day not considered a full day?

    SELECT DISTINCT RoomId, RoomName, "fully occupied" AS Status
    FROM myReservations re
        JOIN myRooms ro
            ON (re.Room = ro.RoomId)
    WHERE DATEDIFF(CheckIn,  DATE("2010-10-30")) <= 0 -- CheckIn
      AND DATEDIFF(CheckOut, DATE("2010-11-05")) > 0 -- CheckOut
    UNION
    SELECT DISTINCT RoomId, RoomName, "partially occupied" AS Status
    FROM myReservations re
        JOIN myRooms ro
            ON (re.Room = ro.RoomId)
    WHERE (DATEDIFF(CheckIn, DATE("2010-10-30")) > 0 -- CheckIn
      AND DATEDIFF(CheckIn, DATE("2010-11-05")) <= 0) -- CheckOut
       OR (DATEDIFF(CheckOut, DATE("2010-10-30")) > 0 -- CheckIn
      AND DATEDIFF(CheckOut, DATE("2010-11-05")) <= 0) -- CheckOut
    UNION
    SELECT DISTINCT RoomId, RoomName, "empty" AS STATUS
    FROM myRooms
    WHERE RoomId NOT IN (
        SELECT DISTINCT RoomId
        FROM myReservations re
            JOIN myRooms ro
                ON (re.Room = ro.RoomId)
        WHERE (DATEDIFF(CheckIn,  DATE("2010-10-30")) <= 0 -- CheckIn
        AND DATEDIFF(CheckOut, DATE("2010-11-05")) > 0) -- CheckOut
        OR (DATEDIFF(CheckIn, DATE("2010-10-30")) > 0 -- CheckIn
        AND DATEDIFF(CheckIn, DATE("2010-11-05")) <= 0) -- CheckOut
        OR (DATEDIFF(CheckOut, DATE("2010-10-30")) > 0 -- CheckIn
        AND DATEDIFF(CheckOut, DATE("2010-11-05")) <= 0) -- CheckOut
    );

    -- showing status
    SELECT *, "fully occupied" AS Status
    FROM myReservations
    WHERE DATEDIFF(CheckIn,  DATE("2010-10-30")) <= 0 -- CheckIn
      AND DATEDIFF(CheckOut, DATE("2010-11-05")) > 0 -- CheckOut
    UNION
    SELECT *, "partially occupied" AS Status
    FROM myReservations
    WHERE (DATEDIFF(CheckIn, DATE("2010-10-30")) > 0 -- CheckIn
      AND DATEDIFF(CheckIn, DATE("2010-11-05")) <= 0) -- CheckOut
       OR (DATEDIFF(CheckOut, DATE("2010-10-30")) > 0 -- CheckIn
      AND DATEDIFF(CheckOut, DATE("2010-11-05")) <= 0); -- CheckOut

    -- for myReservations function
    SELECT *
    FROM myReservations
    WHERE (DATEDIFF(CheckIn,  DATE("2010-10-30")) <= 0 -- CheckIn inner
      AND DATEDIFF(CheckOut, DATE("2010-11-05")) > 0) -- CheckOut inner
       OR (DATEDIFF(CheckIn, DATE("2010-10-30")) > 0 -- CheckIn outer left
      AND DATEDIFF(CheckIn, DATE("2010-11-05")) <= 0) -- CheckOut outer left
       OR (DATEDIFF(CheckOut, DATE("2010-10-30")) > 0 -- CheckIn outer right
      AND DATEDIFF(CheckOut, DATE("2010-11-05")) <= 0); -- CheckOut outer right

SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY, IS_NULLABLE
FROM information_schema.columns 
WHERE table_schema = 'INN';
