// Pega esto en tu archivo .eslintrc.js
module.exports = {
  root: true,
  env: {
    es6: true,
    node: true,
  },
  extends: [
    "eslint:recommended",
    "google",
  ],
  parserOptions: {
    "ecmaVersion": 2020, // Usa una versión más moderna de JS
  },
  rules: {
    // --- REGLAS DESACTIVADAS PARA QUE NO MOLESTEN ---
    "quotes": "off",                          // Desactiva la regla de comillas
    "max-len": "off",                         // Desactiva la longitud máxima de línea
    "indent": "off",                          // Desactiva la indentación estricta
    "comma-dangle": "off",                    // Desactiva la coma al final
    "arrow-parens": "off",                    // Desactiva el requisito de paréntesis en funciones flecha
    "object-curly-spacing": "off",            // Desactiva el espaciado en objetos
    "padded-blocks": "off",                   // Desactiva la regla de líneas en blanco en bloques
    "valid-jsdoc": "off",                     // Desactiva la obligación de documentar funciones
    "no-unused-vars": ["warn", { "args": "none" }], // Convierte "variables no usadas" en una advertencia, no un error
  },
};
