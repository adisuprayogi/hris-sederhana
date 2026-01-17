-- Fix passwords using fresh BCrypt hashes
-- Password: admin123
UPDATE employees SET password = '$2a$10$DwPUnokaCZG943pwfCWSU.79uTcBYYuEWENE3K1IVMf/NZlhr9GZy' 
WHERE email = 'admin@hris.local';

-- Password: test123  
UPDATE employees SET password = '$2a$10$DwaIqifsVcwH7oDyzFkjPunuzONOnyTIzIhr1dECE.B19XBy0xJ6.'
WHERE email = 'allroles@hris.local';
