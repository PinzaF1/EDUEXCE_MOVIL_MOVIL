# 🚨 ERROR BACKEND: ENDPOINT FCM-TOKEN NO FUNCIONA

## ❌ PROBLEMA DETECTADO

El endpoint `/movil/fcm-token` del backend está devolviendo un **error 500**:

```json
{
  "message": "(intermediate value).registrarFcmToken is not a function"
}
```

---

## 📋 DETALLES DEL ERROR

### Request que envía la app móvil:
```
POST https://unimparted-henrietta-uninspissated.ngrok-free.dev/movil/fcm-token

Headers:
  Authorization: Bearer [JWT_TOKEN]
  Content-Type: application/json

Body:
{
  "token": "dowgkSZGSFuV3EMjomzDRj:APA91bGc0P101iOB37Hudr5IrgMXQRy-Y9tdMsnQoAbpGAxjpXgZD1c3zRRWN-vM24lhwF8cqWT5FElq69aIbGomZ4b2ArWozPN6tWO-KTHU7pN6RqBYmEk",
  "device_id": "205176cedc31cf98",
  "platform": "android"
}
```

### Response del backend:
```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "message": "(intermediate value).registrarFcmToken is not a function"
}
```

---

## 🔍 CAUSA PROBABLE

El error `"(intermediate value).registrarFcmToken is not a function"` indica que:

1. **El endpoint existe** (si no existiera, daría 404)
2. **Hay un error en el código** del controlador o servicio
3. **La función `registrarFcmToken()` no está definida** o no se está importando correctamente

### Posibles causas:
- ❌ Función no exportada correctamente
- ❌ Import incorrecto en el controlador
- ❌ Typo en el nombre de la función
- ❌ Servicio no inicializado correctamente

---

## ✅ SOLUCIÓN ESPERADA

### El backend debe:

1. **Implementar el endpoint correctamente:**
   ```javascript
   // Ejemplo en Node.js/Express
   router.post('/movil/fcm-token', authenticate, async (req, res) => {
     try {
       const { token, device_id, platform } = req.body;
       const userId = req.user.id_usuario;
       
       // Guardar token en base de datos
       await fcmService.registrarFcmToken({
         userId,
         token,
         deviceId: device_id,
         platform
       });
       
       res.json({ success: true, message: 'Token FCM registrado correctamente' });
     } catch (error) {
       console.error('Error al registrar token FCM:', error);
       res.status(500).json({ 
         success: false, 
         message: 'Error al registrar token FCM' 
       });
     }
   });
   ```

2. **Guardar el token en la base de datos:**
   ```sql
   CREATE TABLE fcm_tokens (
     id SERIAL PRIMARY KEY,
     id_usuario INTEGER REFERENCES usuarios(id_usuario),
     token TEXT NOT NULL,
     device_id VARCHAR(255),
     platform VARCHAR(50),
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     UNIQUE(id_usuario, device_id)
   );
   ```

3. **Responder con éxito:**
   ```json
   {
     "success": true,
     "message": "Token FCM registrado correctamente"
   }
   ```

---

## 📱 IMPACTO

### ⚠️ Impacto actual:
- ❌ Los tokens FCM NO se guardan en el servidor
- ❌ El backend NO puede enviar notificaciones push
- ✅ **La app funciona normal** (el error no bloquea el flujo)
- ✅ **Las notificaciones de Firebase Console SÍ funcionan**

### ✅ Una vez corregido:
- ✅ Backend podrá guardar tokens FCM
- ✅ Backend podrá enviar notificaciones automáticas:
  - Puntajes bajos detectados
  - Recordatorios de práctica
  - Logros desbloqueados
  - Mensajes personalizados

---

## 🧪 CÓMO PROBAR

### 1. **Verificar que el endpoint exista:**
```bash
curl -X POST https://unimparted-henrietta-uninspissated.ngrok-free.dev/movil/fcm-token \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer [TOKEN]" \
  -d '{
    "token": "test_token",
    "device_id": "test_device",
    "platform": "android"
  }'
```

### 2. **Respuesta esperada:**
```json
{
  "success": true,
  "message": "Token FCM registrado correctamente"
}
```

### 3. **Verificar en base de datos:**
```sql
SELECT * FROM fcm_tokens WHERE id_usuario = [USER_ID] ORDER BY created_at DESC LIMIT 1;
```

---

## 📊 LOGS DE LA APP MÓVIL

```
2025-11-14 09:15:27.314  FCM_TOKEN: 📱 Token FCM completo: dowgkSZGSFuV3EMjomzDRj:...
2025-11-14 09:15:27.322  FCM_TOKEN: 📤 Enviando token al servidor...
2025-11-14 09:15:27.327  okhttp: --> POST /movil/fcm-token
2025-11-14 09:15:27.864  okhttp: <-- 500 (ERROR)
2025-11-14 09:15:27.866  okhttp: {"message":"(intermediate value).registrarFcmToken is not a function"}
2025-11-14 09:15:27.867  FCM_TOKEN: ❌ Error al registrar token: 500
```

---

## 🎯 PRIORIDAD

**PRIORIDAD: MEDIA** 

- No bloquea el uso de la app
- No bloquea pruebas con Firebase Console
- **Bloquea el envío automático de notificaciones desde el backend**

---

## 📝 CONTACTO TÉCNICO

**Equipo:** Android  
**Fecha detección:** 2025-11-14  
**Log ID:** m59b2rzroy6z3jn5zlmdk23k  

**Token de prueba disponible:**
```
dowgkSZGSFuV3EMjomzDRj:APA91bGc0P101iOB37Hudr5IrgMXQRy-Y9tdMsnQoAbpGAxjpXgZD1c3zRRWN-vM24lhwF8cqWT5FElq69aIbGomZ4b2ArWozPN6tWO-KTHU7pN6RqBYmEk
```

---

## ✅ CHECKLIST PARA BACKEND

- [ ] Verificar que `registrarFcmToken()` esté definida en el servicio
- [ ] Verificar imports en el controlador
- [ ] Verificar que la ruta esté correctamente configurada
- [ ] Crear tabla `fcm_tokens` si no existe
- [ ] Probar endpoint con Postman/cURL
- [ ] Verificar que guarda correctamente en BD
- [ ] Responder con status 200 y `{ success: true }`
- [ ] Notificar a equipo móvil cuando esté listo

---

**Estado:** 🔴 **PENDIENTE CORRECCIÓN EN BACKEND**

