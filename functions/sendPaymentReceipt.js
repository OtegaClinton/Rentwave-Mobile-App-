const admin = require("firebase-admin");
const { onCall } = require("firebase-functions/v2/https");
const sendMail = require("./sendMail");
const PDFDocument = require("pdfkit");
const { PassThrough } = require("stream");
const axios = require("axios"); 

// 🔹 FIX: Initialize Admin SDK once
if (!admin.apps.length) {
  admin.initializeApp({
    storageBucket: "rentwave-ica.firebasestorage.app"
  });
}

const logoUrl = "https://rent-wave.vercel.app/assets/logo-D2c4he43.png";
const stampUrl = "https://res.cloudinary.com/dobng9jwd/image/upload/v1726822949/Untitled_design-removebg-preview_fgtbmg.png";

exports.sendPaymentReceipt = onCall(async (request) => {
  const { paymentId } = request.data || {};

  if (!paymentId) throw new Error("paymentId is required");

  const db = admin.firestore();
  const paymentSnap = await db.collection("payments").doc(paymentId).get();
  if (!paymentSnap.exists) throw new Error("Payment not found");

  const paymentData = paymentSnap.data();
  const { userId, amount, timestamp, landlordId } = paymentData;

  const tenantDoc = await db.collection("users").doc(userId).get();
  const tenant = tenantDoc.data();
  const tenantEmail = tenant.email;
  const tenantName = `${tenant.firstName} ${tenant.lastName}`;

  let landlordName = "Unknown";
  if (landlordId) {
    const landlordDoc = await db.collection("users").doc(landlordId).get();
    if (landlordDoc.exists)
      landlordName = `${landlordDoc.data().firstName} ${landlordDoc.data().lastName}`;
  }

  const tenantData = await db.collection("tenants").doc(userId).get();
  const tenantDetails = tenantData.data() || {};
  const propertyName = tenantDetails.propertyName || "Not Assigned";
  const rentStartDate = tenantDetails.rentStartDate || "";
  const nextRentDate = tenantDetails.nextRentDate || "";

  const logoBuffer = (await axios.get(logoUrl, { responseType: "arraybuffer" })).data;
  const stampBuffer = (await axios.get(stampUrl, { responseType: "arraybuffer" })).data;

  const pdfBuffer = await new Promise((resolve) => {
  const doc = new PDFDocument({ size: "A4", margin: 50 });
  const stream = new PassThrough();
  const chunks = [];

  stream.on("data", (chunk) => chunks.push(chunk));
  stream.on("end", () => resolve(Buffer.concat(chunks)));

  doc.pipe(stream);

  // -----------------------------------------------------
  // LOGO (top-left)
  // -----------------------------------------------------
  doc.image(logoBuffer, 50, 30, { width: 120 });

  // Move content *below* the logo safely (prevents overlap)
  doc.y = 150;

  // -----------------------------------------------------
  // TITLE SECTION
  // -----------------------------------------------------
  doc
    .fontSize(24)
    .text("RENT PAYMENT RECEIPT", { align: "center" })
    .moveDown(1.5);

  // -----------------------------------------------------
  // BASIC RECEIPT INFO
  // -----------------------------------------------------
  doc
    .fontSize(12)
    .text(`Receipt No: ${paymentId}`)
    .text(`Date Issued: ${new Date(timestamp.toDate()).toLocaleString()}`)
    .moveDown(1);

  // Horizontal line separator
  doc
    .moveTo(50, doc.y)
    .lineTo(550, doc.y)
    .stroke()
    .moveDown(1.5);

  // -----------------------------------------------------
  // TENANT DETAILS
  // -----------------------------------------------------
  doc
    .fontSize(14)
    .text("Tenant Details", { underline: true })
    .moveDown(0.8);

  doc
    .fontSize(12)
    .text(`Tenant Name: ${tenantName}`)
    .text(`Landlord: ${landlordName}`)
    .text(`Property: ${propertyName}`)
    .moveDown(1);

  // -----------------------------------------------------
  // PAYMENT INFORMATION
  // -----------------------------------------------------
  doc
    .fontSize(14)
    .text("Payment Information", { underline: true })
    .moveDown(0.8);

  doc
    .fontSize(12)
    .text(`Rent Start Date: ${rentStartDate}`)
    .text(`Next Rent Due: ${nextRentDate}`)
    .text(`Amount Paid: £${amount}`)
    .text(`Payment Status: Successful`)
    .moveDown(2);

  // -----------------------------------------------------
  // THANK YOU MESSAGE
  // -----------------------------------------------------
  doc
    .fontSize(14)
    .text("Thank you for your payment!", { align: "center" })
    .moveDown(3);

  // -----------------------------------------------------
  // STAMP (bottom-right)
  // -----------------------------------------------------
  doc.image(stampBuffer, 380, 650, { width: 150 });

  doc.end();
});


  const storage = admin.storage().bucket("rentwave-ica.firebasestorage.app");
  console.log("🔥 Using bucket:", storage.name);
  const filePath = `receipts/${paymentId}.pdf`;

  await storage.file(filePath).save(pdfBuffer, {
    metadata: { contentType: "application/pdf" }
  });

  const [downloadUrl] = await storage.file(filePath).getSignedUrl({
    action: "read",
    expires: "03-09-2035"
  });

  await db.collection("payments").doc(paymentId).update({
    receiptUrl: downloadUrl
  });

  const emailHtml = `
  <!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Payment Confirmation - RentWave</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            color: #333333;
            background-color: #f0f4f8;
            margin: 0;
            padding: 0;
        }
        .container {
            max-width: 800px;
            width: 100%;
            margin: 20px auto;
            padding: 20px;
            border: 1px solid #d0dbe1;
            border-radius: 10px;
            box-shadow: 0 0 15px rgba(0, 0, 0, 0.1);
            background-color: #f4f4f4;
        }
        .header {
            background: #5F92DF;
            padding: 15px;
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
            border-bottom: 2px solid #5F92DF;
            color: #f4f4f4;
            border-radius: 10px 10px 0 0;
        }
        .header img {
            width: 120px;
            height: 100px;
            object-fit: contain;
            position: absolute;
            left: 15px;
        }
        .content {
            padding: 20px;
            color: #333333;
        }
        .footer {
            background: #5F92DF;
            padding: 15px;
            text-align: center;
            border-top: 2px solid #5F92DF;
            font-size: 0.9em;
            color: #f4f4f4;
            border-radius: 0 0 10px 10px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <img src="${logoUrl}" alt="RentWave Logo">
            <h1>Payment Confirmation</h1>
        </div>
        <div class="content">
            <p>Dear ${tenantName},</p>
            <p>We have received your payment successfully. Thank you for completing the transaction.</p>
            <p>Attached to this email is your payment receipt for your records.</p>
            <p>If you have any questions or need further assistance, feel free to reach out to us.</p>
            <p>Thank you for choosing RentWave. We appreciate your prompt payment!</p>
            <p>Best regards,<br>The RentWave Team</p>
        </div>
        <div class="footer">
            RentWave - Making Rental Management Easier
        </div>
    </div>
</body>
</html>`;

  await sendMail({
    to: tenantEmail,
    subject: "RentWave Payment Receipt",
    html: emailHtml,
    attachments: [
      {
        filename: `rent-receipt-${paymentId}.pdf`,
        content: pdfBuffer
      }
    ]
  });

  console.log("📨 Receipt delivered + saved:", tenantEmail);
  return { success: true, receiptUrl: downloadUrl };
});
