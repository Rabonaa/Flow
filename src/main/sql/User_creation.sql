USE flow;
CREATE USER IF NOT EXISTS 'user'@'localhost'
IDENTIFIED BY 'Password123';

GRANT ALL PRIVILEGES 
ON flow.* 
TO 'user'@'localhost';