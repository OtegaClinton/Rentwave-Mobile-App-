const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");
const sendMail = require("./sendMail");

exports.sendRentDueReminders = onSchedule(
  {
    schedule: "every day 09:00",
    timeZone: "Europe/London",
  },
  async () => {
    const db = admin.firestore();

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const tenantsSnap = await db.collection("tenants").get();

    for (const doc of tenantsSnap.docs) {
      const tenant = doc.data();

      if (!tenant.nextRentDate || tenant.rentStatus === "Paid") continue;

      const rentDueDate = new Date(tenant.nextRentDate);
      rentDueDate.setHours(0, 0, 0, 0);

      const diffDays = Math.ceil(
        (rentDueDate - today) / (1000 * 60 * 60 * 24)
      );

      // 🔔 Notify only 3, 2, or 1 days before due date
      if (diffDays > 3 || diffDays < 1) continue;

      const formattedDate = rentDueDate.toDateString();

      const emailHtml = `
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Rent Due Reminder - RentWave</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f0f4f8;
            margin: 0;
            padding: 0;
        }
        .container {
            width: 80%;
            margin: 20px auto;
            padding: 20px;
            border-radius: 10px;
            background-color: #f4f4f4;
        }
        .header {
            background: #5F92DF;
            padding: 15px;
            color: white;
            text-align: center;
            border-radius: 10px 10px 0 0;
        }
        .content {
            padding: 20px;
        }
        .highlight {
            background: #ffffff;
            border-left: 4px solid #5F92DF;
            padding: 12px;
            margin: 16px 0;
        }
        .footer {
            background: #5F92DF;
            padding: 15px;
            text-align: center;
            color: white;
            border-radius: 0 0 10px 10px;
        }
    </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <h1>Rent Due Reminder</h1>
    </div>

    <div class="content">
      <p>Dear ${tenant.firstName || "Tenant"},</p>

      <p>
        This is a friendly reminder that your rent payment for
        <strong>${tenant.propertyName}</strong>
        is due in <strong>${diffDays} day${diffDays > 1 ? "s" : ""}</strong>.
      </p>

      <div class="highlight">
        <p><strong>Amount:</strong> £${tenant.rentAmount}</p>
        <p><strong>Due Date:</strong> ${formattedDate}</p>
      </div>

      <p>Please ensure payment is made on time.</p>

      <p>If you have already paid, kindly ignore this email.</p>

      <p>Best regards,<br><strong>RentWave Team</strong></p>
    </div>

    <div class="footer">
      &copy; ${new Date().getFullYear()} RentWave
    </div>
  </div>
</body>
</html>
      `;

      await sendMail({
        to: tenant.email,
        subject: `Rent Due in ${diffDays} Day${diffDays > 1 ? "s" : ""} – RentWave`,
        html: emailHtml,
      });

      console.log(
        `📧 Rent reminder sent to ${tenant.email} (${diffDays} days left)`
      );
    }

    return null;
  }
);
