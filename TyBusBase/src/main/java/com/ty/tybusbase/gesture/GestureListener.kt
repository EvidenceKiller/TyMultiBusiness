package com.ty.tybusbase.gesture

interface GestureListener {
    fun onGesture(command: GestureCommand, type: GestureType)
}