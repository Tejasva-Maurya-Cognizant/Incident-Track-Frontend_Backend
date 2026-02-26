INSERT IGNORE INTO tasks
(completed_date, created_date, description, due_date, status, title, assigned_by, assigned_to, incident_id)
VALUES
-- IT: created earlier, assigned by manager to employee, still pending
(
 NULL,
 NOW(6) - INTERVAL 5 HOUR,
 'Check VPN client logs, confirm SSO status, test from alternate network.',
 NOW(6) + INTERVAL 12 HOUR,
 'PENDING',
 'Investigate VPN failure',
 (SELECT user_id FROM users WHERE email='it_mgr@company.com' LIMIT 1),
 (SELECT user_id FROM users WHERE email='it_emp2@company.com' LIMIT 1),
 (SELECT incident_id FROM incidents WHERE description LIKE 'VPN not connecting%' ORDER BY incident_id DESC LIMIT 1)
),

-- Security: in progress
(
 NULL,
 NOW(6) - INTERVAL 2 HOUR,
 'Run endpoint scan, reset password, capture suspicious email headers.',
 NOW(6) + INTERVAL 4 HOUR,
 'IN_PROGRESS',
 'Contain phishing impact',
 (SELECT user_id FROM users WHERE email='sec_mgr@company.com' LIMIT 1),
 (SELECT user_id FROM users WHERE email='sec_emp1@company.com' LIMIT 1),
 (SELECT incident_id FROM incidents WHERE description LIKE 'Possible phishing%' ORDER BY incident_id DESC LIMIT 1)
),

-- Facilities: completed by employee
(
 NOW(6) - INTERVAL 30 MINUTE,
 NOW(6) - INTERVAL 90 MINUTE,
 'Coordinate maintenance, check UPS load, restore stable power to desks.',
 NOW(6) + INTERVAL 2 HOUR,
 'COMPLETED',
 'Stabilize power in wing B',
 (SELECT user_id FROM users WHERE email='fac_mgr@company.com' LIMIT 1),
 (SELECT user_id FROM users WHERE email='fac_emp1@company.com' LIMIT 1),
 (SELECT incident_id FROM incidents WHERE description LIKE 'Power flickering%' ORDER BY incident_id DESC LIMIT 1)
),

-- HR: pending
(
 NULL,
 NOW(6) - INTERVAL 20 HOUR,
 'Provision laptop, create accounts, share onboarding checklist.',
 NOW(6) + INTERVAL 20 HOUR,
 'PENDING',
 'Complete onboarding setup',
 (SELECT user_id FROM users WHERE email='hr_mgr@company.com' LIMIT 1),
 (SELECT user_id FROM users WHERE email='hr_emp1@company.com' LIMIT 1),
 (SELECT incident_id FROM incidents WHERE description LIKE 'Onboarding delay%' ORDER BY incident_id DESC LIMIT 1)
),

-- Finance: completed
(
 NOW(6) - INTERVAL 12 HOUR,
 NOW(6) - INTERVAL 40 HOUR,
 'Match invoice items with cost centers and confirm vendor bill.',
 NOW(6) - INTERVAL 10 HOUR,
 'COMPLETED',
 'Reconcile billing discrepancy',
 (SELECT user_id FROM users WHERE email='fin_mgr@company.com' LIMIT 1),
 (SELECT user_id FROM users WHERE email='fin_emp1@company.com' LIMIT 1),
 (SELECT incident_id FROM incidents WHERE description LIKE 'Billing discrepancy%' ORDER BY incident_id DESC LIMIT 1)
),

-- Ops: in progress
(
 NULL,
 NOW(6) - INTERVAL 4 HOUR,
 'Identify failing batch stage, rerun jobs, and monitor completion.',
 NOW(6) + INTERVAL 10 HOUR,
 'IN_PROGRESS',
 'Fix batch processing delay',
 (SELECT user_id FROM users WHERE email='ops_mgr@company.com' LIMIT 1),
 (SELECT user_id FROM users WHERE email='ops_emp1@company.com' LIMIT 1),
 (SELECT incident_id FROM incidents WHERE description LIKE 'Batch processing delay%' ORDER BY incident_id DESC LIMIT 1)
),

-- Procurement/Vendor: pending
(
 NULL,
 NOW(6) - INTERVAL 12 HOUR,
 'Follow up supplier for documents, validate contract checklist.',
 NOW(6) + INTERVAL 30 HOUR,
 'PENDING',
 'Collect vendor onboarding documents',
 (SELECT user_id FROM users WHERE email='proc_mgr@company.com' LIMIT 1),
 (SELECT user_id FROM users WHERE email='proc_emp1@company.com' LIMIT 1),
 (SELECT incident_id FROM incidents WHERE description LIKE 'Vendor onboarding delay%' ORDER BY incident_id DESC LIMIT 1)
);
