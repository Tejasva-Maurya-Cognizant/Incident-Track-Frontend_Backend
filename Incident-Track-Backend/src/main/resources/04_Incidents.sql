INSERT IGNORE INTO incidents
(calculated_severity, description, is_critical, reported_date, sla_breached, sla_due_at, status, category_id, reported_by)
VALUES
-- IT
('MEDIUM','VPN not connecting; login loops on authentication.', b'0',
 NOW(6) - INTERVAL 6 HOUR, b'0',
 NOW(6) + INTERVAL 18 HOUR, 'OPEN',
 (SELECT category_id FROM category WHERE category_name='IT / Technical' AND sub_category='VPN Access Failure' LIMIT 1),
 (SELECT user_id FROM users WHERE email='it_emp1@company.com' LIMIT 1)
),

-- Security
('CRITICAL','Possible phishing: user clicked link, browser redirected unexpectedly.', b'1',
 NOW(6) - INTERVAL 3 HOUR, b'0',
 NOW(6) + INTERVAL 3 HOUR, 'IN_PROGRESS',
 (SELECT category_id FROM category WHERE category_name='Security' AND sub_category='Phishing Attack' LIMIT 1),
 (SELECT user_id FROM users WHERE email='sec_emp1@company.com' LIMIT 1)
),

-- Facilities
('HIGH','Power flickering in wing B; some desks losing power intermittently.', b'1',
 NOW(6) - INTERVAL 2 HOUR, b'0',
 NOW(6) + INTERVAL 6 HOUR, 'OPEN',
 (SELECT category_id FROM category WHERE category_name='Infrastructure' AND sub_category='Power Failure' LIMIT 1),
 (SELECT user_id FROM users WHERE email='fac_emp1@company.com' LIMIT 1)
),

-- HR
('LOW','Onboarding delay: laptop not provisioned for new joiner.', b'0',
 NOW(6) - INTERVAL 1 DAY, b'0',
 NOW(6) + INTERVAL 1 DAY, 'OPEN',
 (SELECT category_id FROM category WHERE category_name='HR / Policy' AND sub_category='Onboarding Delay' LIMIT 1),
 (SELECT user_id FROM users WHERE email='hr_emp1@company.com' LIMIT 1)
),

-- Finance
('LOW','Billing discrepancy in latest vendor invoice; needs reconciliation.', b'0',
 NOW(6) - INTERVAL 2 DAY, b'0',
 NOW(6) + INTERVAL 1 DAY, 'IN_PROGRESS',
 (SELECT category_id FROM category WHERE category_name='Finance' AND sub_category='Billing Discrepancy' LIMIT 1),
 (SELECT user_id FROM users WHERE email='fin_emp1@company.com' LIMIT 1)
),

-- Operations
('MEDIUM','Batch processing delay affecting daily workflow completion.', b'0',
 NOW(6) - INTERVAL 5 HOUR, b'0',
 NOW(6) + INTERVAL 15 HOUR, 'OPEN',
 (SELECT category_id FROM category WHERE category_name='Operations' AND sub_category='Batch Processing Failure' LIMIT 1),
 (SELECT user_id FROM users WHERE email='ops_emp1@company.com' LIMIT 1)
),

-- Procurement / Vendor
('MEDIUM','Vendor onboarding delay: missing documents from supplier.', b'0',
 NOW(6) - INTERVAL 20 HOUR, b'0',
 NOW(6) + INTERVAL 20 HOUR, 'OPEN',
 (SELECT category_id FROM category WHERE category_name='Vendor' AND sub_category='Vendor Onboarding Delay' LIMIT 1),
 (SELECT user_id FROM users WHERE email='proc_emp1@company.com' LIMIT 1)
);
