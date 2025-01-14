import androidx.compose.ui.ComposeScene
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.input.mouse.MouseScrollOrientation
import androidx.compose.ui.input.mouse.MouseScrollUnit
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import org.lwjgl.glfw.GLFW.*
import java.awt.Component
import java.awt.event.InputEvent
import java.awt.event.MouseEvent

@OptIn(ExperimentalComposeUiApi::class)
fun ComposeScene.subscribeToGLFWEvents(windowHandle: Long) {
    glfwSetMouseButtonCallback(windowHandle) { _, button, action, mods ->
        sendPointerEvent(
            position = glfwGetCursorPos(windowHandle),
            eventType = when (action) {
                GLFW_PRESS -> PointerEventType.Press
                GLFW_RELEASE -> PointerEventType.Release
                else -> PointerEventType.Unknown
            },
            mouseEvent = MouseEvent(getAwtMods(windowHandle))
        )
    }

    glfwSetCursorPosCallback(windowHandle) { _, xpos, ypos ->
        sendPointerEvent(
            position = Offset(xpos.toFloat(), ypos.toFloat()),
            eventType = PointerEventType.Move,
            mouseEvent = MouseEvent(getAwtMods(windowHandle))
        )
    }

    glfwSetCursorEnterCallback(windowHandle) { _, entered ->
        sendPointerEvent(
            position = glfwGetCursorPos(windowHandle),
            eventType = if (entered) PointerEventType.Enter else PointerEventType.Exit,
            mouseEvent = MouseEvent(getAwtMods(windowHandle))
        )
    }

    glfwSetScrollCallback(windowHandle) { _, xoffset, yoffset ->
        sendPointerScrollEvent(
            position = glfwGetCursorPos(windowHandle),
            delta = MouseScrollUnit.Line(
                if (yoffset != 0.0) -3 * yoffset.toFloat() else -3 * xoffset.toFloat()
            ),
            orientation = if (yoffset != 0.0) MouseScrollOrientation.Vertical else MouseScrollOrientation.Horizontal
        )
    }

    glfwSetKeyCallback(windowHandle) { _, key, _, action, _ ->
        val awtId = when (action) {
            GLFW_PRESS -> NativeKeyEvent.KEY_PRESSED
            GLFW_REPEAT -> NativeKeyEvent.KEY_PRESSED
            GLFW_RELEASE -> NativeKeyEvent.KEY_RELEASED
            else -> error("Unknown type")
        }
        val awtKey = glfwToAwtKeyCode(key)
        val time = System.nanoTime() / 1_000_000

        // Note that we don't distinguish between Left/Right Shift, Del from numpad or not, etc.
        // To distinguish we should change `location` parameter
        sendKeyEvent(KeyEvent(awtId, time, getAwtMods(windowHandle), awtKey, 0.toChar(), NativeKeyEvent.KEY_LOCATION_STANDARD))
    }

    glfwSetCharCallback(windowHandle) { _, codepoint ->
        for (char in Character.toChars(codepoint)) {
            val time = System.nanoTime() / 1_000_000
            sendKeyEvent(KeyEvent(NativeKeyEvent.KEY_TYPED, time, getAwtMods(windowHandle), 0, char, NativeKeyEvent.KEY_LOCATION_UNKNOWN))
        }
    }

    glfwSetWindowContentScaleCallback(windowHandle) { _, xscale, _ ->
        density = Density(xscale)
    }
}

private fun glfwGetCursorPos(window: Long): Offset {
    val x = DoubleArray(1)
    val y = DoubleArray(1)
    glfwGetCursorPos(window, x, y)
    return Offset(x[0].toFloat(), y[0].toFloat())
}

// in the future versions of Compose we plan to get rid of the need of AWT events/components
val awtComponent = object : Component() {}

private fun KeyEvent(awtId: Int, time: Long, awtMods: Int, key: Int, char: Char, location: Int) = KeyEvent(
    NativeKeyEvent(awtComponent, awtId, time, awtMods, key, char, location)
)

private fun MouseEvent(awtMods: Int) = MouseEvent(
    awtComponent, 0, 0, awtMods, 0, 0, 1, false
)

private fun getAwtMods(windowHandle: Long): Int {
    var awtMods = 0
    if (glfwGetMouseButton(windowHandle, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS)
        awtMods = awtMods or InputEvent.BUTTON1_DOWN_MASK
    if (glfwGetMouseButton(windowHandle, GLFW_MOUSE_BUTTON_2) == GLFW_PRESS)
        awtMods = awtMods or InputEvent.BUTTON2_DOWN_MASK
    if (glfwGetMouseButton(windowHandle, GLFW_MOUSE_BUTTON_3) == GLFW_PRESS)
        awtMods = awtMods or InputEvent.BUTTON3_DOWN_MASK
    if (glfwGetMouseButton(windowHandle, GLFW_MOUSE_BUTTON_4) == GLFW_PRESS)
        awtMods = awtMods or (1 shl 14)
    if (glfwGetMouseButton(windowHandle, GLFW_MOUSE_BUTTON_5) == GLFW_PRESS)
        awtMods = awtMods or (1 shl 15)
    if (glfwGetKey(windowHandle, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS || glfwGetKey(windowHandle, GLFW_KEY_RIGHT_CONTROL) == GLFW_PRESS)
        awtMods = awtMods or InputEvent.CTRL_DOWN_MASK
    if (glfwGetKey(windowHandle, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS || glfwGetKey(windowHandle, GLFW_KEY_RIGHT_SHIFT) == GLFW_PRESS)
        awtMods = awtMods or InputEvent.SHIFT_DOWN_MASK
    if (glfwGetKey(windowHandle, GLFW_KEY_LEFT_ALT) == GLFW_PRESS || glfwGetKey(windowHandle, GLFW_KEY_RIGHT_ALT) == GLFW_PRESS)
        awtMods = awtMods or InputEvent.ALT_DOWN_MASK
    return awtMods
}
