-- Add tenant_id column to attachments table
ALTER TABLE attachments
    ADD COLUMN tenant_id INTEGER;

-- Add foreign key constraint to tenant_id
ALTER TABLE attachments
    ADD CONSTRAINT fk__attachments__tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants (id)
            ON DELETE CASCADE;
