-- AR-1 Current Status Display

    -- Status
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = 'rehensle' -- input1
      AND table_name = 'myRooms'
       OR table_name = 'myReservations';

SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY, IS_NULLABLE
FROM information_schema.columns 
WHERE table_schema = 'INN';


