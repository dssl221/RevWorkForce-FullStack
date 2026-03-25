-- ============================================================
-- Fix User Passwords - Run this on FreeSQLDatabase.com
-- Updates passwords from BCrypt hashes to plain text
-- ============================================================

-- Admin: admin123
UPDATE users SET password = 'admin123'
WHERE email = 'admin@revworkforce.com';

-- Manager: manager123
UPDATE users SET password = 'manager123'
WHERE email = 'manager@revworkforce.com';

-- Employees
UPDATE users SET password = 'employee123'
WHERE email = 'employee@revworkforce.com';

UPDATE users SET password = 'jane123'
WHERE email = 'jane@revworkforce.com';

UPDATE users SET password = '123456'
WHERE email = 'abc@gmail.com';

UPDATE users SET password = '123456'
WHERE email = 'tushar@gmail.com';

COMMIT;

-- Verify the update
SELECT email, password FROM users;
