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

    -- view reservations (OR-5)
    -- Room selected: "IBS"
    SELECT *
    FROM myReservations
    WHERE Room = "IBS" -- change to a specific room
      AND DATEDIFF(CheckIn,  DATE("2010-10-30")) <= 0 
      AND DATEDIFF(CheckOut, DATE("2010-10-30")) >= 0;

    -- Two Dates
    -- FirstDate: "2010-10-30"
    -- LastDate: "2010-11-05"
    -- Note: CheckOut day not considered a full day?

    SELECT DISTINCT RoomId, RoomName, "fully occupied" AS Status
    FROM myReservations re
        JOIN myRooms ro
            ON (re.Room = ro.RoomId)
    WHERE DATEDIFF(CheckIn,  DATE("2010-10-30")) <= 0   -- FirstDate
      AND DATEDIFF(CheckOut, DATE("2010-11-05")) > 0    -- LastDate
    UNION
    SELECT DISTINCT RoomId, RoomName, "partially occupied" AS Status
    FROM myReservations re
        JOIN myRooms ro
            ON (re.Room = ro.RoomId)
    WHERE (DATEDIFF(CheckIn, DATE("2010-10-30")) > 0    -- FirstDate
      AND DATEDIFF(CheckIn, DATE("2010-11-05")) <= 0)   -- LastDate
       OR (DATEDIFF(CheckOut, DATE("2010-10-30")) > 0   -- FirstDate
      AND DATEDIFF(CheckOut, DATE("2010-11-05")) <= 0)  -- LastDate
    UNION
    SELECT DISTINCT RoomId, RoomName, "empty" AS STATUS
    FROM myRooms
    WHERE RoomId NOT IN (
        SELECT DISTINCT RoomId
        FROM myReservations re
            JOIN myRooms ro
                ON (re.Room = ro.RoomId)
        WHERE (DATEDIFF(CheckIn,  DATE("2010-10-30")) <= 0  -- FirstDate
        AND DATEDIFF(CheckOut, DATE("2010-11-05")) > 0)     -- LastDate
        OR (DATEDIFF(CheckIn, DATE("2010-10-30")) > 0       -- FirstDate
        AND DATEDIFF(CheckIn, DATE("2010-11-05")) <= 0)     -- LastDate
        OR (DATEDIFF(CheckOut, DATE("2010-10-30")) > 0      -- FirstDate
        AND DATEDIFF(CheckOut, DATE("2010-11-05")) <= 0)    -- LastDate
    );

    -- showing status
    SELECT *, "fully occupied" AS Status
    FROM myReservations
    WHERE DATEDIFF(CheckIn,  DATE("2010-10-30")) <= 0   -- FirstDate
      AND DATEDIFF(CheckOut, DATE("2010-11-05")) > 0    -- LastDate
    UNION
    SELECT *, "partially occupied" AS Status
    FROM myReservations
    WHERE (DATEDIFF(CheckIn, DATE("2010-10-30")) > 0    -- FirstDate
      AND DATEDIFF(CheckIn, DATE("2010-11-05")) <= 0)   -- LastDate
       OR (DATEDIFF(CheckOut, DATE("2010-10-30")) > 0   -- FirstDate
      AND DATEDIFF(CheckOut, DATE("2010-11-05")) <= 0); -- LastDate

    -- for myReservations function (OR-5)
    -- room selected: "IBS"
    SELECT *
    FROM myReservations
    WHERE Room = "IBS"
      AND (DATEDIFF(CheckIn,  DATE("2010-10-30")) <= 0  -- FirstDate inner
      AND DATEDIFF(CheckOut, DATE("2010-11-05")) > 0)   -- LastDate inner
       OR (DATEDIFF(CheckIn, DATE("2010-10-30")) > 0    -- FirstDate outer left
      AND DATEDIFF(CheckIn, DATE("2010-11-05")) <= 0)   -- LastDate outer left
       OR (DATEDIFF(CheckOut, DATE("2010-10-30")) > 0   -- FirstDate outer right
      AND DATEDIFF(CheckOut, DATE("2010-11-05")) <= 0); -- LastDate outer right

