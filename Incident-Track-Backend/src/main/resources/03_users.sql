INSERT IGNORE INTO users (username, email, password, department_id, role, status) VALUES
-- Dept 1: IT
('it_admin', 'it_admin@company.com', '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 1, 'ADMIN', 'ACTIVE'),
('it_mgr',   'it_mgr@company.com',   '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 1, 'MANAGER','ACTIVE'),
('it_emp1',  'it_emp1@company.com',  '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 1, 'EMPLOYEE','ACTIVE'),
('it_emp2',  'it_emp2@company.com',  '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 1, 'EMPLOYEE','ACTIVE'),

-- Dept 2: Security
('sec_admin','sec_admin@company.com','$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 2, 'ADMIN','ACTIVE'),
('sec_mgr',  'sec_mgr@company.com',  '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 2, 'MANAGER','ACTIVE'),
('sec_emp1', 'sec_emp1@company.com', '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 2, 'EMPLOYEE','ACTIVE'),

-- Dept 3: Legal & Compliance
('comp_admin','comp_admin@company.com','$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 3, 'ADMIN','ACTIVE'),
('comp_mgr',  'comp_mgr@company.com', '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 3, 'MANAGER','ACTIVE'),
('comp_emp1', 'comp_emp1@company.com','$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 3, 'EMPLOYEE','ACTIVE'),

-- Dept 4: Facilities
('fac_admin','fac_admin@company.com','$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 4, 'ADMIN','ACTIVE'),
('fac_mgr',  'fac_mgr@company.com',  '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 4, 'MANAGER','ACTIVE'),
('fac_emp1', 'fac_emp1@company.com', '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 4, 'EMPLOYEE','ACTIVE'),

-- Dept 5: HR
('hr_admin','hr_admin@company.com','$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 5, 'ADMIN','ACTIVE'),
('hr_mgr',  'hr_mgr@company.com',  '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 5, 'MANAGER','ACTIVE'),
('hr_emp1', 'hr_emp1@company.com', '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 5, 'EMPLOYEE','ACTIVE'),

-- Dept 6: Finance
('fin_admin','fin_admin@company.com','$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 6, 'ADMIN','ACTIVE'),
('fin_mgr',  'fin_mgr@company.com',  '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 6, 'MANAGER','ACTIVE'),
('fin_emp1', 'fin_emp1@company.com', '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 6, 'EMPLOYEE','ACTIVE'),

-- Dept 7: Business Ops
('ops_admin','ops_admin@company.com','$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 7, 'ADMIN','ACTIVE'),
('ops_mgr',  'ops_mgr@company.com',  '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 7, 'MANAGER','ACTIVE'),
('ops_emp1', 'ops_emp1@company.com', '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 7, 'EMPLOYEE','ACTIVE'),

-- Dept 8: Procurement
('proc_admin','proc_admin@company.com','$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 8, 'ADMIN','ACTIVE'),
('proc_mgr',  'proc_mgr@company.com', '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 8, 'MANAGER','ACTIVE'),
('proc_emp1', 'proc_emp1@company.com','$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 8, 'EMPLOYEE','ACTIVE'),

-- Dept 9: General Admin
('ga_admin','ga_admin@company.com','$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 9, 'ADMIN','ACTIVE'),
('ga_mgr',  'ga_mgr@company.com',  '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 9, 'MANAGER','ACTIVE'),
('ga_emp1', 'ga_emp1@company.com', '$2a$10$mK391QnRY0GnPe.Ef6aZo.pXyvSuYStxCpkIwoVg5pKfdNkUeVK26', 9, 'EMPLOYEE','ACTIVE');
