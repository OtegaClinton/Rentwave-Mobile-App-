package uk.ac.tees.mad.e4611415.rentwave.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileImageViewModel : ViewModel() {

    private val _imageUrl = MutableStateFlow("")
    val imageUrl = _imageUrl.asStateFlow()

    fun setImage(url: String) {
        _imageUrl.value = url
    }
    fun clearImage() {
        _imageUrl.value = ""
    }
}
