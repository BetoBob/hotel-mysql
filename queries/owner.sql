-- OR-1 Occupancy Overview

    -- One Date
    SELECT DISTINCT RoomId, RoomName, "occupied" AS Status  
    FROM myReservations re
        JOIN myRooms ro
            ON (re.Room = ro.RoomId)
    WHERE DATEDIFF(CheckIn,  DATE("2010-10-30")) <= 0 -- Change to specific day
      AND DATEDIFF(CheckOut, DATE("2010-10-30")) >= 0
    UNION 
    SELECT DISTINCT RoomId, RoomName, "empty" AS Status  
    FROM myRooms
    WHERE RoomName NOT IN (
        SELECT DISTINCT RoomName
        FROM myReservations re
            JOIN myRooms ro
                ON (re.Room = ro.RoomId)
        WHERE DATEDIFF(CheckIn,  DATE("2010-10-30")) <= 0
          AND DATEDIFF(CheckOut, DATE("2010-10-30")) >= 0
    )
    ORDER BY RoomId;

    SELECT *
    FROM myReservations
    WHERE Room = "IBS"
      AND DATEDIFF(CheckIn,  DATE("2010-10-30")) <= 0 
      AND DATEDIFF(CheckOut, DATE("2010-10-30")) >= 0;

    -- Two Dates

SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY, IS_NULLABLE
FROM information_schema.columns 
WHERE table_schema = 'INN';
