package korlibs.korge.ui

import korlibs.event.*
import korlibs.korge.annotations.*
import korlibs.korge.style.*
import korlibs.korge.text.*
import korlibs.korge.view.*
import korlibs.image.color.*
import korlibs.image.font.*
import korlibs.io.async.*
import korlibs.math.geom.*

@KorgeExperimental
inline fun Container.uiTextInput(
    initialText: String = "",
    width: Double = 128.0,
    height: Double = 24.0,
    block: @ViewDslMarker UITextInput.() -> Unit = {}
): UITextInput = UITextInput(initialText, width, height)
    .addTo(this).also { block(it) }

/**
 * Simple Single Line Text Input
 */
@KorgeExperimental
class UITextInput(initialText: String = "", width: Double = 128.0, height: Double = 24.0) :
    UIView(width, height),
    //UIFocusable,
    ISoftKeyboardConfig by SoftKeyboardConfig() {

    //private val bg = ninePatch(NinePatchBmpSlice.createSimple(Bitmap32(3, 3) { x, y -> if (x == 1 && y == 1) Colors.WHITE else Colors.BLACK }.slice(), 1, 1, 2, 2), width, height).also { it.smoothing = false }
    private val bg = renderableView(width, height) {
        styles.uiTextInputBackgroundRender.apply {
            render()
        }
    }
    var skin by bg::viewRenderer
    private val container = clipContainer(0.0, 0.0)
    //private val container = fixedSizeContainer(width - 4.0, height - 4.0).position(2.0, 3.0)
    private val textView = container.text(initialText, 16.0, color = Colors.BLACK, font = DefaultTtfFontAsBitmap)
    //private val textView = container.text(initialText, 16.0, color = Colors.BLACK, font = DefaultTtfFont)
    val controller = TextEditController(textView, textView, this, bg)

    //init { uiScrollable {  } }

    var text: String by controller::text
    var textSize: Double by controller::textSize
    var font: Font by controller::font
    val onReturnPressed: Signal<TextEditController> by controller::onReturnPressed
    val onEscPressed: Signal<TextEditController> by controller::onEscPressed
    val onFocusLost: Signal<TextEditController> by controller::onFocusLost
    var selectionRange: IntRange by controller::selectionRange
    var selectionStart: Int by controller::selectionStart
    var selectionEnd: Int by controller::selectionEnd
    val selectionLength: Int by controller::selectionLength
    fun focus() = controller.focus()
    fun blur() = controller.blur()
    fun selectAll() = controller.selectAll()

    var padding: Margin = Margin(3f, 2f, 2f, 2f)
        set(value) {
            field = value
            onSizeChanged()
        }

    override fun onSizeChanged() {
        bg.setSize(width, height)
        container.bounds(Rectangle(0.0, 0.0, width, height).without(padding))
    }

    init {
        onSizeChanged()
    }

    //override val UIFocusManager.focusView: View get() = this@UITextInput
    //override var tabIndex: Int
    //    get() = TODO("Not yet implemented")
    //    set(value) {}
    //override var focused: Boolean
    //    get() = TODO("Not yet implemented")
    //    set(value) {}
}

var ViewStyles.uiTextInputBackgroundRender: ViewRenderer by ViewStyle(ViewRenderer {
    ctx2d.rect(0.0, 0.0, width, height, Colors.WHITE)
})
