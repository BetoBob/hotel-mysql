-- R-1. Rooms and Rates.

SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY, IS_NULLABLE
FROM information_schema.columns 
WHERE table_schema = 'INN';
