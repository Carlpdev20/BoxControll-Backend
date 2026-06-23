CREATE INDEX idx_tenants_slug ON tenants (slug);

CREATE INDEX idx_membership_plans_tenant ON membership_plans (tenant_id);

CREATE INDEX idx_members_tenant_id ON members (tenant_id);
CREATE INDEX idx_members_document ON members (document_number);

CREATE INDEX idx_memberships_tenant ON memberships (tenant_id);
CREATE INDEX idx_memberships_status ON memberships (status);