package com.zeynekurtulus.wayfare.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar

// Context Extensions
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

// Activity Extensions
fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val view = currentFocus ?: View(this)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Activity.showKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

// Fragment Extensions
fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().showToast(message, duration)
}

fun Fragment.showLongToast(message: String) {
    requireContext().showLongToast(message)
}

fun Fragment.hideKeyboard() {
    requireActivity().hideKeyboard()
}

fun Fragment.showKeyboard(view: View) {
    requireActivity().showKeyboard(view)
}

// View Extensions
fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

fun View.showSnackbarWithAction(
    message: String,
    actionText: String,
    action: () -> Unit,
    duration: Int = Snackbar.LENGTH_INDEFINITE
) {
    Snackbar.make(this, message, duration)
        .setAction(actionText) { action() }
        .show()
}

// String Extensions
fun String.isValidEmail(): Boolean = ValidationUtils.isValidEmail(this)

fun String.isValidPassword(): Boolean = ValidationUtils.isValidPassword(this)

fun String.isValidUsername(): Boolean = ValidationUtils.isValidUsername(this)

fun String.capitalize(): String {
    return if (isEmpty()) this else substring(0, 1).uppercase() + substring(1)
}

// LiveData Extensions
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(value: T) {
            observer.onChanged(value)
            removeObserver(this)
        }
    })
}

// Collection Extensions
fun <T> List<T>.isNotNullOrEmpty(): Boolean {
    return !isNullOrEmpty()
}

fun <T> Collection<T>?.isNotNullOrEmpty(): Boolean {
    return !isNullOrEmpty()
}

// Number Extensions
fun Double.roundToDecimals(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}

// Safe cast extension
inline fun <reified T> Any?.safeCast(): T? {
    return this as? T
}