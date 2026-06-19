
CREATE DATABASE boxcontroll_billing_db;
-- ============================================================================
-- 1. TABLE: payment_methods (Billetera de métodos de pago del cliente)
-- ============================================================================
CREATE TABLE payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL, -- 🔒 Aislamiento SaaS
    member_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL, -- 'cash', 'transfer', 'credit_card' [cite: 17]
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_methods_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_methods_member FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE CASCADE
);

CREATE INDEX idx_payment_methods_tenant_member ON payment_methods (tenant_id, member_id);

-- ============================================================================
-- 2. TABLE: invoices (Las Cuentas / Cuotas por cobrar - EL PUENTE CRÍTICO)
-- ============================================================================
CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL, -- 🔒 Aislamiento SaaS
    membership_id UUID NOT NULL, -- Vínculo al contrato original 
    member_id UUID NOT NULL,
    invoice_number VARCHAR(50) NOT NULL, -- Ej: FACT-2026-0001
    amount DECIMAL(10,2) NOT NULL, -- Monto que DEBE pagar en esta cuota
    due_date DATE NOT NULL, -- Fecha de vencimiento de ESTA cuota mensual 
    status VARCHAR(20) DEFAULT 'unpaid', -- 'unpaid' (pendiente), 'paid' (pagada), 'overdue' (vencida) 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_invoices_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE,
    CONSTRAINT fk_invoices_membership FOREIGN KEY (membership_id) REFERENCES memberships (id) ON DELETE CASCADE,
    CONSTRAINT fk_invoices_member FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE CASCADE,
    CONSTRAINT uq_tenant_invoice_num UNIQUE (tenant_id, invoice_number)
);

CREATE INDEX idx_invoices_tenant_status ON invoices (tenant_id, status);
CREATE INDEX idx_invoices_due_date ON invoices (due_date);

-- ============================================================================
-- 3. TABLE: payments (El dinero real que ingresa a caja)
-- ============================================================================
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL, -- 🔒 Aislamiento SaaS
    invoice_id UUID, -- NULL si es un pago directo, o vinculado a la cuota que está matando
    membership_id UUID NOT NULL,
    member_id UUID NOT NULL,
    amount DECIMAL(10,2) NOT NULL, -- Monto efectivamente pagado 
    currency VARCHAR(3) DEFAULT 'PEN',
    payment_method VARCHAR(50) NOT NULL, -- 'cash', 'transfer' [cite: 17, 54]
    payment_date DATE NOT NULL, [cite: 54]
    reference_number VARCHAR(100), -- 🔍 Nro de operación bancaria para control del admin [cite: 19, 54]
    status VARCHAR(20) DEFAULT 'completed', -- 'completed', 'pending', 'failed' [cite: 56]
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payments_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id) ON DELETE SET NULL,
    CONSTRAINT fk_payments_membership FOREIGN KEY (membership_id) REFERENCES memberships (id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_member FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE CASCADE
);

CREATE INDEX idx_payments_tenant_date ON payments (tenant_id, payment_date);
CREATE INDEX idx_payments_reference ON payments (reference_number);

select * from membership_plans