// --- CÓDIGO FINAL CON SINTAXIS V2 Y GESTIÓN DE SECRETS ---

// Paso 1: Importar los módulos necesarios de la V2
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { defineSecret } = require("firebase-functions/params"); // Para acceder a las variables seguras
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

// Inicializar la app de Admin
admin.initializeApp();

// Definimos los secrets que vamos a usar en nuestras funciones
const gmailUser = defineSecret("GMAIL_USER");
const gmailPass = defineSecret("GMAIL_PASS");

/**
 * Genera un código aleatorio de 6 dígitos.
 */
function generateCode() {
  return Math.floor(100000 + Math.random() * 900000).toString();
}

/**
 * Envía código de recuperación por correo.
 * - Usa sintaxis V2.
 * - Usa Secrets para las credenciales.
 * - Tiene opciones para evitar el "Container Healthcheck failed".
 */
exports.sendRecoveryCode = onCall({
  timeoutSeconds: 60,
  minInstances: 0,
  secrets: ["GMAIL_USER", "GMAIL_PASS"] // <-- CORREGIDO: Se usan los nombres como texto
}, async (request) => {
  try {
    // El transporter se crea DENTRO de la función para acceder a los secrets
    const transporter = nodemailer.createTransport({
        service: 'gmail',
        auth: {
            user: gmailUser.value(), // <-- Así se lee el valor del secret
            pass: gmailPass.value()  // <-- Así se lee el valor del secret
        }
    });

    const email = request.data.email;

    if (!email) {
      throw new HttpsError('invalid-argument', 'El email es requerido.');
    }

    let userRecord;
    try {
      userRecord = await admin.auth().getUserByEmail(email);
    } catch (error) {
      if (error.code === 'auth/user-not-found') {
        console.log('Intento de recuperación para email no registrado:', email);
        return { success: true, message: 'Si el email existe en nuestros registros, recibirás un código.' };
      }
      throw new HttpsError('internal', error.message);
    }

    const code = generateCode();
    const expiresAt = admin.firestore.Timestamp.fromDate(new Date(Date.now() + 10 * 60 * 1000)); // 10 minutos

    await admin.firestore().collection('recovery_codes').doc(email).set({
      code: code,
      email: email,
      userId: userRecord.uid,
      expiresAt: expiresAt,
      used: false,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    const mailOptions = {
        from: `"PlantCare 🌿" <${gmailUser.value()}>`, // Usamos el email del secret
        to: email,
        subject: '🔐 Código de Recuperación - PlantCare',
        html: `
        <!DOCTYPE html>
        <html>
        <head>
          <style>
            body { font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px; }
            .container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
            .header { background: linear-gradient(135deg, #2E7D32 0%, #1B5E20 100%); color: white; padding: 40px 20px; text-align: center; }
            .header h1 { margin: 0; font-size: 28px; }
            .content { padding: 40px 30px; }
            .code-box { background-color: #E8F5E9; border: 2px dashed #4CAF50; border-radius: 12px; padding: 30px; text-align: center; margin: 30px 0; }
            .code { font-size: 48px; font-weight: bold; color: #2E7D32; letter-spacing: 8px; font-family: 'Courier New', monospace; }
            .warning { background-color: #FFF3E0; border-left: 4px solid #FF9800; padding: 15px; margin: 20px 0; border-radius: 4px; }
            .footer { background-color: #f5f5f5; padding: 20px; text-align: center; font-size: 12px; color: #666; }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="header"><h1>🌿 PlantCare</h1><p>Recuperación de Contraseña</p></div>
            <div class="content">
              <h2>¡Hola!</h2>
              <p>Recibimos una solicitud para recuperar tu contraseña. Usa el siguiente código en la aplicación:</p>
              <div class="code-box"><div class="code">${code}</div><p style="margin: 10px 0 0 0; color: #666;">Este código expira en 10 minutos</p></div>
              <div class="warning"><strong>⚠️ Importante:</strong> Si no solicitaste este código, ignora este correo. Tu cuenta está segura.</div>
              <p style="color: #666; font-size: 14px;">Este código solo puede usarse una vez. No lo compartas con nadie.</p>
            </div>
            <div class="footer"><p>© 2025 PlantCare - Cuida tus plantas con amor 🌱</p><p>Este es un correo automático, por favor no responder.</p></div>
          </div>
        </body>
        </html>
        `,
    };

    await transporter.sendMail(mailOptions);

    console.log('✅ Código enviado exitosamente a:', email);
    return { success: true, message: 'Código enviado exitosamente.' };

  } catch (error) {
    console.error('❌ Error en sendRecoveryCode:', error);
    if (error instanceof HttpsError) {
      throw error;
    }
    throw new HttpsError('internal', 'Ocurrió un error inesperado al enviar el código.');
  }
});

/**
 * Verifica el código de recuperación.
 */
exports.verifyRecoveryCode = onCall({
  timeoutSeconds: 60,
  minInstances: 0,
  secrets: ["GMAIL_USER", "GMAIL_PASS"] // <-- CORREGIDO: Se usan los nombres como texto
}, async (request) => {
  try {
    const { email, code } = request.data;

    if (!email || !code) {
      throw new HttpsError('invalid-argument', 'Email y código son requeridos.');
    }

    const docRef = admin.firestore().collection('recovery_codes').doc(email);
    const doc = await docRef.get();

    if (!doc.exists) {
      throw new HttpsError('not-found', 'Código incorrecto o no encontrado.');
    }

    const docData = doc.data();

    if (docData.used) {
      throw new HttpsError('failed-precondition', 'Este código ya ha sido utilizado.');
    }

    const now = admin.firestore.Timestamp.now();
    if (now.toMillis() > docData.expiresAt.toMillis()) {
      throw new HttpsError('deadline-exceeded', 'El código ha expirado.');
    }

    if (docData.code !== code) {
      throw new HttpsError('invalid-argument', 'Código incorrecto o no encontrado.');
    }

    await docRef.update({ used: true });

    console.log('✅ Código verificado exitosamente para:', email);
    return { success: true, userId: docData.userId, message: 'Código verificado correctamente.' };

  } catch (error) {
    console.error('❌ Error en verifyRecoveryCode:', error);
    if (error instanceof HttpsError) {
      throw error;
    }
    throw new HttpsError('internal', 'Ocurrió un error inesperado al verificar el código.');
  }
});

/**
 * Limpia códigos expirados diariamente.
 */
exports.cleanupExpiredCodes = onSchedule({
  schedule: "every 24 hours",
  timeoutSeconds: 300,
  minInstances: 0,
  secrets: ["GMAIL_USER", "GMAIL_PASS"] // <-- CORREGIDO: Se usan los nombres como texto
}, async (event) => {
  try {
    const now = admin.firestore.Timestamp.now();
    const snapshot = await admin.firestore().collection('recovery_codes').where('expiresAt', '<', now).get();

    if (snapshot.empty) {
      console.log('🧹 No hay códigos expirados para limpiar.');
      return;
    }

    const batch = admin.firestore().batch();
    snapshot.docs.forEach(doc => {
      batch.delete(doc.ref);
    });

    await batch.commit();
    console.log(`🧹 Limpiados ${snapshot.size} códigos expirados.`);

  } catch (error) {
    console.error('❌ Error limpiando códigos expirados:', error);
  }
});