-- OR-2 Revenue (just use one of the three for a model)

    -- option 1: # of reservations
    -- "COUNT(*) AS Reservations"
    SELECT RoomId, RoomName, Month, Reservations -- input1 (Reservations)
    FROM (
        SELECT ro.RoomId, ro.RoomName, 
        MONTH(CheckOut) AS monthId, MONTHNAME(CheckOut) AS Month,
        COUNT(*) AS Reservations -- input2
        FROM myReservations re
            JOIN myRooms ro
                ON(re.Room = ro.RoomId)
        WHERE YEAR(CheckOut) = 2010
        GROUP BY ro.RoomId, ro.RoomName, monthId, Month
        UNION 
        SELECT ro.RoomId, ro.RoomName, 
            13 AS monthId, "Total" AS Month,
            COUNT(*) AS Reservations -- input2
        FROM myReservations re
            JOIN myRooms ro
                ON(re.Room = ro.RoomId)
        WHERE YEAR(CheckOut) = 2010
        GROUP BY ro.RoomId, ro.RoomName
    ) AS rt
    ORDER BY RoomId, monthId;

    -- option 2: # of days occupied
    -- "SUM(DATEDIFF(CheckOut, CheckIn)) AS days_occupied"
    SELECT RoomId, RoomName, Month, days_occupied -- input1 (days_occupied)
    FROM (
        SELECT ro.RoomId, ro.RoomName, 
        MONTH(CheckOut) AS monthId, MONTHNAME(CheckOut) AS Month,
        SUM(DATEDIFF(CheckOut, CheckIn)) AS days_occupied -- input2
        FROM myReservations re
            JOIN myRooms ro
                ON(re.Room = ro.RoomId)
        WHERE YEAR(CheckOut) = 2010
        GROUP BY ro.RoomId, ro.RoomName, monthId, Month
        UNION 
        SELECT ro.RoomId, ro.RoomName, 
            13 AS monthId, "Total" AS Month,
            SUM(DATEDIFF(CheckOut, CheckIn)) AS days_occupied -- input2
        FROM myReservations re
            JOIN myRooms ro
                ON(re.Room = ro.RoomId)
        WHERE YEAR(CheckOut) = 2010
        GROUP BY ro.RoomId, ro.RoomName
    ) AS rt
    ORDER BY RoomId, monthId;
    
    -- option 3: dollar revenue of each month
    -- "SUM(DATEDIFF(CheckOut, CheckIn) * Rate) AS Revenue"
    SELECT RoomId, RoomName, Month, Revenue -- input1 (Reservations)
    FROM (
        SELECT ro.RoomId, ro.RoomName, 
        MONTH(CheckOut) AS monthId, MONTHNAME(CheckOut) AS Month,
        SUM(DATEDIFF(CheckOut, CheckIn) * Rate) AS Revenue -- input2
        FROM myReservations re
            JOIN myRooms ro
                ON(re.Room = ro.RoomId)
        WHERE YEAR(CheckOut) = 2010
        GROUP BY ro.RoomId, ro.RoomName, monthId, Month
        UNION 
        SELECT ro.RoomId, ro.RoomName, 
            13 AS monthId, "Total" AS Month,
            SUM(DATEDIFF(CheckOut, CheckIn) * Rate) AS Revenue -- input2
        FROM myReservations re
            JOIN myRooms ro
                ON(re.Room = ro.RoomId)
        WHERE YEAR(CheckOut) = 2010
        GROUP BY ro.RoomId, ro.RoomName
    ) AS rt
    ORDER BY RoomId, monthId;    
    
-- OR-3 Reservations

-- OR-4 Rooms


SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY, IS_NULLABLE
FROM information_schema.columns 
WHERE table_schema = 'INN';
