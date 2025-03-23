INSERT INTO roles (id, name, description, created_at, updated_at)
VALUES 
    (uuid_generate_v4(), 'ROLE_ADMIN', 'Administrador do sistema', NOW(), NOW()),
    (uuid_generate_v4(), 'ROLE_MANAGER', 'Gerente da loja', NOW(), NOW()),
    (uuid_generate_v4(), 'ROLE_CUSTOMER', 'Cliente da loja', NOW(), NOW()); 