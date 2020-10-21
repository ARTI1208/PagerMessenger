package ru.art2000.pager.models

class SimpleAction(
    override val name: Int,
    private val action: (Message) -> Unit,
    private val checker: (Message) -> Boolean,
    private val longChecker: (Message) -> Boolean,
) : MessageAction {

    override fun invoke(message: Message) {
        return action(message)
    }

    override fun displayOnLongClick(message: Message): Boolean {
        return longChecker(message)
    }

    override fun displayOnClick(message: Message): Boolean {
        return checker(message)
    }
}