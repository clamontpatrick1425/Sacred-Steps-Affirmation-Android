import * as admin from "firebase-admin";
import * as functions from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";
import { defineSecret } from "firebase-functions/params";
import { Resend } from "resend";

admin.initializeApp();

const resendApiKey = defineSecret("RESEND_API_KEY");
const resendAudienceId = defineSecret("RESEND_AUDIENCE_ID");
const resendFromEmail = defineSecret("RESEND_FROM_EMAIL");
const convertKitApiKey = defineSecret("CONVERTKIT_API_KEY");
const convertKitFormId = defineSecret("CONVERTKIT_FORM_ID");
const mailchimpApiKey = defineSecret("MAILCHIMP_API_KEY");
const mailchimpServerPrefix = defineSecret("MAILCHIMP_SERVER_PREFIX");
const mailchimpListId = defineSecret("MAILCHIMP_LIST_ID");

interface SignupRequestBody {
  name?: string;
  email?: string;
  esp_provider?: string;
}

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export const handleAppSignup = functions.onRequest(
  {
    region: "us-central1",
    secrets: [
      resendApiKey,
      resendAudienceId,
      resendFromEmail,
      convertKitApiKey,
      convertKitFormId,
      mailchimpApiKey,
      mailchimpServerPrefix,
      mailchimpListId,
    ],
  },
  async (req, res) => {
    res.set("Access-Control-Allow-Origin", "*");
    res.set("Access-Control-Allow-Methods", "POST, OPTIONS");
    res.set("Access-Control-Allow-Headers", "Content-Type");

    if (req.method === "OPTIONS") {
      res.status(204).send("");
      return;
    }

    if (req.method !== "POST") {
      res.status(403).send("Forbidden");
      return;
    }

    try {
      const { name, email, esp_provider } = (req.body ?? {}) as SignupRequestBody;
      const cleanEmail = (email ?? "").trim().toLowerCase();
      const cleanName = (name ?? "").trim() || "Seeker";
      const provider = (esp_provider ?? "Resend").trim();

      if (!cleanEmail || !EMAIL_REGEX.test(cleanEmail)) {
        res.status(400).json({ success: false, error: "Valid email is required" });
        return;
      }

      logger.info(`Processing Sacred Steps signup for: ${cleanEmail} via ${provider}`);

      if (provider === "ConvertKit") {
        await subscribeConvertKit(cleanEmail, cleanName);
      } else if (provider === "Mailchimp") {
        await subscribeMailchimp(cleanEmail, cleanName);
      } else {
        await subscribeResend(cleanEmail, cleanName);
      }

      res.status(200).json({
        success: true,
        provider,
        message: "Welcome to the Sacred Steps community. Your journey continues.",
      });
    } catch (err) {
      logger.error("Function execution error:", err);
      res.status(500).json({ success: false, error: "Internal server error" });
    }
  }
);

async function subscribeResend(email: string, firstName: string): Promise<void> {
  const apiKey = resendApiKey.value() || process.env.RESEND_API_KEY || "";
  const audienceId = resendAudienceId.value() || process.env.RESEND_AUDIENCE_ID || "";
  const fromEmail =
    resendFromEmail.value() ||
    process.env.RESEND_FROM_EMAIL ||
    "Sacred Steps <onboarding@sacredsteps.com>";

  if (!apiKey) {
    throw new Error("RESEND_API_KEY is not configured.");
  }

  const resend = new Resend(apiKey);

  if (audienceId) {
    const { error } = await resend.contacts.create({
      email,
      firstName,
      audienceId,
      unsubscribed: false,
    });

    if (error) {
      logger.error("Resend Audience Error:", error);
      throw new Error("Failed to subscribe user to Resend audience");
    }
  }

  const { error: emailError } = await resend.emails.send({
    from: fromEmail,
    to: [email],
    subject: "Your 30-Day Redemption Prayer Guide · Sacred Steps",
    text: [
      `Welcome home, ${firstName}.`,
      "",
      "All 365 daily steps are now open to you in Sacred Steps to Redemption.",
      "Take one honest breath and step into today's grace.",
      "",
      "Zero spam. No shame.",
    ].join("\n"),
  });

  if (emailError) {
    logger.error("Resend Email Delivery Error:", emailError);
    throw new Error("Failed to deliver 30-Day Redemption Prayer Guide");
  }
}

async function subscribeConvertKit(email: string, firstName: string): Promise<void> {
  const apiKey = convertKitApiKey.value() || process.env.CONVERTKIT_API_KEY || "";
  const formId = convertKitFormId.value() || process.env.CONVERTKIT_FORM_ID || "";
  if (!apiKey || !formId) {
    throw new Error("ConvertKit secrets are not configured.");
  }

  const response = await fetch(
    `https://api.convertkit.com/v3/forms/${encodeURIComponent(formId)}/subscribe`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        api_key: apiKey,
        email,
        first_name: firstName,
      }),
    }
  );

  if (!response.ok) {
    const body = await response.text();
    throw new Error(`ConvertKit error ${response.status}: ${body}`);
  }
}

async function subscribeMailchimp(email: string, firstName: string): Promise<void> {
  const apiKey = mailchimpApiKey.value() || process.env.MAILCHIMP_API_KEY || "";
  const prefix = mailchimpServerPrefix.value() || process.env.MAILCHIMP_SERVER_PREFIX || "";
  const listId = mailchimpListId.value() || process.env.MAILCHIMP_LIST_ID || "";
  if (!apiKey || !prefix || !listId) {
    throw new Error("Mailchimp secrets are not configured.");
  }

  const authHeader = Buffer.from(`anystring:${apiKey}`).toString("base64");
  const response = await fetch(
    `https://${encodeURIComponent(prefix)}.api.mailchimp.com/3.0/lists/${encodeURIComponent(listId)}/members`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Basic ${authHeader}`,
      },
      body: JSON.stringify({
        email_address: email,
        status: "subscribed",
        merge_fields: { FNAME: firstName },
      }),
    }
  );

  if (!response.ok && response.status !== 400) {
    const body = await response.text();
    throw new Error(`Mailchimp error ${response.status}: ${body}`);
  }
}
