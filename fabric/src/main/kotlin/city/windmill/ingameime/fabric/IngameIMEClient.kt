package city.windmill.ingameime.fabric

import city.windmill.ingameime.client.ConfigHandler
import city.windmill.ingameime.client.IMEHandler
import city.windmill.ingameime.client.KeyHandler
import city.windmill.ingameime.client.ScreenHandler
import city.windmill.ingameime.client.gui.OverlayScreen
import city.windmill.ingameime.client.jni.ExternalBaseIME
import city.windmill.ingameime.fabric.ScreenEvents.EDIT_CARET
import city.windmill.ingameime.fabric.ScreenEvents.EDIT_CLOSE
import city.windmill.ingameime.fabric.ScreenEvents.EDIT_OPEN
import city.windmill.ingameime.fabric.ScreenEvents.SCREEN_CHANGED
import city.windmill.ingameime.fabric.ScreenEvents.WINDOW_SIZE_CHANGED
import ladysnake.satin.api.event.ResolutionChangeCallback
import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks
import me.shedaniel.cloth.api.client.events.v0.ScreenKeyPressedCallback
import me.shedaniel.cloth.api.client.events.v0.ScreenKeyReleasedCallback
import me.shedaniel.cloth.api.client.events.v0.ScreenRenderCallback
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionResult
import org.apache.logging.log4j.LogManager

@Environment(EnvType.CLIENT)
object IngameIMEClient : ClientModInitializer {
    @Suppress("MemberVisibilityCanBePrivate", "PropertyName")
    val LOGGER = LogManager.getFormatterLogger("IngameIME")!!

    /**
     * Track mouse move
     */
    private var prevX = 0
    private var prevY = 0

    override fun onInitializeClient() {
        if (Util.getPlatform() == Util.OS.WINDOWS) {
            LOGGER.info("it is Windows OS! Loading mod...")

            ClientLifecycleEvents.CLIENT_STARTED.register(ClientLifecycleEvents.ClientStarted {
                ConfigHandler.initialConfig()

                ClothClientHooks.SCREEN_LATE_RENDER.register(ScreenRenderCallback.Post { matrixStack, _, _, mouseX, mouseY, delta ->
                    //Track mouse move here
                    if (mouseX != prevX || mouseY != prevY) {
                        ScreenEvents.SCREEN_MOUSE_MOVE.invoker().onMouseMove(prevX, prevY, mouseX, mouseY)

                        prevX = mouseX
                        prevY = mouseY
                    }

                    OverlayScreen.render(matrixStack, mouseX, mouseY, delta)
                })
                ScreenEvents.SCREEN_MOUSE_MOVE.register(ScreenEvents.MouseMove { _, _, _, _ ->
                    IMEHandler.IMEState.onMouseMove()
                })
                ClothClientHooks.SCREEN_KEY_PRESSED.register(ScreenKeyPressedCallback { _, _, keyCode, scanCode, modifier ->
                    if (KeyHandler.KeyState.onKeyDown(keyCode, scanCode, modifier))
                        InteractionResult.CONSUME
                    else
                        InteractionResult.PASS
                })
                ClothClientHooks.SCREEN_KEY_RELEASED.register(ScreenKeyReleasedCallback { _, _, keyCode, scanCode, modifier ->
                    if (KeyHandler.KeyState.onKeyUp(keyCode, scanCode, modifier))
                        InteractionResult.CONSUME
                    else
                        InteractionResult.PASS
                })
                if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("satin"))
                    ResolutionChangeCallback.EVENT.register(ResolutionChangeCallback { _, _ ->
                        ExternalBaseIME.FullScreen = Minecraft.getInstance().window.isFullscreen
                    })
                else {
                    WINDOW_SIZE_CHANGED.register(ScreenEvents.WindowSizeChanged { _, _ ->
                        ExternalBaseIME.FullScreen = Minecraft.getInstance().window.isFullscreen
                    })
                }
                with(ScreenHandler.ScreenState) {
                    SCREEN_CHANGED.register(ScreenEvents.ScreenChanged(::onScreenChange))
                }
                with(ScreenHandler.ScreenState.EditState) {
                    EDIT_OPEN.register(ScreenEvents.EditOpen(::onEditOpen))
                    EDIT_CARET.register(ScreenEvents.EditCaret(::onEditCaret))
                    EDIT_CLOSE.register(ScreenEvents.EditClose(::onEditClose))
                }
                //Ensure native dll are loaded, or crash the game
                LOGGER.info("Current IME State:${ExternalBaseIME.State}")
            })
            KeyBindingHelper.registerKeyBinding(KeyHandler.toggleKey)
        } else
            LOGGER.warn("This mod cant work in ${Util.getPlatform()} !")
    }
}