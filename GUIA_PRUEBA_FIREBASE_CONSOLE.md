# 🧪 GUÍA: PROBAR NOTIFICACIONES CON FIREBASE CONSOLE

## 📋 PASOS COMPLETOS PARA PRUEBA

---

## **PASO 1: Obtener el Token FCM** 📱

### ⚠️ IMPORTANTE: Fix Aplicado
**El código ha sido actualizado** para que el token FCM se registre automáticamente después del login. Antes de continuar:

1. ✅ **Recompila la app** con los últimos cambios
2. ✅ **Desinstala la app anterior** del dispositivo (si existe)
3. ✅ **Instala la nueva versión**

### 1.1 Ejecutar la App
1. ✅ Abre Android Studio
2. ✅ Ejecuta la app en tu dispositivo/emulador
3. ✅ Haz login normalmente

### 1.2 Copiar el Token desde Logcat
1. ✅ Abre Logcat en Android Studio (View → Tool Windows → Logcat)
2. ✅ Filtra por: `FCM_TOKEN`
3. ✅ Busca el log que dice:
   ```
   ===================================
   COPIA ESTE TOKEN PARA FIREBASE CONSOLE:
   [TOKEN_LARGO_AQUÍ]
   ===================================
   ```
4. ✅ **Copia todo el token** (es muy largo, aprox 150+ caracteres)

**Si NO ves el token:**
- Verifica que Firebase esté correctamente configurado en `google-services.json`
- Asegúrate de que la app tenga permisos de notificaciones (Android 13+)
- Revisa Logcat buscando errores de Firebase

**Ejemplo de token:**
```
dGhpc2lzYW5leGFtcGxldG9rZW5mb3JmaXJlYmFzZXRlc3RpbmdwdXJwb3Nlc29ubHkxMjM0NTY3ODkw...
```

---

## **PASO 2: Ir a Firebase Console** 🌐

