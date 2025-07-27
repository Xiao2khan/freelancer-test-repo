// Handles all emails to *@ea.reai.no catch-all email route

import PostalMime from 'postal-mime';

const MAX_FILE_SIZE = 25 * 1024 * 1024; // 25 MB
export default {
    /** Cloudflare Email Worker entry-point */
    async email(message, env, ctx) {
        console.debug("Received email");
        try {
            if (message.rawSize > MAX_FILE_SIZE * 2) {
                console.log(`Email too large: ${message.rawSize} bytes`);
                return;
            }

            // Parse the raw RFC-822 message
            const rawEmail = await new Response(message.raw).arrayBuffer();
            const email = await PostalMime.parse(rawEmail, {attachmentEncoding: 'base64'});

            const companySlug = (message.to || '').split('@')[0].toLowerCase();

            for (const attachment of email.attachments ?? []) {
                const fileData = attachment.content;                // already base64
                if (!fileData) continue;

                const res = await fetch(`https://app.reai.no/api/voucher-reception`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-Tenant-Slug': companySlug,
                        'X-Email-Worker-Token': "learnEveryDay!",
                    },
                    body: JSON.stringify({
                        filename: attachment.filename || 'unnamed_file',
                        mimeType: attachment.mimeType || 'application/octet-stream',
                        fileData,
                        senderEmail: message.from
                    })
                });

                if (!res.ok) {
                    throw new Error(`API request failed: ${res.status} ${res.statusText}`);
                }
                await console.log(`Uploaded ${{
                    filename: attachment.filename || 'unnamed_file',
                    mimeType: attachment.mimeType || 'application/octet-stream',
                    senderEmail: message.from
                }.filename} for ${companySlug}`);
            }
        } catch (err) {
            console.error('Error processing email:', err);
        }
    },

    /** Simple health-check for HTTP requests */
    async fetch(request) {
        if (request.method === 'GET' && new URL(request.url).pathname === '/health') {
            return new Response('OK', {status: 200});
        }
        return new Response('Email worker active: index.js published and processing emails', {status: 200});
    }
};

