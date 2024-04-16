package com.reee.fntasker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class EventData (
    var eventType: EventType,
    var params: @RawValue HashMap<String, Any>?
) : Parcelable {

}
