INSERT INTO permissions (name, code, description, created_at, updated_at) VALUES
     ('Company Create', 'company:create', 'Create company', NOW(), NOW()),
     ('Company Read', 'company:read', 'Only read the data of the company they belong to', NOW(), NOW());

-- Insert tenant roles
INSERT INTO roles (name, code, description, created_at, updated_at) VALUES
   ('Registered User', 'REGISTERED_USER', 'Create company, create and manage sub user', NOW(), NOW()),
   ('Sub User', 'SUB_USER', 'Access with limit permission, can not create company and new user', NOW(), NOW());


-- Link roles to permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
  ((SELECT id FROM roles WHERE code = 'REGISTERED_USER'), (SELECT id FROM permissions WHERE code = 'company:create')),
  ((SELECT id FROM roles WHERE code = 'SUB_USER'), (SELECT id FROM permissions WHERE code = 'company:read'));


-- Link all current users with role REGISTERED_USER
INSERT INTO user_roles (role_id, user_id)
SELECT r.id, u.id
FROM users u, roles r WHERE r.code = 'REGISTERED_USER';
