const functions = require("firebase-functions");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

admin.initializeApp();

// Configurar Gmail para enviar correos (usa una cuenta real)
const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: "TU_CORREO@gmail.com",
    pass: "TU_PASSWORD_DE_APLICACION", // NO la contraseña normal
  },
});

// ✅ 1) Generar y enviar OTP
exports.sendRecoveryCode = functions.https.onCall(async (data, context) => {
  const email = data.email;
  if (!email) return { success: false, error: "Email requerido" };

  // Generar código de 6 dígitos
  const code = Math.floor(100000 + Math.random() * 900000).toString();

  // Guardarlo temporalmente en Firestore
  await admin.firestore().collection("password_resets").doc(email).set({
    code: code,
    createdAt: admin.firestore.Timestamp.now(),
  });

  // Enviar correo
  const mailOptions = {
    from: "PlantCare <TU_CORREO@gmail.com>",
    to: email,
    subject: "Código de verificación - PlantCare",
    text: `Tu código de verificación es: ${code}\nEste código vence en 10 minutos.`,
  };

  try {
    await transporter.sendMail(mailOptions);
    return { success: true };
  } catch (error) {
    console.error(error);
    return { success: false, error: error.message };
  }
});
