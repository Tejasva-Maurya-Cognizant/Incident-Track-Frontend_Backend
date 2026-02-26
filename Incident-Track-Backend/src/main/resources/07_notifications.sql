INSERT IGNORE INTO notification
(user_id, type, message, status, created_date_time)
VALUES
(
 (SELECT user_id FROM users WHERE email='it_mgr@company.com' LIMIT 1),
 'INCIDENT_REPORTED',
 'New incident reported: VPN not connecting.',
 'UNREAD',
 NOW(6) - INTERVAL 5 HOUR
),
(
 (SELECT user_id FROM users WHERE email='sec_mgr@company.com' LIMIT 1),
 'CRITICAL_INCIDENT_ALERT',
 'Critical incident reported: Possible phishing.',
 'UNREAD',
 NOW(6) - INTERVAL 2 HOUR
),
(
 (SELECT user_id FROM users WHERE email='fac_mgr@company.com' LIMIT 1),
 'TASK_ASSIGNED',
 'Task assigned: Stabilize power in wing B.',
 'READ',
 NOW(6) - INTERVAL 90 MINUTE
),
(
 (SELECT user_id FROM users WHERE email='hr_mgr@company.com' LIMIT 1),
 'INCIDENT_REPORTED',
 'New incident reported: Onboarding delay.',
 'UNREAD',
 NOW(6) - INTERVAL 20 HOUR
),
(
 (SELECT user_id FROM users WHERE email='fin_mgr@company.com' LIMIT 1),
 'INCIDENT_RESOLVED',
 'Incident resolved: Billing discrepancy reconciled.',
 'READ',
 NOW(6) - INTERVAL 10 HOUR
),
(
 (SELECT user_id FROM users WHERE email='ops_mgr@company.com' LIMIT 1),
 'TASK_ASSIGNED',
 'Task assigned: Fix batch processing delay.',
 'UNREAD',
 NOW(6) - INTERVAL 4 HOUR
),
(
 (SELECT user_id FROM users WHERE email='proc_mgr@company.com' LIMIT 1),
 'INCIDENT_REPORTED',
 'New incident reported: Vendor onboarding delay.',
 'UNREAD',
 NOW(6) - INTERVAL 12 HOUR
);
