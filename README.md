# n4y Client · Glass Edition

Interfaz **Liquid Glass** para Minecraft, construida como mod de **Fabric**.
Es solo visual: los controles son demostrativos y no modifican las mecánicas del juego.

## Versiones

| Componente | Versión |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | 0.20.1 |
| Fabric API | 0.141.6+1.21.11 |
| Fabric Loom | 1.14.10 |
| Gradle (wrapper) | 9.2.1 |
| Java | 21 |
| Mapeos | Mojang oficiales |

## Desarrollo

```bash
./gradlew runClient   # abre Minecraft con el mod
./gradlew build       # genera build/libs/n4y-glass-<versión>.jar
```

En IntelliJ IDEA: *File → Open* sobre la carpeta del proyecto, usar JDK 21 y esperar a que Gradle sincronice.

## Uso

- **Shift derecho** dentro de un mundo abre el menú (se puede cambiar en *Controles → n4y Glass*).
- En el menú principal aparece el botón **n4y Glass** arriba a la izquierda.
- La pantalla usa como fondo `assets/n4y_glass/textures/gui/wallpaper.png` (también es el icono del mod).
- El desenfoque usa la opción de Minecraft *Accesibilidad → Desenfoque del fondo del menú*; si está a 0 no habrá blur.

## Hoja de ruta

1. ✅ Proyecto base y pantalla principal
2. Fondo desenfocado y panel de cristal (shader SDF)
3. Bordes luminosos, reflejos y glow
4. Navegación lateral y categorías con animaciones
5. Tarjetas e interruptores de demostración
6. Búsqueda funcional
7. Refracción líquida y pulido
