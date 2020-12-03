package ru.art2000.pager.models

interface CommunicatorPlugin {

    fun transform(message: PagerMessage): PagerMessage

}