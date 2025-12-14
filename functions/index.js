const { onCall } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const sendMail = require("./sendMail");
require("dotenv").config();

// Initialize Firebase Admin once
admin.initializeApp({
  credential: admin.credential.applicationDefault(),
    storageBucket: "rentwave-ica.firebasestorage.app"
});


exports.createTenant = onCall(async (request) => {
  console.log("📩 createTenant called:", request.data);

  const {
    email,
    firstName,
    lastName,
    phone,
    tempPassword,
    propertyId,
    landlordId,
    rentAmount,
    rentStartDate,
    nextRentDate,
    propertyName,
  } = request.data || {};

  // 🔴 Only validate the truly required fields
  if (!email || !tempPassword || !propertyId) {
    console.error("❌ Missing required fields (email, password, or propertyId)");
    throw new Error("Missing required fields");
  }

  try {
    // 1️⃣ Create Firebase Auth User for Tenant
    const userRecord = await admin.auth().createUser({
      email,
      password: tempPassword,
    });

    const tenantId = userRecord.uid;
    const db = admin.firestore();

    // 2️⃣ Save Tenant Data to Firestore (includes rent + property info)
    const tenantData = {
      firstName: firstName || "",
      lastName: lastName || "",
      email,
      phone: phone || "",
      propertyId,
      propertyName: propertyName || "",
      landlordId: landlordId || "", // if null, store empty string instead of failing
      rentAmount: rentAmount || "",
      rentStartDate: rentStartDate || "",
      nextRentDate: nextRentDate || "",
      role: "tenant",
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    };

    await db.collection("tenants").doc(tenantId).set(tenantData);
    await db.collection("users").doc(tenantId).set(tenantData);

    console.log("🔥 Tenant saved to Firestore:", tenantId);

    // 3️⃣ Send Email via your Nodemailer Function (YOUR TEMPLATE KEPT 100%)
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

    // Try sending email, but don't fail the whole function if email fails
    try {
      await sendMail({
        to: email,
        subject: "RentWave Tenant Login Details",
        html: htmlContent,
      });
      console.log("📨 Email sent successfully");
    } catch (emailErr) {
      console.error("⚠ Failed to send email, but tenant was created:", emailErr);
    }

    return { success: true, tenantId };

  } catch (error) {
    console.error("❌ Error creating tenant:", error);
    throw new Error(error.message || "Unknown error creating tenant");
  }
});



exports.sendPaymentReceipt = require("./sendPaymentReceipt").sendPaymentReceipt;
