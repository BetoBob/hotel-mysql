-- AR-1 Current Status Display

    -- Status
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = 'rehensle';

    -- Reservations
    SELECT COUNT(*)
    FROM reservations;

    -- Rooms
    SELECT COUNT(*)
    FROM rooms;

-- AR-2 Table display

-- AR-3 Clear database

-- AR-4 Load/Reload DB

-- AR-5 Databse Removal

-- AR-6 Switch subsytem

SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY, IS_NULLABLE
FROM information_schema.columns 
WHERE table_schema = 'INN';
