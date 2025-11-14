# Guía para Posicionar las Islas Manualmente

## 📍 Archivo: `app/src/main/res/layout/activity_home.xml`

### 🔧 Cómo Ajustar las Posiciones:

#### 1. **Posición Horizontal (Izquierda-Derecha)**
- **Con `horizontalBias`**: Valores de `0.0` a `1.0`
  - `0.0` = Todo a la izquierda
  - `0.5` = Centro
  - `1.0` = Todo a la derecha
  - Ejemplo: `app:layout_constraintHorizontal_bias="0.498"` (centro horizontal)

- **Con `layout_marginStart`**: Márgenes desde la izquierda en dp
  - Ejemplo: `android:layout_marginStart="236dp"` (236dp desde la izquierda)

- **Con `layout_marginEnd`**: Márgenes desde la derecha en dp
  - Ejemplo: `android:layout_marginEnd="244dp"` (244dp desde la derecha)

#### 2. **Posición Vertical (Arriba-Abajo)**
- **Con `verticalBias`**: Valores de `0.0` a `1.0`
  - `0.0` = Todo arriba
  - `0.5` = Centro vertical
  - `1.0` = Todo abajo
  - Ejemplo: `app:layout_constraintVertical_bias="0.036"` (cerca del top)

#### 3. **Tamaño de las Islas**
- **Tamaño actual**:
  - Conocimiento: `200dp x 200dp`
  - Lectura: `180dp x 180dp`
  - Sociales: `180dp x 180dp`
  - Ciencias: `210dp x 210dp`
  - Matemáticas: `190dp x 190dp`
  - Inglés: `180dp x 180dp`

- **Para cambiar el tamaño**: Modifica `android:layout_width` y `android:layout_height`
  - Ejemplo: `android:layout_width="200dp"` y `android:layout_height="200dp"`

### 📝 Posiciones Actuales de las Islas:

#### **Isla de Conocimiento** (Líneas 38-56)
```xml
app:layout_constraintHorizontal_bias="0.498"  <!-- Centro horizontal -->
app:layout_constraintVertical_bias="0.036"    <!-- Cerca del top -->
```
- **Para mover izquierda/derecha**: Cambia `horizontalBias` (0.0-1.0)
- **Para mover arriba/abajo**: Cambia `verticalBias` (0.0-1.0)

#### **Isla de Lectura** (Líneas 58-75)
```xml
android:layout_marginStart="236dp"            <!-- 236dp desde la izquierda -->
app:layout_constraintVertical_bias="0.449"    <!-- Centro-izquierda vertical -->
```
- **Para mover izquierda/derecha**: Cambia `marginStart` (aumentar = más a la derecha)
- **Para mover arriba/abajo**: Cambia `verticalBias` (0.0-1.0)

#### **Isla de Sociales** (Líneas 77-94)
```xml
android:layout_marginStart="36dp"             <!-- 36dp desde la izquierda -->
app:layout_constraintVertical_bias="0.312"    <!-- Arriba-izquierda vertical -->
```
- **Para mover izquierda/derecha**: Cambia `marginStart` (aumentar = más a la derecha)
- **Para mover arriba/abajo**: Cambia `verticalBias` (0.0-1.0)

#### **Isla de Ciencias** (Líneas 96-114)
```xml
app:layout_constraintHorizontal_bias="0.061"  <!-- Izquierda -->
app:layout_constraintVertical_bias="1.0"      <!-- Abajo -->
```
- **Para mover izquierda/derecha**: Cambia `horizontalBias` (0.0-1.0)
- **Para mover arriba/abajo**: Cambia `verticalBias` (0.0-1.0)

#### **Isla de Matemáticas** (Líneas 116-133)
```xml
app:layout_constraintHorizontal_bias="0.85"   <!-- Derecha -->
app:layout_constraintVertical_bias="0.68"     <!-- Centro-derecha vertical -->
```
- **Para mover izquierda/derecha**: Cambia `horizontalBias` (0.0-1.0)
- **Para mover arriba/abajo**: Cambia `verticalBias` (0.0-1.0)

#### **Isla de Inglés** (Líneas 135-152)
```xml
android:layout_marginEnd="244dp"              <!-- 244dp desde la derecha -->
app:layout_constraintVertical_bias="0.663"    <!-- Centro-derecha vertical -->
```
- **Para mover izquierda/derecha**: Cambia `marginEnd` (aumentar = más a la izquierda)
- **Para mover arriba/abajo**: Cambia `verticalBias` (0.0-1.0)

### 💡 Tips para Ajustar Posiciones:

1. **Usa el editor visual de Android Studio**:
   - Abre `activity_home.xml` en modo Design
   - Arrastra las islas visualmente
   - Los valores se actualizan automáticamente

2. **Valores de Bias recomendados**:
   - `0.0` - `0.2`: Izquierda/Arriba
   - `0.3` - `0.7`: Centro
   - `0.8` - `1.0`: Derecha/Abajo

3. **Márgenes recomendados**:
   - Márgenes pequeños: `16dp` - `50dp`
   - Márgenes medianos: `50dp` - `150dp`
   - Márgenes grandes: `150dp` - `300dp`

4. **Para ajustes finos**:
   - Usa valores decimales en bias: `0.498`, `0.312`, etc.
   - Ajusta márgenes en incrementos de `8dp` o `16dp`

### ⚠️ Importante:
- Las islas deben estar en el contenedor `islandsContainer` (líneas 23-154)
- Las posiciones deben coincidir con los hotspots para que los clics funcionen correctamente
- Los hotspots están en el contenedor `hotspotsContainer` (líneas 124-229)