### 2.1 Acceder
1. ✅ Abre tu navegador
2. ✅ Ve a: [https://console.firebase.google.com](https://console.firebase.google.com)
3. ✅ Selecciona tu proyecto **Eduexce** (o como se llame tu proyecto)

### 2.2 Navegar a Cloud Messaging
1. ✅ En el menú lateral izquierdo, busca **"Interactuar"** o **"Engage"**
2. ✅ Click en **"Messaging"** o **"Cloud Messaging"**
3. ✅ Click en **"Enviar tu primer mensaje"** o **"Send your first message"**

**Ruta visual:**
```
Firebase Console
  └─ [Tu Proyecto]
      └─ Engage / Interactuar
          └─ Messaging / Cloud Messaging
              └─ [Botón: Enviar tu primer mensaje]
```

---

## **PASO 3: Configurar la Notificación de Prueba** ✍️

### 3.1 Pestaña "Notification" (Notificación)

**Título de la notificación:**
```
📉 Puntaje bajo detectado
```

**Texto de la notificación:**
```
Obtuviste 35% en Matemáticas. ¡Sigue practicando!
```

**Imagen de la notificación (opcional):**
```
[Dejar en blanco]
```

**Nombre de la notificación (opcional):**
```
Test Notificación - Puntaje Bajo
```

### 3.2 Click en "Siguiente" / "Next"

---

## **PASO 4: Seleccionar Dispositivo de Destino** 🎯

### 4.1 Seleccionar "Dispositivo de prueba"
1. ✅ En la sección **"Target"** / **"Destino"**
2. ✅ Selecciona **"Dispositivo de prueba"** / **"Test device"**
3. ✅ Click en **"Agregar un dispositivo de prueba"** / **"Add test device"**

### 4.2 Pegar el Token FCM
1. ✅ En el campo que aparece, **pega el token FCM completo** que copiaste en el Paso 1
2. ✅ (Opcional) Agrega un nombre descriptivo: "Mi Dispositivo Android"
3. ✅ Click en **"Agregar"** / **"Add"**
4. ✅ Asegúrate de que el checkbox esté marcado

### 4.3 Click en "Siguiente" / "Next"

---

## **PASO 5: Configurar Opciones Adicionales** ⚙️

### 5.1 Programación (opcional)
- ✅ Selecciona **"Ahora"** / **"Now"**
- ✅ Click en **"Siguiente"** / **"Next"**

### 5.2 Conversión (opcional)
- ✅ Deja en blanco o por defecto
- ✅ Click en **"Siguiente"** / **"Next"**

### 5.3 Opciones Adicionales - **¡IMPORTANTE!** 🔴

Aquí es donde configuramos los **datos personalizados**:

1. ✅ Expande **"Opciones adicionales"** / **"Additional options"**
2. ✅ Busca la sección **"Datos personalizados"** / **"Custom data"**
3. ✅ Agrega los siguientes pares clave-valor:

| Clave | Valor |
|-------|-------|
| `tipo` | `puntaje_bajo_inmediato` |
| `area` | `Matemáticas` |
| `puntaje` | `35` |

**Cómo agregar:**
- Click en **"Agregar"** / **"Add"**
- Escribe la clave en el campo izquierdo
- Escribe el valor en el campo derecho
- Repite para cada par

### 5.4 Click en "Revisar" / "Review"

---

## **PASO 6: Enviar la Notificación** 🚀

### 6.1 Revisar Configuración
1. ✅ Verifica que todo esté correcto:
   - Título: "📉 Puntaje bajo detectado"
   - Mensaje: "Obtuviste 35% en Matemáticas..."
   - Destino: Tu dispositivo de prueba
   - Datos personalizados: tipo, area, puntaje

### 6.2 Publicar
1. ✅ Click en **"Publicar"** / **"Publish"**
2. ✅ Espera la confirmación: "Notificación enviada"

---

## **PASO 7: Verificar en la App** ✅

### 7.1 Notificación en Barra de Estado
**Si la app está en segundo plano:**
- ✅ Deberías ver la notificación en la barra de notificaciones de Android
- ✅ Título: "📉 Puntaje bajo detectado"
- ✅ Mensaje: "Obtuviste 35% en Matemáticas..."

**Si la app está en primer plano:**
- ✅ La notificación se maneja en `MyFirebaseMessagingService`
- ✅ Se guarda automáticamente en el historial
- ✅ Se muestra en la barra de estado

### 7.2 Badge en HomeActivity
1. ✅ Abre la app (o ya está abierta)
2. ✅ Ve a HomeActivity
3. ✅ **Deberías ver el badge rojo con "1"** en el ícono de campana 🔔

### 7.3 Lista de Notificaciones
1. ✅ Click en el ícono de campana
2. ✅ Deberías ver la notificación con:
   - **Fondo azul claro** (no leída)
   - **Puntito azul** en la esquina
   - Título: "📉 Puntaje bajo detectado"
   - Mensaje: "Obtuviste 35% en Matemáticas..."
   - Chips: "📚 Matemáticas • 35%"
   - Tiempo: "Ahora"

### 7.4 Interacciones
1. ✅ Click en la notificación
   - Animación de escala
   - Marca como leída automáticamente
   - Fondo cambia a transparente
   - Opacidad reduce a 70%
   - Puntito desaparece
   - Badge actualiza a "0" (si no hay más)

---

## **PASO 8: Probar Funcionalidades Adicionales** 🧪

### 8.1 Enviar Múltiples Notificaciones
1. ✅ Repite los pasos 3-6 con diferentes datos:

**Notificación 2 - Recordatorio:**
- Título: `📢 Recordatorio de práctica`
- Mensaje: `¡Hora de practicar Ciencias Naturales!`
- Datos:
  - `tipo`: `recordatorio_practica`
  - `area`: `Ciencias`

**Notificación 3 - Logro:**
- Título: `🎉 Logro desbloqueado`
- Mensaje: `¡Completaste el nivel 3 de Matemáticas!`
- Datos:
  - `tipo`: `logro_desbloqueado`
  - `area`: `Matemáticas`

### 8.2 Verificar Badge
- ✅ Badge debería mostrar "3"
- ✅ Contador "3 sin leer" visible

### 8.3 Probar "Marcar todas como leídas"
1. ✅ Abre NotificationsActivity
2. ✅ Click en "Marcar todas como leídas"
3. ✅ Verifica:
   - Mensaje "✓ Todas leídas" por 1.5 segundos
   - Badge desaparece
   - Contador se oculta
   - Todas las notificaciones con fondo transparente

---

## 📊 **RESULTADOS ESPERADOS**

### ✅ Notificación Recibida
- [x] Aparece en barra de notificaciones Android
- [x] Se guarda en historial local
- [x] Badge muestra contador correcto

### ✅ Estados Visuales
- [x] No leída: Fondo azul claro + puntito azul
- [x] Leída: Fondo transparente + opacidad 70%

### ✅ Interacciones
- [x] Click marca como leída
- [x] Animación de escala
- [x] Badge se actualiza automáticamente

### ✅ Marcar Todas
- [x] Botón visible cuando hay no leídas
- [x] Marca todas con un click
- [x] Feedback "✓ Todas leídas"

---

## 🔍 **TROUBLESHOOTING**

### ❌ No llega la notificación
**Soluciones:**
1. ✅ Verifica que el token FCM sea el correcto (cópialo de nuevo)
2. ✅ Asegúrate de que la app tiene permisos de notificaciones (Android 13+)
3. ✅ Verifica que Firebase Cloud Messaging esté habilitado en tu proyecto
4. ✅ Revisa Logcat para ver si hay errores

### ❌ No aparece el badge
**Soluciones:**
1. ✅ Verifica que la notificación se guardó (Logcat: "✅ Notificación guardada")
2. ✅ Abre y cierra la app para refrescar
3. ✅ Ve a NotificationsActivity y vuelve a Home

### ❌ Notificación no se marca como leída
**Soluciones:**
1. ✅ Verifica que el click funcione (deberías ver la animación)
2. ✅ Revisa Logcat para errores
3. ✅ Limpia datos de la app y vuelve a intentar

---

## 📱 **TIPOS DE NOTIFICACIONES DISPONIBLES**

### 1. Puntaje Bajo (Rojo) 🔴
```
Título: 📉 Puntaje bajo detectado
Datos:
  tipo: puntaje_bajo_inmediato
  area: [Matemáticas/Lenguaje/Ciencias/etc]
  puntaje: [0-40]
```

### 2. Recordatorio (Naranja) 🟠
```
Título: 📢 Recordatorio de práctica
Datos:
  tipo: recordatorio_practica
  area: [Cualquier área]
```

### 3. Logro (Verde) 🟢
```
Título: 🎉 Logro desbloqueado
Datos:
  tipo: logro_desbloqueado
  area: [Cualquier área]
```

### 4. General (Azul) 🔵
```
Título: [Cualquier título]
Datos:
  tipo: [otro tipo]
```

---

## 🎯 **CHECKLIST DE PRUEBAS**

- [ ] Token FCM obtenido correctamente
- [ ] Notificación enviada desde Firebase Console
- [ ] Notificación recibida en dispositivo
- [ ] Badge aparece con contador correcto
- [ ] Click en campana abre NotificationsActivity
- [ ] Notificación tiene fondo azul (no leída)
- [ ] Click en notificación marca como leída
- [ ] Animación de escala funciona
- [ ] Fondo cambia a transparente (leída)
- [ ] Badge se actualiza a "0"
- [ ] Botón "Marcar todas" visible con no leídas
- [ ] Marcar todas funciona correctamente
- [ ] Contador "X sin leer" actualiza correctamente

---

## 🎉 **¡ÉXITO!**

Si completaste todos los pasos y las verificaciones, tu sistema de notificaciones está funcionando **100% correctamente**. 

**Próximo paso:** Integrar con el backend para que envíe notificaciones automáticas basadas en eventos reales (puntajes bajos, recordatorios, logros).

---

## 📝 **NOTAS IMPORTANTES**

### ⚠️ Token Temporal en Logs
El log que muestra el token completo es **solo para testing**. Después de las pruebas, deberías revertir el cambio para no exponer el token en producción.

### 🔒 Seguridad
- Los tokens FCM son sensibles pero regenerables
- No los incluyas en repositorios públicos
- El backend debe almacenarlos de forma segura

### 🔄 Token puede cambiar
El token FCM puede cambiar si:
- Usuario desinstala y reinstala la app
- Se borran datos de la app
- Firebase lo regenera automáticamente

---

**Fecha:** 2025-11-14  
**Versión:** Testing v1.0

