const nodemailer = require("nodemailer");
require("dotenv").config();

const sendMail = async (options) => {
  try {
    // Create a transporter object
    const transporter = nodemailer.createTransport({
      service: process.env.SERVICE, 
      secure: process.env.SERVICE === 'gmail', 
      auth: {
        user: process.env.MAIL_USER, 
        pass: process.env.MAIL_PASSWORD, 
      },
    });

    // Define the email options
    const mailOptions = {
      from: `"RentWave" <${process.env.MAIL_USER}>`, 
      to: options.to, 
      subject: options.subject, 
      text: options.text || '', 
      html: options.html, 
      attachments: options.attachments || [], 
};

    // Debugging: Log mail options
    console.log("Mail options:", mailOptions);

    // Send the email
    const info = await transporter.sendMail(mailOptions);
    console.log("Email sent: " + info.response);
  } catch (error) {
    console.error("Error sending email:", error);
  }
};

module.exports = sendMail;

