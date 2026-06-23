-- 1. TABLA: tenants
CREATE TABLE tenants (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(255) NOT NULL,
  slug VARCHAR(255) UNIQUE NOT NULL,
  email VARCHAR(255) NOT NULL,
  phone VARCHAR(20),
  address TEXT,
  city VARCHAR(100),
  country VARCHAR(100),
  active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tenants_slug ON tenants (slug);

CREATE UNIQUE INDEX idx_tenant_admins_tenant_email ON tenant_admins (tenant_id, email);
CREATE INDEX idx_tenant_admins_email ON tenant_admins (email);

ALTER TABLE tenant_admins 
  ADD CONSTRAINT fk_tenant_admins_tenant 
  FOREIGN KEY (tenant_id) REFERENCES tenants (id) 
  ON DELETE CASCADE;

  -- 1. Insertar el Gimnasio base (Tenant - Escenario PowerFit)
INSERT INTO tenants (id, name, slug, email, phone, address, city, country, active)
VALUES (
  'edf27f12-2d14-4e20-8041-e9dbdf200001', 
  'PowerFit Center', 
  'powerfit', 
  'contacto@powerfit.com', 
  '987654321', 
  'Av. Larco 123', 
  'Lima', 
  'Peru', 
  true
);

-- 2. Insertar el Administrador vinculado a PowerFit
-- NOTA: El password_hash de abajo corresponde exactamente a la clave "1234" encriptada en BCrypt
INSERT INTO tenant_admins (id, tenant_id, email, password_hash, first_name, last_name, active)
VALUES (
  'a4c49f38-bc02-4b21-995a-67c8a6600002', 
  'edf27f12-2d14-4e20-8041-e9dbdf200001', 
  'admin@powerfit.com', 
  '$2a$12$US00g/uMhoSBm.HiuieBjeMtoN69SN.GE25fCpldebzkryUyopws6', 
  'Carlos', 
  'Pérez', 
  true
);