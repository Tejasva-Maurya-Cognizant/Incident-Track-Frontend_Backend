INSERT IGNORE INTO incident_sla_breach
(incident_id, sla_due_at, breached_at, breach_minutes, breach_status, reason)
VALUES
(
 (SELECT incident_id FROM incidents WHERE description LIKE 'Possible phishing%' ORDER BY incident_id DESC LIMIT 1),
 NOW(6) - INTERVAL 30 MINUTE,
 NOW(6) - INTERVAL 15 MINUTE,
 15,
 'OPEN',
 'SLA exceeded before resolution'
),
(
 (SELECT incident_id FROM incidents WHERE description LIKE 'Billing discrepancy%' ORDER BY incident_id DESC LIMIT 1),
 NOW(6) - INTERVAL 18 HOUR,
 NOW(6) - INTERVAL 12 HOUR,
 360,
 'RESOLVED',
 'SLA breached but incident later resolved'
);
