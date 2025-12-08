const { onCall } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const sgMail = require("@sendgrid/mail");
require("dotenv").config();

// 🔹 Initialise Firebase Admin (once)
if (admin.apps.length === 0) {
  admin.initializeApp();
}

// 🔹 Read SendGrid API key from .env
//    Accepts either SENDGRID_API_KEY or SENDGRID_KEY so we don't get stuck on names
const SENDGRID_KEY =
  process.env.SENDGRID_API_KEY || process.env.SENDGRID_KEY || "";

if (!SENDGRID_KEY) {
  console.error(
    "⚠️ No SendGrid key found. Make sure .env contains SENDGRID_API_KEY=... or SENDGRID_KEY=..."
  );
} else {
  sgMail.setApiKey(SENDGRID_KEY);
  console.log("✅ SendGrid initialised. Key length:", SENDGRID_KEY.length);
}

// 👉 This is the function your Android app calls: "createTenant"
exports.createTenant = onCall(async (request) => {
  console.log(
    "📩 createTenant called with data:",
    JSON.stringify(request.data || {}, null, 2)
  );

  const {
    email,
    firstName,
    lastName,
    phone,
    tempPassword,
    propertyId,
    landlordId,
  } = request.data || {};

  // Basic validation
  if (!email || !tempPassword || !propertyId || !landlordId) {
    console.error("❌ Missing required fields");
    return { success: false, error: "Missing required fields" };
  }

  try {
    // 1️⃣ CREATE Firebase Authentication user for tenant
    const userRecord = await admin.auth().createUser({
      email,
      password: tempPassword,
    });

    const tenantId = userRecord.uid;
    console.log("✅ Auth user created:", tenantId);

    // 2️⃣ SAVE tenant data in Firestore (tenants + users)
    const tenantData = {
      firstName: firstName || "",
      lastName: lastName || "",
      email,
      phone: phone || "",
      propertyId,
      landlordId,
      role: "tenant",
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    };

    const db = admin.firestore();

    await db.collection("tenants").doc(tenantId).set(tenantData);
    await db.collection("users").doc(tenantId).set(tenantData);
    console.log("✅ Firestore docs written for tenant:", tenantId);

    // 3️⃣ SEND EMAIL — your HTML template kept
    const htmlContent = `
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Tenant Onboarding - RentWave</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            color: #333333;
            background-color: #f0f4f8; /* Light background for contrast */
            margin: 0;
            padding: 0;
        }
        .container {
            width: 80%;
            margin: 20px auto;
            padding: 20px;
            border: 1px solid #d0dbe1;
            border-radius: 10px;
            box-shadow: 0 0 15px rgba(0, 0, 0, 0.1);
            background-color: #f4f4f4; /* Clean white background */
        }
        .header {
            background: #5F92DF; /* Royal Blue */
            padding: 15px;
            display: flex;
            align-items: center; /* Align items vertically */
            justify-content: center; /* Center content horizontally */
            position: relative; /* Allows positioning of the logo */
            border-bottom: 2px solid #5F92DF; /* Darker shade of Royal Blue */
            color: #f4f4f4;
            border-radius: 10px 10px 0 0; /* Rounded top corners */
        }
        .header img {
            width: 120px;
            height: 100px;
            object-fit: contain;
            position: absolute;
            left: 15px; /* Position logo on the left */
        }
        .content {
            padding: 20px;
            color: #333333;
        }
        .footer {
            background: #5F92DF; /* Darker shade of Royal Blue */
            padding: 15px;
            text-align: center;
            border-top: 2px solid #5F92DF; /* Royal Blue */
            font-size: 0.9em;
            color: #f4f4f4;
            border-radius: 0 0 10px 10px; /* Rounded bottom corners */
        }
        .button {
            display: inline-block;
            background-color: #5F92DF; /* Royal Blue */
            color: #f4f4f4;
            padding: 12px 24px;
            text-decoration: none;
            border-radius: 5px;
            font-weight: bold;
            transition: background-color 0.3s ease;
        }
        .button:hover {
            background-color: #002a80; /* Darker shade of Royal Blue */
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <img src="https://rent-wave.vercel.app/assets/logo-D2c4he43.png" alt="RentWave Logo">
            <h1>Welcome to RENTWAVE!</h1>
        </div>
        <div class="content">
            <p>Dear ${firstName || ""} ${lastName || ""},</p>
            <p>Welcome to RentWave! We are excited to inform you that you have been successfully onboarded as a tenant in our community.</p>
            <p>Your account has been created, and you can now access your RentWave tenant portal using the login credentials below:</p>
            <p><strong>Email:</strong> ${email}</p>
            <p><strong>Password:</strong> ${tempPassword}</p>
            <p>For your security, please change your password upon your first login to keep your account secure.</p>
            <p>If you have any questions or need assistance, our support team is here to help you at any time.</p>
            <p>Thank you for choosing RentWave. We look forward to making your rental experience smooth and enjoyable.</p>
            <p>Best regards,<br>RentWave Team</p>
        </div>
        <div class="footer">
            <p>&copy; ${new Date().getFullYear()} RentWave. All rights reserved.</p>
        </div>
    </div>
</body>
</html>
`;

    console.log("🚀 Sending email via SendGrid to:", email);
    await sgMail.send({
      to: email,
      from: "oghenetegaokotie@gmail.com", // your verified single sender
      subject: "Your RentWave Login Details",
      html: htmlContent,
    });
    console.log("✅ Email sent successfully to:", email);

    return { success: true };
  } catch (error) {
    console.error("❌ Error in createTenant:", error);
    if (error.response && error.response.body) {
      console.error(
        "SendGrid response body:",
        JSON.stringify(error.response.body)
      );
    }
    return { success: false, error: error.message || "Unknown error" };
  }
});
