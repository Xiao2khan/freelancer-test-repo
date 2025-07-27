-- Create the voucher attachments table
CREATE TABLE IF NOT EXISTS voucher_attachments
(
    id            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    voucher_id    BIGINT      NOT NULL,
    attachment_id INTEGER     NOT NULL,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id     INTEGER     NOT NULL,
    CONSTRAINT fk_voucher_attachments_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers (id) ON DELETE CASCADE,
    CONSTRAINT fk_voucher_attachments_attachment FOREIGN KEY (attachment_id) REFERENCES attachments (id) ON DELETE CASCADE,
    CONSTRAINT fk_voucher_attachments_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

-- Create indexes for voucher attachments table
CREATE INDEX idx_voucher_attachments_voucher_id ON voucher_attachments (voucher_id);
CREATE INDEX idx_voucher_attachments_tenant_id ON voucher_attachments (tenant_id); 