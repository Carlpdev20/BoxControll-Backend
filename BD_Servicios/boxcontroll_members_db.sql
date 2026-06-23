-- 1. tenants: Contenedor lógico aislado para cada gimnasio cliente[cite: 26].
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_tenants_slug ON tenants (slug);

-- 2. membership_plans: Planes ofrecidos por cada gimnasio de forma independiente[cite: 35].
CREATE TABLE membership_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL, -- Aislamiento multi-tenant [cite: 27]
    name VARCHAR(100) NOT NULL,
    description TEXT,
    duration_days INTEGER NOT NULL, -- Determina los días de vigencia del acceso [cite: 39]
    price DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'PEN',
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_membership_plans_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE,
    CONSTRAINT uq_tenant_plan_name UNIQUE (tenant_id, name)
);
CREATE INDEX idx_membership_plans_tenant ON membership_plans (tenant_id);

-- 3. members: Datos personales de los deportistas locales[cite: 41].
CREATE TABLE members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL, -- Aislamiento multi-tenant [cite: 27]
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    document_type VARCHAR(20),
    document_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'active', -- active, inactive, suspended [cite: 43]
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_members_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE,
    CONSTRAINT uq_tenant_document UNIQUE (tenant_id, document_number) -- Permite DNI repetido en distintos gimnasios [cite: 44]
);
CREATE INDEX idx_members_tenant_id ON members (tenant_id);
CREATE INDEX idx_members_document ON members (document_number);

ALTER TABLE members 
ADD COLUMN plan VARCHAR(50),
ADD COLUMN expires_at TIMESTAMP;

-- 4. memberships: Historial de afiliaciones y ciclos de acceso contratados[cite: 46].
CREATE TABLE memberships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL, -- Aislamiento multi-tenant [cite: 27]
    member_id UUID NOT NULL,
    membership_plan_id UUID NOT NULL,
    affiliation_date DATE NOT NULL, -- Registro administrativo [cite: 50]
    start_date DATE NOT NULL, -- Inicio del acceso físico [cite: 50]
    end_date DATE NOT NULL, -- Calculado automáticamente según duration_days [cite: 51]
    status VARCHAR(20) DEFAULT 'active', -- active, cancelled, renewed, expired [cite: 48]
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_memberships_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE,
    CONSTRAINT fk_memberships_member FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE CASCADE,
    CONSTRAINT fk_memberships_plan FOREIGN KEY (membership_plan_id) REFERENCES membership_plans (id)
);
CREATE INDEX idx_memberships_tenant ON memberships (tenant_id);
CREATE INDEX idx_memberships_status ON memberships (status);

-- 5. renewal_reminders: Registro de alertas pre-vencimiento[cite: 58].
CREATE TABLE renewal_reminders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    membership_id UUID NOT NULL,
    member_id UUID NOT NULL,
    reminder_date DATE NOT NULL, -- Fijado 14 días antes del fin de la membresía [cite: 59]
    sent BOOLEAN DEFAULT false, -- Evita notificaciones duplicadas [cite: 60]
    contact_method VARCHAR(50) DEFAULT 'whatsapp', -- Canal de envío [cite: 61]
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_reminders_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE,
    CONSTRAINT fk_reminders_membership FOREIGN KEY (membership_id) REFERENCES memberships (id) ON DELETE CASCADE,
    CONSTRAINT fk_reminders_member FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE CASCADE
);

use boxcontroll_members_db