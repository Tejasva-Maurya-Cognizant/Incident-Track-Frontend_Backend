INSERT IGNORE INTO audit_log
(action_type, details, timestamp, incident_id, user_id)
VALUES
-- IT incident
('INCIDENT_CREATED', 'IT employee reported VPN login loop issue.', NOW(6) - INTERVAL 6 HOUR,
 (SELECT incident_id FROM incidents WHERE description LIKE 'VPN not connecting%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='it_emp1@company.com' LIMIT 1)
),
-- task created by ADMIN (anyone can create)
('TASK_CREATED', 'Task created to investigate VPN issue.', NOW(6) - INTERVAL 5 HOUR,
 (SELECT incident_id FROM incidents WHERE description LIKE 'VPN not connecting%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='it_admin@company.com' LIMIT 1)
),
('TASK_ASSIGNED', 'IT manager assigned task to IT employee.', NOW(6) - INTERVAL 5 HOUR + INTERVAL 1 MINUTE,
 (SELECT incident_id FROM incidents WHERE description LIKE 'VPN not connecting%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='it_mgr@company.com' LIMIT 1)
),

-- Security incident
('INCIDENT_CREATED', 'Security employee reported suspected phishing click.', NOW(6) - INTERVAL 3 HOUR,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Possible phishing%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='sec_emp1@company.com' LIMIT 1)
),
-- task created by EMPLOYEE (anyone can create)
('TASK_CREATED', 'Containment task created after initial report.', NOW(6) - INTERVAL 2 HOUR,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Possible phishing%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='sec_emp1@company.com' LIMIT 1)
),
('TASK_ASSIGNED', 'Security manager assigned containment task to employee.', NOW(6) - INTERVAL 2 HOUR + INTERVAL 2 MINUTE,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Possible phishing%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='sec_mgr@company.com' LIMIT 1)
),
('INCIDENT_STATUS_CHANGED', 'Incident moved to IN_PROGRESS for containment and scanning.', NOW(6) - INTERVAL 2 HOUR + INTERVAL 5 MINUTE,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Possible phishing%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='sec_mgr@company.com' LIMIT 1)
),

-- Facilities incident
('INCIDENT_CREATED', 'Facilities employee reported intermittent power loss.', NOW(6) - INTERVAL 2 HOUR,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Power flickering%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='fac_emp1@company.com' LIMIT 1)
),
-- task created by MANAGER
('TASK_CREATED', 'Task created to stabilize power and check UPS.', NOW(6) - INTERVAL 90 MINUTE,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Power flickering%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='fac_mgr@company.com' LIMIT 1)
),
('TASK_ASSIGNED', 'Facilities manager assigned task to employee.', NOW(6) - INTERVAL 89 MINUTE,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Power flickering%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='fac_mgr@company.com' LIMIT 1)
),
('TASK_STATUS_CHANGED', 'Employee marked task COMPLETED after restoring stable power.', NOW(6) - INTERVAL 30 MINUTE,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Power flickering%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='fac_emp1@company.com' LIMIT 1)
),

-- HR incident
('INCIDENT_CREATED', 'HR employee reported onboarding delay due to missing laptop.', NOW(6) - INTERVAL 1 DAY,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Onboarding delay%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='hr_emp1@company.com' LIMIT 1)
),
('TASK_CREATED', 'Task created to provision laptop and accounts.', NOW(6) - INTERVAL 20 HOUR,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Onboarding delay%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='hr_admin@company.com' LIMIT 1)
),
('TASK_ASSIGNED', 'HR manager assigned onboarding setup task to employee.', NOW(6) - INTERVAL 19 HOUR + INTERVAL 55 MINUTE,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Onboarding delay%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='hr_mgr@company.com' LIMIT 1)
),

-- Finance incident
('INCIDENT_CREATED', 'Finance employee reported billing mismatch for vendor invoice.', NOW(6) - INTERVAL 2 DAY,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Billing discrepancy%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='fin_emp1@company.com' LIMIT 1)
),
('TASK_CREATED', 'Reconciliation task created to validate invoice vs cost centers.', NOW(6) - INTERVAL 40 HOUR,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Billing discrepancy%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='fin_emp1@company.com' LIMIT 1)
),
('TASK_ASSIGNED', 'Finance manager assigned reconciliation task to employee.', NOW(6) - INTERVAL 39 HOUR + INTERVAL 58 MINUTE,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Billing discrepancy%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='fin_mgr@company.com' LIMIT 1)
),
('TASK_STATUS_CHANGED', 'Employee marked task COMPLETED after reconciliation.', NOW(6) - INTERVAL 12 HOUR,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Billing discrepancy%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='fin_emp1@company.com' LIMIT 1)
),

-- Operations incident
('INCIDENT_CREATED', 'Operations employee reported batch processing delay.', NOW(6) - INTERVAL 5 HOUR,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Batch processing delay%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='ops_emp1@company.com' LIMIT 1)
),
('TASK_CREATED', 'Task created to identify failing stage and rerun batch.', NOW(6) - INTERVAL 4 HOUR,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Batch processing delay%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='ops_admin@company.com' LIMIT 1)
),
('TASK_ASSIGNED', 'Ops manager assigned batch fix task to employee.', NOW(6) - INTERVAL 4 HOUR + INTERVAL 2 MINUTE,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Batch processing delay%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='ops_mgr@company.com' LIMIT 1)
),

-- Procurement/Vendor incident
('INCIDENT_CREATED', 'Procurement employee reported vendor onboarding delay (missing docs).', NOW(6) - INTERVAL 20 HOUR,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Vendor onboarding delay%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='proc_emp1@company.com' LIMIT 1)
),
('TASK_CREATED', 'Task created to follow up supplier for documents.', NOW(6) - INTERVAL 12 HOUR,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Vendor onboarding delay%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='proc_admin@company.com' LIMIT 1)
),
('TASK_ASSIGNED', 'Procurement manager assigned follow-up task to employee.', NOW(6) - INTERVAL 12 HOUR + INTERVAL 1 MINUTE,
 (SELECT incident_id FROM incidents WHERE description LIKE 'Vendor onboarding delay%' ORDER BY incident_id DESC LIMIT 1),
 (SELECT user_id FROM users WHERE email='proc_mgr@company.com' LIMIT 1)
);
