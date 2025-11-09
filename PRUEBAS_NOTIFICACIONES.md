# 🔔 Guía de Pruebas - Sistema de Notificaciones Push

## ✅ Checklist de Verificación

### 1️⃣ Verificar Token FCM Registrado

**Pasos:**
1. Abre la app y haz login
2. Ve a la pantalla de **Notificaciones** (ícono de campana)
3. Revisa el **Logcat** con el filtro `FCM_TOKEN`

**Deberías ver:**
```
📱 Token FCM actual: [tu_token_aqui]
✅ Token guardado correctamente
✅ Usuario autenticado
```

**Si ves:**
```
⚠️ No hay token FCM guardado
```
Significa que Firebase no generó el token. Verifica la configuración de Firebase.

---

### 2️⃣ Probar Notificación Local (Sin Backend)

**Pasos:**
1. En la pantalla de **Notificaciones**, toca el ícono **+** (arriba a la derecha)
2. Deberías ver un Toast: "✅ Notificación de prueba creada"
3. La notificación aparecerá en la lista con:
   - Título: "📉 Prueba de Notificación"
   - Mensaje: "Esta es una notificación de prueba..."
   - Indicador rojo (puntaje bajo)
   - Área: Matemáticas • Puntaje: 35%

**Esto verifica:**
- ✅ NotificationStorage funciona
- ✅ RecyclerView muestra notificaciones
- ✅ Colores dinámicos funcionan
- ✅ UI está correcta

---

### 3️⃣ Enviar Notificación desde el Backend

**Requisitos:**
- Backend corriendo en `http://localhost:3333` (o tu ngrok URL)
- Token FCM registrado (paso 1)

**Opción A: Usar Firebase Console**
1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Selecciona tu proyecto: `eduexce-b1296`
3. Ve a **Cloud Messaging** → **Send your first message**
4. Configura:
   - **Título:** "📉 Puntaje bajo detectado"
   - **Texto:** "Obtuviste 35% en Matemáticas. ¡Sigue practicando!"
5. En **Target**, selecciona **Single device** y pega tu token FCM
6. En **Additional options** → **Custom data**, agrega:
   ```
   tipo: puntaje_bajo_inmediato
   area: Matemáticas
   puntaje: 35
   ```
7. Envía la notificación

**Opción B: Desde tu Backend (Node.js)**

Copia el token FCM del logcat y ejecuta esto en tu backend:

```javascript
// En tu backend Node.js
const admin = require('firebase-admin');

async function enviarNotificacionPrueba(fcmToken) {
  const message = {
    notification: {
      title: '📉 Puntaje bajo detectado',
      body: 'Obtuviste 35% en Matemáticas. ¡Sigue practicando!'
    },
    data: {
      tipo: 'puntaje_bajo_inmediato',
      area: 'Matemáticas',
      puntaje: '35',
      id_usuario: '123'
    },
    token: fcmToken
  };

  try {
    const response = await admin.messaging().send(message);
    console.log('✅ Notificación enviada:', response);
  } catch (error) {
    console.error('❌ Error:', error);
  }
}

// Reemplaza con tu token FCM real
enviarNotificacionPrueba('TU_TOKEN_FCM_AQUI');
```

**Deberías ver:**
1. Notificación push en la barra de notificaciones de Android
2. Color rojo (puntaje bajo)
3. Al tocar, abre la app
4. La notificación aparece en el historial

---

### 4️⃣ Verificar Colores Dinámicos

Prueba diferentes tipos de notificaciones:

**Puntaje Bajo (Rojo):**
```json
{
  "tipo": "puntaje_bajo_inmediato",
  "puntaje": "35"
}
```

**Recordatorio (Naranja):**
```json
{
  "tipo": "recordatorio_practica",
  "puntaje": "55"
}
```

**Logro (Verde):**
```json
{
  "tipo": "logro_desbloqueado",
  "puntaje": "85"
}
```

---

## 🐛 Solución de Problemas

### ❌ No aparece token FCM
**Causa:** Firebase no está inicializado correctamente

**Solución:**
1. Verifica que `google-services.json` esté en `app/`
2. Verifica que el `applicationId` coincida con Firebase Console
3. Limpia y reconstruye: `Build → Clean Project → Rebuild Project`

---

### ❌ Token no se registra en el servidor
**Logcat muestra:**
```
❌ Error al registrar token FCM: 401
```

**Causa:** Token de autenticación inválido o expirado

**Solución:**
1. Cierra sesión y vuelve a hacer login
2. Verifica que el endpoint sea correcto: `POST /movil/fcm-token`
3. Verifica que el backend esté corriendo

---

### ❌ Notificaciones no llegan desde el backend
**Verifica:**
1. ✅ Token FCM registrado en el servidor
2. ✅ Backend tiene Firebase Admin SDK inicializado
3. ✅ App está en segundo plano (las notificaciones en primer plano se manejan diferente)
4. ✅ Permisos de notificaciones otorgados (Android 13+)

**Logs a revisar:**
```
Logcat → Filtro: "FCM"
```

---

## 📊 Formato de Datos del Backend

Tu backend debe enviar notificaciones en este formato:

```json
{
  "notification": {
    "title": "📉 Puntaje bajo detectado",
    "body": "Obtuviste 35% en Matemáticas. ¡Sigue practicando!"
  },
  "data": {
    "tipo": "puntaje_bajo_inmediato",
    "area": "Matemáticas",
    "puntaje": "35",
    "id_usuario": "123"
  },
  "token": "fcm_token_del_usuario"
}
```

---

## 🎯 Checklist Final

- [ ] Token FCM se genera al hacer login
- [ ] Token se guarda en SharedPreferences
- [ ] Token se envía al servidor correctamente
- [ ] Notificación de prueba local funciona
- [ ] Notificaciones desde Firebase Console llegan
- [ ] Notificaciones desde backend llegan
- [ ] Colores dinámicos funcionan (rojo, naranja, verde, azul)
- [ ] Historial de notificaciones se muestra correctamente
- [ ] Marcar como leída funciona
- [ ] Estado vacío se muestra cuando no hay notificaciones

---

## 📱 Próximos Pasos

1. **Compila la app**
2. **Haz login**
3. **Ve a Notificaciones** y toca el botón **+** para crear una notificación de prueba
4. **Revisa el Logcat** para ver el token FCM
5. **Copia el token** y envía una notificación desde Firebase Console o tu backend
6. **Verifica** que todo funcione correctamente

¡Listo! 🚀
