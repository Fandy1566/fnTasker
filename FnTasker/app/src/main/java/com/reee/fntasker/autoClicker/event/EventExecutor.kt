package com.reee.fntasker.autoClicker.event

import com.reee.fntasker.autoClicker.interfaces.Event


class EventExecutor {
    private val eventList : MutableList<Event> = mutableListOf()

    fun addEvent(event: Event) {
        eventList.add(event)
    }

    fun executeEvents() {
        for (event in eventList)
            event.execute()
    }

    fun clearEvents() {
        eventList.clear()
    }
}